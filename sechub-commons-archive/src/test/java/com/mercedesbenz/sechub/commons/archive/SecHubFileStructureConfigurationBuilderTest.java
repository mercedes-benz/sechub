package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;

class SecHubFileStructureConfigurationBuilderTest {

    private SecHubFileStructureConfigurationBuilder builderToTest;

    @BeforeEach
    void beforeEach() {
        builderToTest = SecHubFileStructureConfiguration.builder();
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
    void for_scanType_codescan_and_empty_model_builder_creates_an_configuration() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureConfiguration configuration = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(configuration);
        assertTrue(configuration.getAcceptedReferenceNames().isEmpty());
        assertTrue(configuration.isRootFolderAccepted());
    }

    @Test
    void for_scanType_licensescan_and_empty_model_builder_creates_an_configuration() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        SecHubFileStructureConfiguration configuration = builderToTest.setModel(model).setScanType(ScanType.LICENSE_SCAN).build();

        /* test */
        assertNotNull(configuration);
        assertTrue(configuration.getAcceptedReferenceNames().isEmpty());
        assertFalse(configuration.isRootFolderAccepted());
    }

    @Test
    void for_scanType_codescan_and_model_with_codescan_embedded_filesystem_builder_creates_a_configuration() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        codeScan.setFileSystem(fileSystemConfiguration);
        fileSystemConfiguration.getFolders().add("myfolder");
        model.setCodeScan(codeScan);

        /* execute */
        SecHubFileStructureConfiguration configuration = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(configuration);
        assertTrue(configuration.getAcceptedReferenceNames().isEmpty());
        assertTrue(configuration.isRootFolderAccepted());
    }

    @Test
    void for_scanType_codescan_and_model_with_codescan_by_data_section_filesystem_builder_creates_a_configuration() {
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
        SecHubFileStructureConfiguration configuration = builderToTest.setModel(model).setScanType(ScanType.CODE_SCAN).build();

        /* test */
        assertNotNull(configuration);
        assertTrue(configuration.getAcceptedReferenceNames().contains("test-ref-1"));
        assertTrue(configuration.isRootFolderAccepted());
    }

}
