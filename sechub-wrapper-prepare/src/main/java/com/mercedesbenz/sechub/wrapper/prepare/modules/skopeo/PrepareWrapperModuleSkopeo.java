package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.CREDENTIALS_NOT_DEFINED;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.DOWNLOAD_NOT_SUCCESSFUL;
import static com.mercedesbenz.sechub.wrapper.prepare.upload.UploadExceptionExitCode.SKOPEO_BINARY_UPLOAD_FAILED;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
public class PrepareWrapperModuleSkopeo implements PrepareWrapperModule {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperModuleSkopeo.class);

    @Value("${" + KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED + ":true}")
    private boolean pdsPrepareModuleSkopeoEnabled;

    @Autowired
    SkopeoInputValidator skopeoInputValidator;

    @Autowired
    WrapperSkopeo skopeo;

    @Autowired
    FileNameSupport filesSupport;

    @Autowired
    PrepareWrapperUploadService uploadService;

    @Autowired
    PDSLogSanitizer pdsLogSanitizer;

    @Override
    public boolean prepare(PrepareWrapperContext context) throws IOException {

        if (!pdsPrepareModuleSkopeoEnabled) {
            LOG.debug("Skopeo module is disabled.");
            return false;
        }

        try {
            skopeoInputValidator.validate(context);
        } catch (PrepareWrapperInputValidatorException e) {
            LOG.warn("Module {} could not resolve remote configuration.", getClass().getSimpleName(), e);
            return false;
        }
        LOG.debug("Module {} resolved remote configuration and will prepare.", getClass().getSimpleName());

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        SkopeoContext skopeoContext = initializeSkopeoContext(context, secHubRemoteDataConfiguration);
        prepareRemoteConfiguration(skopeoContext, secHubRemoteDataConfiguration);

        if (!isDownloadSuccessful(skopeoContext)) {
            LOG.error("Download of docker image was not successful.");
            throw new PrepareWrapperUsageException("Download of docker image was not successful.", DOWNLOAD_NOT_SUCCESSFUL);
        }
        cleanup(skopeoContext);

        try {
            uploadService.upload(context, skopeoContext);
        } catch (Exception e) {
            LOG.error("Upload of docker image failed.", e);
            throw new PrepareWrapperUploadException("Upload of docker image failed.", e, SKOPEO_BINARY_UPLOAD_FAILED);
        }
        return true;
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
        skopeoContext.setWorkingDirectory(workingDirectory);

        createDownloadDirectory(skopeoContext.getToolDownloadDirectory());

        return skopeoContext;
    }

    private void cleanup(SkopeoContext skopeoContext) throws IOException {
        skopeo.cleanUploadDirectory(skopeoContext.getToolDownloadDirectory());
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
            addCredentialsToContext(skopeoContext, user);
        }

        skopeo.download(skopeoContext);
    }

    private void addCredentialsToContext(SkopeoContext skopeoContext, SecHubRemoteCredentialUserData user) {
        HashMap<String, SealedObject> credentialMap = new HashMap<>();
        addSealedUserCredentials(user, credentialMap);

        skopeoContext.setCredentialMap(credentialMap);
    }
}
