// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.api.internal.gen.model.*;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinitionTransformer;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;

class RunSecHubJobDefinitionTransformerTest {

    private static final String BINARY_FILE = "binaryFile";
    private static final String BINARIES_FOLDER = "binariesFolder";
    private static final String SOURCE_FOLDER = "sourceFolder";
    private static final String SOURCE_FILE = "sourceFile";

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
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals("projectName1", result.getProjectId());
    }

    @Test
    void configuration_has_api_1_0() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

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
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(codeScan, result.getCodeScan());
    }

    @Test
    void configuration_contains_web_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        Optional<SecHubWebScanConfiguration> webScanOpt = Optional.of(webScan);
        definition.setWebScan(webScanOpt);

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(webScan, result.getWebScan());
    }

    @Test
    void configuration_contains_license_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
        Optional<SecHubLicenseScanConfiguration> licenseScanOpt = Optional.of(licenseScan);
        definition.setLicenseScan(licenseScanOpt);

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(licenseScan, result.getLicenseScan());
    }

    @Test
    void configuration_contains_infrascan_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
        Optional<SecHubInfrastructureScanConfiguration> infraScanOpt = Optional.of(infraScan);
        definition.setInfraScan(infraScanOpt);

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(infraScan, result.getInfraScan());
    }

    @Test
    void configuration_contains_secretscan_scan_from_definition() {
        /* prepare */
        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();
        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
        Optional<SecHubSecretScanConfiguration> secretScanOpt = Optional.of(secretScan);
        definition.setSecretScan(secretScanOpt);

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        assertEquals(secretScan, result.getSecretScan());
    }

    @Test
    void source_uploads_are_transformed_to_data_section() {
        /* prepare */
        String referenceId = "ref1";
        String folder = "somewhere";
        String file = "some-source-file";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));
        upload.setSourceFolder(Optional.of(folder));
        upload.setSourceFile(Optional.of(file));

        definition.getUploads().add(upload);

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        SecHubDataConfiguration data = result.getData();
        assertNotNull(data);
        List<SecHubSourceDataConfiguration> sourceConfigurations = data.getSources();
        assertNotNull(sourceConfigurations);
        assertEquals(1, sourceConfigurations.size());
        SecHubSourceDataConfiguration sourceConfig = sourceConfigurations.iterator().next();
        String uniqueName1 = sourceConfig.getName();
        assertEquals(referenceId, uniqueName1);

        SecHubFileSystemConfiguration fileSystemConfiguration = sourceConfig.getFileSystem();
        assertNotNull(fileSystemConfiguration);
        List<String> folders = fileSystemConfiguration.getFolders();
        assertNotNull(folders);
        assertTrue(folders.contains(folder));

        List<String> files = fileSystemConfiguration.getFiles();
        assertNotNull(files);
        assertTrue(files.contains(file));

    }

    @Test
    void binary_uploads_are_transformed_to_data_section() {
        /* prepare */
        String referenceId = "ref2";
        String folder = "somewhere-bin";
        String file = "some-binary-file";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));
        upload.setBinariesFolder(Optional.of(folder));
        upload.setBinaryFile(Optional.of(file));
        definition.getUploads().add(upload);

        /* execute */
        SecHubConfiguration result = transformerToTest.transformToSecHubConfiguration(definition);

        /* test */
        SecHubDataConfiguration data = result.getData();
        assertNotNull(data);
        List<SecHubBinaryDataConfiguration> binaryConfigurations = data.getBinaries();
        assertNotNull(binaryConfigurations);
        assertEquals(1, binaryConfigurations.size());
        SecHubBinaryDataConfiguration binaryConfig = binaryConfigurations.iterator().next();
        String uniqueName1 = binaryConfig.getName();
        assertEquals(referenceId, uniqueName1);

        SecHubFileSystemConfiguration fileSystemConfiguration = binaryConfig.getFileSystem();
        assertNotNull(fileSystemConfiguration);
        List<String> folders = fileSystemConfiguration.getFolders();
        assertNotNull(folders);
        assertTrue(folders.contains(folder));

        List<String> files = fileSystemConfiguration.getFiles();
        assertNotNull(files);
        assertTrue(files.contains(file));

    }

    @Test
    void transformation_fails_when_no_files_and_no_folders_defined_in_upload_definition() {
        /* prepare */
        String referenceId = "reference-id";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));
        definition.getUploads().add(upload);

        /* execute + test */
        assertThrows(IllegalStateException.class, () -> transformerToTest.transformToSecHubConfiguration(definition));
    }

    /* @formatter:off */
    @ParameterizedTest
    @CsvSource({
        SOURCE_FILE+","+BINARY_FILE,
        SOURCE_FILE+","+BINARIES_FOLDER,
        SOURCE_FOLDER+","+BINARY_FILE,
        SOURCE_FOLDER+","+BINARIES_FOLDER
        })
    /* @formatter:on */
    void transformation_fails_for_invalid_upload_combinations_on_same_reference_id(String type1, String type2) {
        /* prepare */
        String referenceId = "reference-id";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));

        callUploadSetterForType(upload, type1);
        callUploadSetterForType(upload, type2);

        definition.getUploads().add(upload);

        /* execute + test */
        assertThrows(IllegalStateException.class, () -> transformerToTest.transformToSecHubConfiguration(definition));
    }

    /* @formatter:off */
    @ParameterizedTest
    @CsvSource({
        SOURCE_FILE+","+SOURCE_FOLDER,
        BINARY_FILE+","+BINARIES_FOLDER
    })
    /* @formatter:on */
    void transformation_does_not_fails_for_valid_upload_combinations_on_same_reference_id(String type1, String type2) {
        /* prepare */
        String referenceId = "reference-id";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));

        callUploadSetterForType(upload, type1);
        callUploadSetterForType(upload, type2);

        definition.getUploads().add(upload);

        /* execute + test */
        assertDoesNotThrow(() -> transformerToTest.transformToSecHubConfiguration(definition));
    }

    @ParameterizedTest
    @ValueSource(strings = { SOURCE_FILE, SOURCE_FOLDER, BINARIES_FOLDER, BINARY_FILE })
    void transformation_does_not_fails_when_at_least_one_file_or_folder_in_upload_definition(String type) {
        /* prepare */
        String referenceId = "reference-id";

        RunSecHubJobDefinition definition = new RunSecHubJobDefinition();

        UploadDefinition upload = new UploadDefinition();
        upload.setReferenceId(Optional.of(referenceId));

        callUploadSetterForType(upload, type);

        definition.getUploads().add(upload);

        /* execute + test */
        assertDoesNotThrow(() -> transformerToTest.transformToSecHubConfiguration(definition));

    }

    private void callUploadSetterForType(UploadDefinition upload, String type) {
        switch (type) {
        case SOURCE_FILE -> upload.setSourceFile(Optional.of("x"));
        case SOURCE_FOLDER -> upload.setSourceFolder(Optional.of("x"));
        case BINARIES_FOLDER -> upload.setBinariesFolder(Optional.of("y"));
        case BINARY_FILE -> upload.setBinaryFile(Optional.of("y"));
        default -> throw new IllegalStateException("Not tested");
        }
    }

}
