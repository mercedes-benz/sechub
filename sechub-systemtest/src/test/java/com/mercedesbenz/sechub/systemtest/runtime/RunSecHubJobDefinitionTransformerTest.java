package com.mercedesbenz.sechub.systemtest.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubBinaryDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;

class RunSecHubJobDefinitionTransformerTest {

    private RunSecHubJobDefinitionTransformer transformerToTest;

    @BeforeEach
    void beforeEach() {
        transformerToTest = new RunSecHubJobDefinitionTransformer();
    }
    
    @Test
    void configuration_has_project_id_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        definition.setProject("projectName1");
        
        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);
        
        /* test */
        assertEquals("projectName1", result.getProjectId());
    }
    @Test
    void configuration_has_api_1_0() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals("1.0", result.getApiVersion());
    }

    @Test
    void configuration_contains_code_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        Optional<SecHubCodeScanConfiguration> codeScanOpt = Optional.of(codeScan);
        definition.setCodeScan(codeScanOpt);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(codeScan, result.getCodeScan().get());
    }

    @Test
    void configuration_contains_web_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        Optional<SecHubWebScanConfiguration> webScanOpt = Optional.of(webScan);
        definition.setWebScan(webScanOpt);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(webScan, result.getWebScan().get());
    }

    @Test
    void configuration_contains_license_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
        Optional<SecHubLicenseScanConfiguration> licenseScanOpt = Optional.of(licenseScan);
        definition.setLicenseScan(licenseScanOpt);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(licenseScan, result.getLicenseScan().get());
    }

    @Test
    void configuration_contains_infrascan_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
        Optional<SecHubInfrastructureScanConfiguration> infraScanOpt = Optional.of(infraScan);
        definition.setInfraScan(infraScanOpt);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(infraScan, result.getInfraScan().get());
    }

    @Test
    void configuration_contains_secretscan_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
        Optional<SecHubSecretScanConfiguration> secretScanOpt = Optional.of(secretScan);
        definition.setSecretScan(secretScanOpt);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(secretScan, result.getSecretScan().get());
    }

    @Test
    void source_uploads_are_transformed_to_data_section() {
        /* prepare */
        String referenceId = "ref1";
        String folder = "somewhere";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));
        upload.setSourceFolder(Optional.of(folder));

        definition.getUploads().add(upload);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        Optional<SecHubDataConfiguration> dataOpt = result.getData();
        assertTrue(dataOpt.isPresent());
        SecHubDataConfiguration data = dataOpt.get();
        List<SecHubSourceDataConfiguration> sourceConfigurations = data.getSources();
        assertEquals(1, sourceConfigurations.size());
        SecHubSourceDataConfiguration sourceConfig = sourceConfigurations.iterator().next();
        String uniqueName1 = sourceConfig.getUniqueName();
        assertEquals(referenceId, uniqueName1);

        Optional<SecHubFileSystemConfiguration> fileSystemConfigurationOpt = sourceConfig.getFileSystem();
        assertTrue(fileSystemConfigurationOpt.isPresent());
        SecHubFileSystemConfiguration fileSystemConfiguration = fileSystemConfigurationOpt.get();
        List<String> folders = fileSystemConfiguration.getFolders();
        assertTrue(folders.contains(folder));

    }

    @Test
    void binary_uploads_are_transformed_to_data_section() {
        /* prepare */
        String referenceId = "ref2";
        String folder = "somewhere-bin";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));
        upload.setBinariesFolder(Optional.of(folder));

        definition.getUploads().add(upload);

        /* execute */
        SecHubConfigurationModel result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        Optional<SecHubDataConfiguration> dataOpt = result.getData();
        assertTrue(dataOpt.isPresent());
        SecHubDataConfiguration data = dataOpt.get();
        List<SecHubBinaryDataConfiguration> binaryConfigurations = data.getBinaries();
        assertEquals(1, binaryConfigurations.size());
        SecHubBinaryDataConfiguration binaryConfig = binaryConfigurations.iterator().next();
        String uniqueName1 = binaryConfig.getUniqueName();
        assertEquals(referenceId, uniqueName1);

        Optional<SecHubFileSystemConfiguration> fileSystemConfigurationOpt = binaryConfig.getFileSystem();
        assertTrue(fileSystemConfigurationOpt.isPresent());
        SecHubFileSystemConfiguration fileSystemConfiguration = fileSystemConfigurationOpt.get();
        List<String> folders = fileSystemConfiguration.getFolders();
        assertTrue(folders.contains(folder));
    }

}
