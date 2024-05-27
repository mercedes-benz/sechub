package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED;

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
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
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
            throw new IOException("Download of docker image was not successful.");
        }
        cleanup(skopeoContext);

        uploadService.upload(context, skopeoContext);
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
                throw new IllegalStateException("Defined credentials have no credential user data for location: " + location);
            }

            SecHubRemoteCredentialUserData user = optUser.get();
            addCredentialsToContext(skopeoContext, user, location);
        }

        skopeo.download(skopeoContext);
    }

    private void addCredentialsToContext(SkopeoContext skopeoContext, SecHubRemoteCredentialUserData user, String location) throws IOException {
        HashMap<String, SealedObject> credentialMap = new HashMap<>();
        addSealedUserCredentials(user, credentialMap);

        skopeoContext.setCredentialMap(credentialMap);
    }
}
