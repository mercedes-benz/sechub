// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;

class SecHubFileStructureDataProviderBuilderTest {

    private SecHubFileStructureDataProviderBuilder builderToTest;

    @BeforeEach
    void beforeEach() {
        builderToTest = SecHubFileStructureDataProvider.builder();
    }

    @Test
    void include_file_pattern_not_set_results_in_empty_list() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertNotNull(dataProvider.getUnmodifiableIncludeFilePatterns());
        assertTrue(dataProvider.getUnmodifiableIncludeFilePatterns().isEmpty());
    }

    @Test
    void exclude_file_pattern_not_set_results_in_empty_list() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertNotNull(dataProvider.getUnmodifiableExcludeFilePatterns());
        assertTrue(dataProvider.getUnmodifiableExcludeFilePatterns().isEmpty());
    }

    @Test
    void exclude_file_pattern_set_results_in_filled_patterns_in_build_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN)
                .setExcludedFilePatterns(Arrays.asList("*.go", "*.html")).build();

        /* test */
        assertNotNull(dataProvider);
        Set<String> excludeFilePatterns = dataProvider.getUnmodifiableExcludeFilePatterns();

        assertNotNull(excludeFilePatterns);
        assertFalse(excludeFilePatterns.isEmpty());
        assertTrue(excludeFilePatterns.contains("*.go"));
        assertTrue(excludeFilePatterns.contains("*.html"));
    }

    @Test
    void include_file_pattern_set_results_in_filled_patterns_in_build_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN)
                .setIncludedFilePatterns(Arrays.asList("*.go", "*.html")).build();

        /* test */
        assertNotNull(dataProvider);
        Set<String> includeFilePatterns = dataProvider.getUnmodifiableIncludeFilePatterns();

        assertNotNull(includeFilePatterns);
        assertFalse(includeFilePatterns.isEmpty());
        assertTrue(includeFilePatterns.contains("*.go"));
        assertTrue(includeFilePatterns.contains("*.html"));
    }

    @Test
    void nothing_set_throws_illegal_state_exception() {
        assertThrows(IllegalStateException.class, () -> builderToTest.build());
    }

    @Test
    void model_missing_throws_illegal_state_exception() {
        assertThrows(IllegalStateException.class, () -> builderToTest.setScanType(ScanType.CODE_SCAN).build());
    }

    @Test
    void scan_type_missing_throws_illegal_state_exception() {
        assertThrows(IllegalStateException.class, () -> builderToTest.setModel(new SecHubConfigurationModel()).build());
    }

    @Test
    void for_scanType_codescan_and_empty_model_builder_creates_a_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().isEmpty());
        assertTrue(dataProvider.isRootFolderAccepted());
    }

    @Test
    void for_scanType_licensescan_and_empty_model_builder_creates_a_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.LICENSE_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().isEmpty());
        assertFalse(dataProvider.isRootFolderAccepted());
    }

    @Test
    void for_scanType_codescan_and_model_with_codescan_embedded_filesystem_builder_creates_a_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        codeScan.setFileSystem(fileSystemConfiguration);
        fileSystemConfiguration.getFolders().add("myfolder");
        model.setCodeScan(codeScan);

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().isEmpty());
        assertTrue(dataProvider.isRootFolderAccepted());
    }

    @Test
    void for_scanType_analytics_and_model_with_codescan_embedded_filesystem_builder_creates_a_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        codeScan.setFileSystem(fileSystemConfiguration);
        fileSystemConfiguration.getFolders().add("myfolder");
        model.setCodeScan(codeScan);

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.ANALYTICS).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().isEmpty());
        assertTrue(dataProvider.isRootFolderAccepted());
    }

    @Test
    void for_scanType_codescan_and_model_with_codescan_by_data_section_filesystem_builder_creates_a_dataProvider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        codeScan.getNamesOfUsedDataConfigurationObjects().add("test-ref-1");

        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add("myfolder1");

        SecHubSourceDataConfiguration sourceConfig1 = new SecHubSourceDataConfiguration();
        sourceConfig1.setFileSystem(fileSystemConfiguration);
        sourceConfig1.setUniqueName("test-ref-1");

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        data.getSources().add(sourceConfig1);

        model.setData(data);
        codeScan.setFileSystem(fileSystemConfiguration);

        model.setCodeScan(codeScan);

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().contains("test-ref-1"));
        assertTrue(dataProvider.isRootFolderAccepted());
    }

    @Test
    void for_scantype_secretscan_and_source_data_section_create_data_provider() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
        secretScan.getNamesOfUsedDataConfigurationObjects().add("test-ref-1");

        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add("myfolder1");

        SecHubSourceDataConfiguration sourceConfig1 = new SecHubSourceDataConfiguration();
        sourceConfig1.setFileSystem(fileSystemConfiguration);
        sourceConfig1.setUniqueName("test-ref-1");

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        data.getSources().add(sourceConfig1);

        model.setData(data);
        model.setSecretScan(secretScan);

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.SECRET_SCAN).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().contains("test-ref-1"));
        assertFalse(dataProvider.isRootFolderAccepted());
    }

    @Test
    void for_scantype_webscan_sources_data_section_for_api_and_client_certificate_create_data_provider() {
        /* prepare */
        String json = """
                 {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : {
                        "files" : [ "openApi.json" ]
                     }
                    },
                   {
                      "name" : "client-cert-api-file-reference",
                      "fileSystem" : {
                        "files" : [ "certificate.p12" ]
                      }
                    },
                    {
                      "name" : "header-file-ref-for-big-tokens",
                      "fileSystem" : {
                        "files" : [ "bearer-token.txt" ]
                      }
                    }]
                  },
                  "webScan" : {
                    "url" : "https://localhost",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    },
                    "clientCertificate" : {
                      "password" : "secret-password",
                      "use" : [ "client-cert-api-file-reference" ]
                    },
                    "headers" : [{
                      "name" : "Authorization",
                      "use" : [ "header-file-ref-for-big-tokens" ]
                    }]
                  }
                }
                """;
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(ScanType.WEB_SCAN).build();

        /* test */
        assertNotNull(dataProvider);

        Set<String> acceptedReferenceNames = dataProvider.getUnmodifiableSetOfAcceptedReferenceNames();

        assertTrue(acceptedReferenceNames.contains("open-api-file-reference"));
        assertTrue(acceptedReferenceNames.contains("client-cert-api-file-reference"));
        assertTrue(acceptedReferenceNames.contains("header-file-ref-for-big-tokens"));
        assertFalse(dataProvider.isRootFolderAccepted());
    }

}
