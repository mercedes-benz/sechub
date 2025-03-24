// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.*;
import static com.mercedesbenz.sechub.wrapper.prepare.upload.UploadExceptionExitCode.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractPrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadException;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

@Component
public class GitPrepareWrapperModule extends AbstractPrepareWrapperModule {

    private static final Logger LOG = LoggerFactory.getLogger(GitPrepareWrapperModule.class);

    @Value("${" + KEY_PDS_PREPARE_MODULE_GIT_CLONE_WITHOUT_GIT_HISTORY + ":true}")
    boolean cloneWithoutGitHistory;

    @Value("${" + KEY_PDS_PREPARE_MODULE_GIT_REMOVE_GIT_FILES_BEFORE_UPLOAD + ":true}")
    boolean removeGitFilesBeforeUpload;

    @Value("${" + KEY_PDS_PREPARE_MODULE_GIT_ENABLED + ":true}")
    boolean enabled;

    private final GitWrapper gitWrapper;
    private final GitPrepareInputValidator gitPrepareInputValidator;
    private final PrepareWrapperUploadService uploadService;
    private final FileNameSupport filesSupport;
    private final PDSLogSanitizer pdsLogSanitizer;
    private final GitLocationConverter gitLocationConverter;

    public GitPrepareWrapperModule(GitWrapper gitWrapper, GitPrepareInputValidator gitPrepareInputValidator, PrepareWrapperUploadService uploadService,
            FileNameSupport filesSupport, PDSLogSanitizer pdsLogSanitizer, GitLocationConverter gitLocationConverter) {
        this.gitWrapper = gitWrapper;
        this.gitPrepareInputValidator = gitPrepareInputValidator;
        this.uploadService = uploadService;
        this.filesSupport = filesSupport;
        this.pdsLogSanitizer = pdsLogSanitizer;
        this.gitLocationConverter = gitLocationConverter;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isResponsibleToPrepare(PrepareWrapperContext context) {
        return gitPrepareInputValidator.isAccepting(context);
    }

    @Override
    public String getUserMessageForPreparationDone() {
        return "Git repository fetched remote";
    }

    protected void prepareImpl(PrepareWrapperContext context) throws IOException {
        gitPrepareInputValidator.validate(context);

        LOG.debug("Module {} resolved remote configuration and will prepare.", getClass().getSimpleName());

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        GitContext gitContext = initializeGitContext(context, secHubRemoteDataConfiguration);
        ensureDirectoryExists(gitContext.getToolDownloadDirectory());

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Preparing git repository.");
        context.getUserMessages().add(message);

        prepareRemoteConfiguration(gitContext, secHubRemoteDataConfiguration);
        LOG.debug("Remote configuration preparation done");

        assertDownloadSuccessful(gitContext);

        try {
            beforeUpload(gitContext);

            LOG.info("Start git content upload to storage");
            uploadService.upload(context, gitContext);
            LOG.info("Upload to storage succesful");

        } catch (Exception e) {
            LOG.error("Upload of git repository to shared storage failed.", e);
            throw new PrepareWrapperUploadException("Upload of git repository failed.", e, GIT_REPOSITORY_UPLOAD_FAILED);
        }
    }

    private void beforeUpload(GitContext gitContext) throws IOException {
        LOG.debug("Handle before upload: cloneWithoutGitHistory={}, removeGitFilesBeforeUpload={}", cloneWithoutGitHistory, removeGitFilesBeforeUpload);
        if (cloneWithoutGitHistory) {
            gitWrapper.removeGitFolders(gitContext.getToolDownloadDirectory());
        }

        if (removeGitFilesBeforeUpload) {
            gitWrapper.removeAdditionalGitFiles(gitContext.getToolDownloadDirectory());
        }
    }

    private GitContext initializeGitContext(PrepareWrapperContext context, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        Path workingDirectory = Paths.get(context.getEnvironment().getPdsJobWorkspaceLocation());
        String location = secHubRemoteDataConfiguration.getLocation();

        GitContext gitContext = new GitContext();
        gitContext.setCloneWithoutHistory(cloneWithoutGitHistory);
        gitContext.setLocation(location);
        gitContext.setRepositoryName(gitLocationConverter.convertLocationToRepositoryName(location));
        gitContext.init(workingDirectory);

        return gitContext;
    }

    protected void assertDownloadSuccessful(GitContext gitContext) {
        Path path = gitContext.getToolDownloadDirectory();

        if (Files.isDirectory(path)) {
            List<Path> repositories = filesSupport.getRepositoriesFromDirectory(path);

            if (repositories.isEmpty()) {
                LOG.error("Download of git repository was not successful. No repositories found in {}.", path);
                throw new IllegalStateException("Download of git repository was not successful. No repositories found.");
            }

            for (Path repository : repositories) {
                // check if repository folder contains .git directory -> marker that it was
                // succesful
                Path gitPath = repository.resolve(".git");

                if (!Files.exists(gitPath)) {
                    LOG.error("Download of git repository: {} was not successful.", pdsLogSanitizer.sanitize(repository, 1024));
                    throw new PrepareWrapperUsageException("Download of git repository was not successful. Git folder (.git) not found.",
                            DOWNLOAD_NOT_SUCCESSFUL);
                }
            }

        } else {
            LOG.error("Download of git repository was not successful. Git download directory is not a directory: {}", path);
            throw new IllegalStateException("Download of git repository was not successful. Git download directory is not a directory.");
        }
    }

    private void prepareRemoteConfiguration(GitContext gitContext, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        String location = secHubRemoteDataConfiguration.getLocation();
        Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

        if (credentials.isEmpty()) {
            gitWrapper.downloadRemoteData(gitContext);
            return;
        }

        Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
        if (optUser.isEmpty()) {
            throw new PrepareWrapperUsageException("Defined credentials have no credential user data for location: " + pdsLogSanitizer.sanitize(location, 1024),
                    CREDENTIALS_NOT_DEFINED);
        }

        SecHubRemoteCredentialUserData user = optUser.get();
        gitContext.setSealedCredentials(user);

        gitWrapper.downloadRemoteData(gitContext);
    }

}
