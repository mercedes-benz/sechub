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
    private static SecHubConfigurationModel sechub_code_scan_config_source_embedded_def_example;
    private static SecHubConfigurationModel sechub_web_scan_config_source_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_and_binary_binary_used_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_and_binary_both_used_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_and_binary_source_used_example;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example1;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example2;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example3;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example4;

    @BeforeAll
    static void beforeAll() {
        /* license scan */
        sechub_license_scan_config_binary_example = loadModel("sechub_license_scan_config_binary_example.json");
        sechub_license_scan_config_source_example = loadModel("sechub_license_scan_config_source_example.json");

        sechub_license_scan_config_source_and_binary_binary_used_example = loadModel("sechub_license_scan_config_source_and_binary_binary_used_example.json");
        sechub_license_scan_config_source_and_binary_both_used_example = loadModel("sechub_license_scan_config_source_and_binary_both_used_example.json");
        sechub_license_scan_config_source_and_binary_source_used_example = loadModel("sechub_license_scan_config_source_and_binary_source_used_example.json");

        /* code scan */
        sechub_code_scan_config_binary_example = loadModel("sechub_code_scan_config_binary_example.json");
        sechub_code_scan_config_source_example = loadModel("sechub_code_scan_config_source_example.json");
        sechub_code_scan_config_source_embedded_def_example = loadModel("sechub_code_scan_config_source_embedded_def_example.json");

        /* web scan */
        sechub_web_scan_config_source_example = loadModel("sechub_web_scan_config_source_example.json");

        /* multi */
        sechub_license_and_code_scan_example1 = loadModel("sechub_license_and_code_scan_example1.json");
        sechub_license_and_code_scan_example2 = loadModel("sechub_license_and_code_scan_example2.json");
        sechub_license_and_code_scan_example3 = loadModel("sechub_license_and_code_scan_example3.json");
        sechub_license_and_code_scan_example4 = loadModel("sechub_license_and_code_scan_example4.json");
    }

    @BeforeEach
    void beforeEach() {
        supportToTest = new SecHubConfigurationModelSupport();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + .......code + license scan (multi).............. + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example3__binary_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example3;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example4__source_required_by_codescan_only(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example4;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example4__binary_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example4;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = false;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example3__source_required_by_codescan_and_licensescan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example3;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example2__binary_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example2;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example2__source_required_by_codescan_only(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example2;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example1__binary_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example1;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example1__source_required_by_license_scan_and_codescan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example1;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.CODE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_and_code_scan_example1__binary_required_by_license_scan_only(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example1;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + .......license scan (one definition)............ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_binary_example__binary_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_binary_example__source_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = false;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_example__binary_required_never(ScanType scanType) {

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
    void sechub_license_scan_config_binary_example__source_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + .......license scan.(two defined, one used)...... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_and_binary_binary_used_example__binary_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_and_binary_binary_used_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_and_binary_binary_used_example__source_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_and_binary_binary_used_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = false;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_and_binary_source_used_example__binary_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_and_binary_source_used_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = false;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_and_binary_source_used_example__source_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_and_binary_source_used_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + .......license scan.(two defined, both used)...... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_and_binary_both_used_example__source_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_and_binary_both_used_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_license_scan_config_source_and_binary_both_used_example__binary_required_only_by_license_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_scan_config_source_and_binary_both_used_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean sourceZipNeeded = ScanType.LICENSE_SCAN.equals(scanType);
        assertEquals(sourceZipNeeded, result, "Source zip needed must be:" + sourceZipNeeded);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................code scan (embedded def)........ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_source_embedded_def_example__binary_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_source_embedded_def_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = false;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_source_embedded_def_example__source_required_only_by_code_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_source_embedded_def_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................code scan....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_binary_example__binary_required_only_by_code_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_binary_example__source_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = false;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_code_scan_config_source_examples__binary_required_never(ScanType scanType) {

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
    void sechub_code_scan_config_source_examples__source_required_only_by_code_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_code_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................web scan ....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_config_source_examples__binary_required_never(ScanType scanType) {

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
    void sechub_web_scan_config_source_examples__source_required_only_by_web_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.WEB_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    static SecHubConfigurationModel loadModel(String testFileName) {
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/" + testFileName));
        return converter.fromJSON(SecHubConfigurationModel.class, json);
    }
}
