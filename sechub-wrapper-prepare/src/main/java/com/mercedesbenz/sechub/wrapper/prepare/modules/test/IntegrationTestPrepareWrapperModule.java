// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractPrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

@Service
@Profile(PDSProfiles.INTEGRATIONTEST)
/**
 * Special integration test prepare wrapper module. Is always active in profile
 * integration test but only responsible for a defined type "test"
 *
 * @author Albert Tregnaghi
 *
 */
public class IntegrationTestPrepareWrapperModule extends AbstractPrepareWrapperModule {

    @Autowired
    PrepareWrapperUploadService uploadService;

    @Autowired
    TextFileWriter writer;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isResponsibleToPrepare(PrepareWrapperContext context) {
        SecHubRemoteDataConfiguration config = context.getRemoteDataConfiguration();
        if (config == null) {
            return false;
        }
        String type = config.getType();
        if (type == null) {
            return false;
        }
        return type.equals("integrationtest");
    }

    @Override
    public String getUserMessageForPreparationDone() {
        return "Integration test preparation done";
    }

    protected void prepareImpl(PrepareWrapperContext context) throws IOException {

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        IntegrationTestContext integrationTestContext = initializeIntegrationTestContext(context, secHubRemoteDataConfiguration);
        ensureDirectoryExists(integrationTestContext.getToolDownloadDirectory());
        prepareRemoteConfiguration(integrationTestContext, secHubRemoteDataConfiguration);

        uploadService.upload(context, integrationTestContext);

    }

    private IntegrationTestContext initializeIntegrationTestContext(PrepareWrapperContext context,
            SecHubRemoteDataConfiguration secHubRemoteDataConfiguration) {
        Path workingDirectory = Paths.get(context.getEnvironment().getPdsJobWorkspaceLocation());

        IntegrationTestContext integrationTestContext = new IntegrationTestContext();
        integrationTestContext.setLocation(secHubRemoteDataConfiguration.getLocation());
        integrationTestContext.init(workingDirectory);

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

                /*
                 * store data - used by integration test code result importer + inside
                 * integration test
                 */
                Path dataFile = tempDir.resolve("data.txt");

                String mediumIntegrationTestData = """
                        MEDIUM:i am a medium error from IntegrationTestPrepareWrapperModule
                        INFO:i am just an information from IntegrationTestPrepareWrapperModule
                                                """;

                writer.save(dataFile.toFile(), mediumIntegrationTestData, false);

            } catch (IOException e) {
                throw new RuntimeException("Error while files in directory: " + downloadPath, e);
            }
        }
    }

}
