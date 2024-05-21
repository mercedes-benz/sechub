package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

@Service
public class PrepareWrapperModuleSkopeo implements PrepareWrapperModule {

    Logger LOG = LoggerFactory.getLogger(PrepareWrapperModuleSkopeo.class);

    @Value("${" + KEY_PDS_PREPARE_MODULE_SKOPEO_ENABLED + ":true}")
    private boolean pdsPrepareModuleSkopeoEnabled;

    @Autowired
    SkopeoInputValidator skopeoInputValidator;

    @Autowired
    WrapperSkopeo skopeo;

    @Override
    public boolean prepare(PrepareWrapperContext context) throws IOException {
        LOG.debug("Start remote data preparation for Docker repository");

        if (!pdsPrepareModuleSkopeoEnabled) {
            LOG.debug("Skopeo module is disabled");
            return false;
        }

        try {
            skopeoInputValidator.validate(context);
        } catch (PrepareWrapperInputValidatorException e) {
            LOG.warn("Module {} could not resolve remote configuration.", getClass().getSimpleName(), e);
            return false;
        }

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = context.getRemoteDataConfigurationList();

        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : remoteDataConfigurationList) {
            prepareRemoteConfiguration(context, secHubRemoteDataConfiguration);
        }

        if (!isDownloadSuccessful(context)) {
            throw new IOException("Download of docker image was not successful.");
        }
        cleanup(context);
        return true;
    }

    public boolean isDownloadSuccessful(PrepareWrapperContext context) {
        // check if download folder contains a .tar archive
        Path path = Paths.get(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                List<String> result = walk.filter(p -> !Files.isDirectory(p)) // not a directory
                        .map(p -> p.toString().toLowerCase()) // convert path to string
                        .filter(f -> f.endsWith(".tar")) // check end with
                        .toList(); // collect all matched to a List
                return !result.isEmpty();
            } catch (IOException e) {
                throw new RuntimeException("Error while checking download of docker image", e);
            }
        }
        return false;
    }

    private void cleanup(PrepareWrapperContext context) throws IOException {
        skopeo.cleanUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
    }

    private void prepareRemoteConfiguration(PrepareWrapperContext context, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) throws IOException {
        String location = secHubRemoteDataConfiguration.getLocation();
        Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

        if (credentials.isEmpty()) {
            downloadPublicImage(context, location);
            return;
        }

        Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
        if (optUser.isPresent()) {
            SecHubRemoteCredentialUserData user = optUser.get();
            downloadPrivateImage(context, user, location);
            return;
        }

        throw new IllegalStateException("Defined credentials have no credential user data for location: " + location);
    }

    private void downloadPrivateImage(PrepareWrapperContext context, SecHubRemoteCredentialUserData user, String location) throws IOException {

        HashMap<String, SealedObject> credentialMap = new HashMap<>();
        addSealedUserCredentials(user, credentialMap);

        /* @formatter:off */
        SkopeoContext skopeoContext = (SkopeoContext) new SkopeoContext.SkopeoContextBuilder().
                setLocation(location).
                setCredentialMap(credentialMap).
                setUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).
                build();
        /* @formatter:on */

        skopeo.download(skopeoContext);

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Cloned private repository: " + location);
        context.getUserMessages().add(message);
    }

    private void downloadPublicImage(PrepareWrapperContext context, String location) throws IOException {
        /* @formatter:off */
        SkopeoContext skopeoContext = (SkopeoContext) new SkopeoContext.SkopeoContextBuilder().
                setLocation(location).
                setUploadDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).
                build();
        /* @formatter:on */

        skopeo.download(skopeoContext);

        SecHubMessage message = new SecHubMessage(SecHubMessageType.INFO, "Cloned public repository: " + location);
        context.getUserMessages().add(message);
    }
}
