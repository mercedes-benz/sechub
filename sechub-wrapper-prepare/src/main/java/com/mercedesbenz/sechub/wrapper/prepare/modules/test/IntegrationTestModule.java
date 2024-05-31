package com.mercedesbenz.sechub.wrapper.prepare.modules.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

@Service
@Profile(PDSProfiles.INTEGRATIONTEST)
public class IntegrationTestModule implements PrepareWrapperModule {

    @Autowired
    PrepareWrapperUploadService uploadService;

    public boolean prepare(PrepareWrapperContext context) throws IOException {

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        IntegrationTestContext integrationTestContext = initializeIntegrationTestContext(context, secHubRemoteDataConfiguration);
        prepareRemoteConfiguration(integrationTestContext, secHubRemoteDataConfiguration);

        uploadService.upload(context, integrationTestContext);

        return true;
    }

    private IntegrationTestContext initializeIntegrationTestContext(PrepareWrapperContext context,
            SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        Path workingDirectory = Paths.get(context.getEnvironment().getPdsJobWorkspaceLocation());

        IntegrationTestContext integrationTestContext = new IntegrationTestContext();
        integrationTestContext.setLocation(secHubRemoteDataConfiguration.getLocation());
        integrationTestContext.setWorkingDirectory(workingDirectory);

        createDownloadDirectory(integrationTestContext.getToolDownloadDirectory());

        return integrationTestContext;
    }

    private void prepareRemoteConfiguration(IntegrationTestContext integrationTestContext, SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        createIntegrationTestFiles(integrationTestContext);
    }

    private void createIntegrationTestFiles(IntegrationTestContext integrationTestContext) {
        Path downloadPath = integrationTestContext.getToolDownloadDirectory();

        if (Files.exists(downloadPath)) {
            try {
                /* create path that look like git repository */
                Path repository = downloadPath.resolve(integrationTestContext.getRepositoryName());
                if (Files.exists(repository)) {
                    return;
                }
                Path tempDir = Files.createTempDirectory(downloadPath, repository.getFileName().toString());
                Files.createTempFile(tempDir, "integration-test-file_01", ".java");
                Files.createTempFile(tempDir, "integration-test-file_02", ".java");
            } catch (IOException e) {
                throw new RuntimeException("Error while files in directory: " + downloadPath, e);
            }
        }
    }

}
