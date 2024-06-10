// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.*;
import static com.mercedesbenz.sechub.wrapper.prepare.upload.UploadExceptionExitCode.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service
public class SkopeoPrepareWrapperModule extends AbstractPrepareWrapperModule {

    private static final Logger LOG = LoggerFactory.getLogger(SkopeoPrepareWrapperModule.class);

    @Value("${" + KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED + ":true}")
    boolean enabled;

    @Autowired
    SkopeoPrepareInputValidator inputValidator;

    @Autowired
    SkopeoWrapper skopeoWrapper;

    @Autowired
    FileNameSupport filesSupport;

    @Autowired
    PrepareWrapperUploadService uploadService;

    @Autowired
    PDSLogSanitizer pdsLogSanitizer;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isResponsibleToPrepare(PrepareWrapperContext context) {
        return inputValidator.isAccepting(context);
    }

    @Override
    public String getUserMessageForPreparationDone() {
        return "Docker image fetched remote";
    }

    @Override
    protected void prepareImpl(PrepareWrapperContext context) throws IOException {

        /* validate */
        inputValidator.validate(context);

        /* prepare context */
        LOG.debug("Module {} resolved remote configuration and will prepare.", getClass().getSimpleName());

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        SkopeoContext skopeoContext = initializeSkopeoContext(context, secHubRemoteDataConfiguration);
        ensureDirectoryExists(skopeoContext.getToolDownloadDirectory());
        prepareRemoteConfiguration(skopeoContext, secHubRemoteDataConfiguration);

        /* download docker image */
        skopeoWrapper.download(skopeoContext);

        if (!isDownloadSuccessful(skopeoContext)) {
            LOG.error("Download of docker image was not successful.");
            throw new PrepareWrapperUsageException("Download of docker image was not successful.", DOWNLOAD_NOT_SUCCESSFUL);
        }

        /* upload into sechub storage */
        try {
            uploadService.upload(context, skopeoContext);
        } catch (Exception e) {
            LOG.error("Upload of docker image failed.", e);
            throw new PrepareWrapperUploadException("Upload of docker image failed.", e, SKOPEO_BINARY_UPLOAD_FAILED);
        }
    }

    protected boolean isDownloadSuccessful(SkopeoContext skopeoContext) {
        // check if download folder contains a .tar archive
        Path path = skopeoContext.getToolDownloadDirectory();
        List<Path> tarFiles = filesSupport.getTarFilesFromDirectory(path);

        return !tarFiles.isEmpty();
    }

    private SkopeoContext initializeSkopeoContext(PrepareWrapperContext context, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        Path workingDirectory = Paths.get(context.getEnvironment().getPdsJobWorkspaceLocation());

        SkopeoContext skopeoContext = new SkopeoContext();
        skopeoContext.setLocation(secHubRemoteDataConfiguration.getLocation());
        skopeoContext.init(workingDirectory);

        return skopeoContext;
    }

    private void prepareRemoteConfiguration(SkopeoContext skopeoContext, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) throws IOException {
        String location = secHubRemoteDataConfiguration.getLocation();
        Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

        if (credentials.isPresent()) {
            Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
            if (optUser.isEmpty()) {
                throw new PrepareWrapperUsageException(
                        "Defined credentials have no credential user data for location: " + pdsLogSanitizer.sanitize(location, 1024), CREDENTIALS_NOT_DEFINED);
            }

            SecHubRemoteCredentialUserData user = optUser.get();
            skopeoContext.setSealedCredentials(user);
        }

    }

}
