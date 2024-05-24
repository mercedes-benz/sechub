package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

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

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

@Service
public class PrepareWrapperModuleGit implements PrepareWrapperModule {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperModuleGit.class);

    @Value("${" + KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER + ":true}")
    private boolean pdsPrepareAutoCleanupGitFolder;

    @Value("${" + KEY_PDS_PREPARE_MODULE_GIT_ENABLED + ":true}")
    private boolean pdsPrepareModuleGitEnabled;

    @Autowired
    WrapperGit git;

    @Autowired
    GitInputValidator gitInputValidator;

    @Autowired
    PrepareWrapperUploadService uploadService;

    @Autowired
    FileNameSupport filesSupport;

    public boolean prepare(PrepareWrapperContext context) throws IOException {

        if (!pdsPrepareModuleGitEnabled) {
            LOG.debug("Git module is disabled");
            return false;
        }

        try {
            gitInputValidator.validate(context);
        } catch (PrepareWrapperInputValidatorException e) {
            LOG.warn("Module {} could not resolve remote configuration.", getClass().getSimpleName(), e);
            return false;
        }

        LOG.debug("Module {} resolved remote configuration and will prepare.", getClass().getSimpleName());

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();

        prepareRemoteConfiguration(context, secHubRemoteDataConfiguration);

        if (!isDownloadSuccessful(context)) {
            LOG.error("Download of git repository was not successful.");
            throw new IOException("Download of git repository was not successful.");
        }
        cleanup(context);

        uploadService.upload(context);
        return true;
    }

    protected boolean isDownloadSuccessful(PrepareWrapperContext context) {
        // check if download folder contains git

        String uploadFolder = context.getEnvironment().getPdsPrepareUploadFolderDirectory();
        if (Files.isDirectory(Path.of(uploadFolder))) {
            String gitRepo = filesSupport.getSubfolderFileNameFromDirectory(uploadFolder);
            Path path = Paths.get(uploadFolder + "/" + gitRepo).toAbsolutePath();
            if (Files.isDirectory(path)) {
                String gitFile = ".git";
                Path gitPath = Paths.get(path + "/" + gitFile);
                return Files.exists(gitPath);
            }
        }
        return false;
    }

    private void prepareRemoteConfiguration(PrepareWrapperContext context, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) throws IOException {
        String location = secHubRemoteDataConfiguration.getLocation();
        Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

        if (credentials.isEmpty()) {
            clonePublicRepository(context, location);
            return;
        }

        Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
        if (optUser.isEmpty()) {
            throw new IllegalStateException("Defined credentials have no credential user data for location: " + location);
        }

        SecHubRemoteCredentialUserData user = optUser.get();
        clonePrivateRepository(context, user, location);
    }

    private void clonePrivateRepository(PrepareWrapperContext context, SecHubRemoteCredentialUserData user, String location) throws IOException {
        HashMap<String, SealedObject> credentialMap = new HashMap<>();
        addSealedUserCredentials(user, credentialMap);

        /* @formatter:off */
        GitContext gitContext = (GitContext) new GitContext.GitContextBuilder().
                setCloneWithoutHistory(pdsPrepareAutoCleanupGitFolder).
                setLocation(location).
                setCredentialMap(credentialMap).
                setUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).
                build();
        /* @formatter:on */

        git.downloadRemoteData(gitContext);

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Cloned private image: " + location);
        context.getUserMessages().add(message);
        // TODO: 23.05.24 laura isDownloadSuccessful check

    }

    private void clonePublicRepository(PrepareWrapperContext context, String location) {
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
        // TODO: 23.05.24 laura isDownloadSuccessful check
    }

    private void cleanup(PrepareWrapperContext context) throws IOException {
        if (pdsPrepareAutoCleanupGitFolder) {
            git.cleanUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        }
    }
}
