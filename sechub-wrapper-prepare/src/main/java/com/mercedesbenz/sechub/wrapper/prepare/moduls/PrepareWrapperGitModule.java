package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_ENABLED_GIT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

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
public class PrepareWrapperGitModule implements PrepareWrapperModule {

    Logger LOG = LoggerFactory.getLogger(PrepareWrapperGitModule.class);

    private static final String GIT_PATTERN = "((git|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?";
    private static final String TYPE = "git";
    private final Pattern gitPattern = Pattern.compile(GIT_PATTERN);

    @Value("${" + KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER + ":true}")
    private boolean pdsPrepareAutoCleanupGitFolder;

    @Value("${" + KEY_PDS_PREPARE_MODULE_ENABLED_GIT + ":true}")
    private boolean pdsPrepareModuleEnabledGit;

    @Autowired
    PrepareWrapperGIT git;

    public boolean isAbleToPrepare(PrepareWrapperContext context) {
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : context.getRemoteDataConfigurationList()) {
            String location = secHubRemoteDataConfiguration.getLocation();
            // returns true when either the type is git or the location matches a git URL
            if (isMatchingGitType(secHubRemoteDataConfiguration.getType())) {
                LOG.debug("Type is git");
                if (!isMatchingGitPattern(location)) {
                    context.addUserMessage(new SecHubMessage(SecHubMessageType.WARNING, "Type is git but location does not match git URL pattern"));
                    LOG.warn("Type is git but location does not match git URL pattern");
                }
                return true;
            }
            if (isMatchingGitPattern(location)) {
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
            String location = secHubRemoteDataConfiguration.getLocation();
            Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

            Map<String, SealedObject> credentialMap = new HashMap<>();

            if (!credentials.isPresent()) {
                // public repository does not need credentials
                GitContext gitContext = new GitContext(location, pdsPrepareAutoCleanupGitFolder, credentialMap,
                        context.getEnvironment().getPdsPrepareUploadFolderDirectory());
                git.cloneRepository(gitContext);
                SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO,
                        "Cloned public repository " + location + " into " + context.getEnvironment().getPdsPrepareUploadFolderDirectory());
                context.addUserMessage(message);
                return;
            }

            Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
            if (optUser.isPresent()) {
                SecHubRemoteCredentialUserData user = optUser.get();
                String username = user.getName();
                String password = user.getPassword();

                if (username == null || username.isEmpty()) {
                    LOG.error("No username found for User: " + user);
                    throw new IllegalStateException("No username found for User: " + user);
                }
                if (password == null || password.isEmpty()) {
                    LOG.error("No password found for User: " + user);
                    throw new IllegalStateException("No password found for User: " + user);
                }

                SealedObject sealdPassword = CryptoAccess.CRYPTO_STRING.seal(password);
                SealedObject sealdUsername = CryptoAccess.CRYPTO_STRING.seal(username);
                credentialMap.put(PDS_PREPARE_CREDENTIAL_USERNAME, sealdUsername);
                credentialMap.put(PDS_PREPARE_CREDENTIAL_PASSWORD, sealdPassword);

                GitContext gitContext = new GitContext(location, pdsPrepareAutoCleanupGitFolder, credentialMap,
                        context.getEnvironment().getPdsPrepareUploadFolderDirectory());
                git.cloneRepository(gitContext);
                SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO,
                        "Cloned private repository " + location + " into " + context.getEnvironment().getPdsPrepareUploadFolderDirectory());
                context.addUserMessage(message);
                return;
            }

            LOG.error("Defined credentials have no credential user data for location: " + location);
            throw new IllegalStateException("Defined credentials have no credential user data for location: " + location);
        }
    }

    public void cleanup(PrepareWrapperContext context) throws IOException {
        if (pdsPrepareAutoCleanupGitFolder) {
            git.cleanGitDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        }
    }

    public boolean isDownloadSuccessful(PrepareWrapperContext context) {
        // check if download folder contains git
        Path path = Paths.get(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        if (Files.isDirectory(path)) {
            String gitFile = ".git";
            Path gitPath = Paths.get(path + "/" + gitFile);
            return Files.exists(gitPath);
        }
        return false;
    }

    public boolean isModuleEnabled() {
        return pdsPrepareModuleEnabledGit;
    }

    boolean isMatchingGitPattern(String location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        return gitPattern.matcher(location).matches();
    }

    boolean isMatchingGitType(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        type = type.toLowerCase();
        return TYPE.equals(type);
    }
}
