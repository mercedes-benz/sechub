package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_GIT_ENABLED;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.CREDENTIALS_NOT_DEFINED;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.DOWNLOAD_NOT_SUCCESSFUL;
import static com.mercedesbenz.sechub.wrapper.prepare.upload.UploadExceptionExitCode.GIT_REPOSITORY_UPLOAD_FAILED;

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
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperUsageException;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadException;
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

    @Autowired
    PDSLogSanitizer pdsLogSanitizer;

    public boolean prepare(PrepareWrapperContext context) throws IOException {

        if (!pdsPrepareModuleGitEnabled) {
            LOG.debug("Git module is disabled.");
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
        GitContext gitContext = initializeGitContext(context, secHubRemoteDataConfiguration);

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Preparing git repository.");
        context.getUserMessages().add(message);

        prepareRemoteConfiguration(gitContext, secHubRemoteDataConfiguration);
        assertDownloadSuccessful(gitContext);
        cleanup(gitContext);

        try {
            uploadService.upload(context, gitContext);
        } catch (Exception e) {
            LOG.error("Upload of git repository to shared storage failed.", e);
            throw new PrepareWrapperUploadException("Upload of git repository failed.", e, GIT_REPOSITORY_UPLOAD_FAILED);
        }

        return true;
    }

    private GitContext initializeGitContext(PrepareWrapperContext context, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        Path workingDirectory = Paths.get(context.getEnvironment().getPdsJobWorkspaceLocation());

        GitContext gitContext = new GitContext();
        gitContext.setCloneWithoutHistory(pdsPrepareAutoCleanupGitFolder);
        gitContext.setLocation(secHubRemoteDataConfiguration.getLocation());
        gitContext.setWorkingDirectory(workingDirectory);

        createDownloadDirectory(gitContext.getToolDownloadDirectory());

        return gitContext;
    }

    protected void assertDownloadSuccessful(GitContext gitContext) {
        // check if download folder contains git
        Path git = Path.of(".git");
        Path path = gitContext.getToolDownloadDirectory();

        if (Files.isDirectory(path)) {
            List<Path> repositories = filesSupport.getRepositoriesFromDirectory(path);

            if (repositories.isEmpty()) {
                LOG.error("Download of git repository was not successful. No repositories found in {}.", path);
                throw new IllegalStateException("Download of git repository was not successful. No repositories found.");
            }

            for (Path repository : repositories) {
                Path gitPath = repository.resolve(git);
                if (!Files.exists(gitPath)) {
                    LOG.error("Download of git repository: {} was not successful.", pdsLogSanitizer.sanitize(repository, 1024));
                    throw new PrepareWrapperUsageException("Download of git repository was not successful. Git folder (.git) not found.",
                            DOWNLOAD_NOT_SUCCESSFUL);
                }
            }

        } else {
            LOG.error("Download of git repository was not successful. Git download directory is not a directory: {}", path);
            throw new RuntimeException("Download of git repository was not successful. Git download directory is not a directory.");
        }
    }

    private void prepareRemoteConfiguration(GitContext gitContext, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        String location = secHubRemoteDataConfiguration.getLocation();
        Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

        if (credentials.isEmpty()) {
            git.downloadRemoteData(gitContext);
            return;
        }

        Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
        if (optUser.isEmpty()) {
            throw new PrepareWrapperUsageException("Defined credentials have no credential user data for location: " + pdsLogSanitizer.sanitize(location, 1024),
                    CREDENTIALS_NOT_DEFINED);
        }

        SecHubRemoteCredentialUserData user = optUser.get();
        clonePrivateRepository(gitContext, user);
    }

    private void clonePrivateRepository(GitContext gitContext, SecHubRemoteCredentialUserData user) {
        HashMap<String, SealedObject> credentialMap = new HashMap<>();
        addSealedUserCredentials(user, credentialMap);

        gitContext.setCredentialMap(credentialMap);
        git.downloadRemoteData(gitContext);
    }

    private void cleanup(GitContext gitContext) throws IOException {
        if (pdsPrepareAutoCleanupGitFolder) {
            git.cleanUploadDirectory(gitContext.getToolDownloadDirectory());
        }
    }
}
