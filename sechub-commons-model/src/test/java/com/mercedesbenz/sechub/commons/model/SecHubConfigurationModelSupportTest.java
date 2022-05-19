package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.test.TestFileReader;

class SecHubConfigurationModelSupportTest {

    private static SecHubConfigurationModel sechub_license_scan_config_binary_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_example;

    private SecHubConfigurationModelSupport supportToTest;
    private static JSONConverter converter = new JSONConverter();
    private static SecHubConfigurationModel sechub_code_scan_config_binary_example;
    private static SecHubConfigurationModel sechub_code_scan_config_source_example;
    private static SecHubConfigurationModel sechub_web_scan_config_source_example;

    @BeforeAll
    static void beforeAll() {
        sechub_license_scan_config_binary_example = loadModel("sechub_license_scan_config_binary_example.json");
        sechub_license_scan_config_source_example = loadModel("sechub_license_scan_config_source_example.json");

        sechub_code_scan_config_binary_example = loadModel("sechub_code_scan_config_binary_example.json");
        sechub_code_scan_config_source_example = loadModel("sechub_code_scan_config_source_example.json");

        sechub_web_scan_config_source_example = loadModel("sechub_web_scan_config_source_example.json");
    }

    @BeforeEach
    void beforeEach() {
        supportToTest = new SecHubConfigurationModelSupport();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................license scan.................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_binary_example__binary_tar_needed_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_binary_example__source_zip_needed_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = false;
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_example__binary_tar_needed_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean sourceZipNeeded = false;
        assertEquals(sourceZipNeeded, result, "Source zip needed must be:" + sourceZipNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_binary_example__source_zip_needed_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................code scan....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_binary_example__binary_tar_needed_only_by_code_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_binary_example__source_zip_needed_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = false;
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_source_examples__binary_tar_needed_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean sourceZipNeeded = false;
        assertEquals(sourceZipNeeded, result, "Source zip needed must be:" + sourceZipNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_source_examples__source_zip_needed_only_by_code_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................web scan ....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_config_source_examples__binary_tar_needed_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean sourceZipNeeded = false;
        assertEquals(sourceZipNeeded, result, "Source zip needed must be:" + sourceZipNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_config_source_examples__source_zip_needed_only_by_code_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean binaryTarNeeded = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(binaryTarNeeded, result, "Binary tar needed must be:" + binaryTarNeeded);
    }

    static SecHubConfigurationModel loadModel(String testFileName) {
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/" + testFileName));
        return converter.fromJSON(SecHubConfigurationModel.class, json);
    }
}
