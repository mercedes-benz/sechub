package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_GIT_ENABLED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public class PrepareWrapperModuleGit implements PrepareWrapperModule {

    Logger LOG = LoggerFactory.getLogger(PrepareWrapperModuleGit.class);

    private static final String TYPE = "git";

    @Value("${" + KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER + ":true}")
    private boolean pdsPrepareAutoCleanupGitFolder;

    @Value("${" + KEY_PDS_PREPARE_MODULE_GIT_ENABLED + ":true}")
    private boolean pdsPrepareModuleGitEnabled;

    @Autowired
    WrapperGit git;

    @Autowired
    GitInputValidator gitInputValidator;

    public boolean isAbleToPrepare(PrepareWrapperContext context) {

        if (!pdsPrepareModuleGitEnabled) {
            LOG.debug("Git module is disabled");
            return false;
        }

        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : context.getRemoteDataConfigurationList()) {
            String location = secHubRemoteDataConfiguration.getLocation();

            gitInputValidator.validateLocationCharacters(location, null);

            if (isMatchingGitType(secHubRemoteDataConfiguration.getType())) {
                LOG.debug("Type is git");
                if (!gitInputValidator.validateLocation(location)) {
                    context.getUserMessages().add(new SecHubMessage(SecHubMessageType.WARNING, "Type is git but location does not match git URL pattern"));
                    LOG.warn("User defined type as 'git', but the defined location was not a valid git location: {}", location);
                    return false;
                }
                return true;
            }

            if (gitInputValidator.validateLocation(location)) {
                LOG.debug("Location is a git URL");
                return true;
            }

        }
        return false;
    }

    public void prepare(PrepareWrapperContext context) throws IOException {

        LOG.debug("Start remote data preparation for GIT repository");

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = context.getRemoteDataConfigurationList();

        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : remoteDataConfigurationList) {
            prepareRemoteConfiguration(context, secHubRemoteDataConfiguration);
        }

        if (!isDownloadSuccessful(context)) {
            throw new IOException("Download of git repository was not successful.");
        }
        cleanup(context);
    }

    boolean isMatchingGitType(String type) {
        if (type == null || type.isBlank()) {
            return false;
        }
        return TYPE.equalsIgnoreCase(type);
    }

    boolean isDownloadSuccessful(PrepareWrapperContext context) {
        // check if download folder contains git
        Path path = Paths.get(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        if (Files.isDirectory(path)) {
            String gitFile = ".git";
            Path gitPath = Paths.get(path + "/" + gitFile);
            return Files.exists(gitPath);
        }
        return false;
    }

    private void prepareRemoteConfiguration(PrepareWrapperContext context, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) throws IOException {
        String location = secHubRemoteDataConfiguration.getLocation();
        Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

        if (!credentials.isPresent()) {
            clonePublicRepository(context, location);
            return;
        }

        Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
        if (optUser.isPresent()) {
            SecHubRemoteCredentialUserData user = optUser.get();
            clonePrivateRepository(context, user, location);
            return;
        }

        throw new IllegalStateException("Defined credentials have no credential user data for location: " + location);
    }

    private void clonePrivateRepository(PrepareWrapperContext context, SecHubRemoteCredentialUserData user, String location) throws IOException {
        String username = user.getName();
        String password = user.getPassword();

        assertUserCredentials(username, password);

        HashMap<String, SealedObject> credentialMap = new HashMap<>();
        addSealedUserCredentials(password, username, credentialMap);

        /* @formatter:off */
        GitContext gitContext = (GitContext) new GitContext.GitContextBuilder().
                setCloneWithoutHistory(pdsPrepareAutoCleanupGitFolder).
                setLocation(location)
                .setCredentialMap(credentialMap).
                setUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).
                build();
        /* @formatter:on */

        git.downloadRemoteData(gitContext);

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Cloned public repository: " + location);
        context.getUserMessages().add(message);
    }

    private static void addSealedUserCredentials(String password, String username, HashMap<String, SealedObject> credentialMap) {
        SealedObject sealedPassword = CryptoAccess.CRYPTO_STRING.seal(password);
        SealedObject sealedUsername = CryptoAccess.CRYPTO_STRING.seal(username);
        credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, sealedUsername);
        credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, sealedPassword);
    }

    private void assertUserCredentials(String username, String password) {
        gitInputValidator.validateUsername(username);
        gitInputValidator.validatePassword(password);
    }

    private void clonePublicRepository(PrepareWrapperContext context, String location) throws IOException {
        /* @formatter:off */
        GitContext contextGit = (GitContext) new GitContext.GitContextBuilder().
                setCloneWithoutHistory(pdsPrepareAutoCleanupGitFolder).
                setLocation(location).
                setUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).
                build();
        /* @formatter:on */

        git.downloadRemoteData(contextGit);

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Cloned public repository: " + location);
        context.getUserMessages().add(message);
    }

    private void cleanup(PrepareWrapperContext context) throws IOException {
        if (pdsPrepareAutoCleanupGitFolder) {
            git.cleanUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        }
    }
}
