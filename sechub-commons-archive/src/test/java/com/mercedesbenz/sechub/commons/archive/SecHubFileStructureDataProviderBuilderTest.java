// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubIacScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

class SecHubFileStructureDataProviderBuilderTest {

    private SecHubFileStructureDataProviderBuilder builderToTest;

    @BeforeEach
    void beforeEach() {
        builderToTest = SecHubFileStructureDataProvider.builder();
    }

    /*
     * The file data structure provide shall NEVER accept folders of reserved
     * archive root references but instead allow the root folder in this case.
     */
    @ParameterizedTest
    @ArgumentsSource(RootFolderAcceptedByArchiveRootReferenceOnlyArgumentsProvider.class)
    void reserved_archive_root_references_results_in_root_acceptance_but_not_in_accepted_reference_names(String variant, ScanType type, List<String> use) {
        /* prepare */
        SecHubConfigurationModel model = createModelForTypeWithUsages(type, use);

        /* execute */
        SecHubFileStructureDataProvider result = builderToTest.setModel(model).setScanType(type).build();

        /* test 1 */
        assertTrue(result.isRootFolderAccepted());

        /* test 2 - root references are not listed */
        Set<String> acceptedReferenceIds = result.getUnmodifiableSetOfAcceptedReferenceNames();
        for (String rootReferenceId : CommonConstants.getAllRootArchiveReferenceIdentifiers()) {
            assertFalse(acceptedReferenceIds.contains(rootReferenceId));
        }

        /* test 3 - other references are still inside */
        List<String> useWithoutArchiveRoots = new ArrayList<>(use);
        useWithoutArchiveRoots.removeAll(CommonConstants.getAllRootArchiveReferenceIdentifiers());

        assertTrue(acceptedReferenceIds.containsAll(useWithoutArchiveRoots));
    }

    private SecHubConfigurationModel createModelForTypeWithUsages(ScanType type, List<String> references) {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        switch (type) {
        case CODE_SCAN:
            SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
            model.setCodeScan(codeScan);
            addUsages(codeScan, references);
            break;
        case LICENSE_SCAN:
            SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
            model.setLicenseScan(licenseScan);
            addUsages(licenseScan, references);
            break;
        case SECRET_SCAN:
            SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
            model.setSecretScan(secretScan);

            addUsages(secretScan, references);
            break;
        case IAC_SCAN:
            SecHubIacScanConfiguration iacScan = new SecHubIacScanConfiguration();
            model.setIacScan(iacScan);

            addUsages(iacScan, references);
            break;
        case WEB_SCAN:
            SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
            model.setWebScan(webScan);
            ClientCertificateConfiguration clientCertificate = new ClientCertificateConfiguration();
            webScan.setClientCertificate(Optional.of(clientCertificate));
            addUsages(clientCertificate, references);
            break;
        default:
            throw new IllegalStateException("Not implemented for this test!");

        }
        return model;
    }

    <T extends SecHubDataConfigurationUsageByName> T addUsages(T usage, List<String> references) {
        usage.getNamesOfUsedDataConfigurationObjects().addAll(references);
        return usage;
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

    @ParameterizedTest
    @ArgumentsSource(EmptyModelArgumentsBuilderArgumentProvider.class)
    void for_this_scanType_and_an_empty_model_the_builder_creates_a_dataProvider(ScanType scanType, boolean rootFolderAccepted) {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureDataProvider dataProvider = builderToTest.setModel(model).setScanType(scanType).build();

        /* test */
        assertNotNull(dataProvider);
        assertTrue(dataProvider.getUnmodifiableSetOfAcceptedReferenceNames().isEmpty());
        assertEquals(dataProvider.isRootFolderAccepted(), rootFolderAccepted);
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

    private static class RootFolderAcceptedByArchiveRootReferenceOnlyArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    /* Arguments: variant, scanType, list with reference names to use */
                    Arguments.of("C0", ScanType.CODE_SCAN, List.of()), // code scan always accepts root - this is tested here as well!
                    Arguments.of("C1", ScanType.CODE_SCAN, List.of(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("C2", ScanType.CODE_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("C3", ScanType.CODE_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("C4", ScanType.CODE_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, "additional1")),

                    Arguments.of("I1", ScanType.IAC_SCAN, List.of(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("I2", ScanType.IAC_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("I3", ScanType.IAC_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("I4", ScanType.IAC_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, "additional1")),

                    Arguments.of("S1", ScanType.SECRET_SCAN, List.of(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S2", ScanType.SECRET_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S3", ScanType.SECRET_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S4", ScanType.SECRET_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER,"additional2")),

                    Arguments.of("L1", ScanType.LICENSE_SCAN, List.of(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L2", ScanType.LICENSE_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L3", ScanType.LICENSE_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, "additional3")),
                    Arguments.of("L4", ScanType.LICENSE_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W1", ScanType.WEB_SCAN, List.of(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W2", ScanType.WEB_SCAN, List.of(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, "addtional4")),
                    Arguments.of("W3", ScanType.WEB_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W4", ScanType.WEB_SCAN, List.of(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER))

                    );

        }
        /* @formatter:on*/
    }

    private static class EmptyModelArgumentsBuilderArgumentProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              /* Arguments: scanType, rootFolderAccepted */
              Arguments.of(ScanType.CODE_SCAN, true),
              Arguments.of(ScanType.LICENSE_SCAN, false),
              Arguments.of(ScanType.SECRET_SCAN, false),
              Arguments.of(ScanType.IAC_SCAN, false))
              ;
        }
        /* @formatter:on*/
    }

}
