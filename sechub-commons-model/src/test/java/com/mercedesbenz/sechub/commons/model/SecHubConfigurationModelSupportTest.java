// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
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
    private static SecHubConfigurationModel sechub_web_scan_openapi_config_source_example;
    private static SecHubConfigurationModel sechub_web_scan_header_values_from_file_config_source_example;
    private static SecHubConfigurationModel sechub_web_scan_client_certificate_config_source_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_and_binary_binary_used_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_and_binary_both_used_example;
    private static SecHubConfigurationModel sechub_license_scan_config_source_and_binary_source_used_example;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example1;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example2;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example3;
    private static SecHubConfigurationModel sechub_license_and_code_scan_example4;
    private static SecHubConfigurationModel sechub_secret_scan_config_binary_example;
    private static SecHubConfigurationModel sechub_secret_scan_config_source_example;
    private static SecHubConfigurationModel sechub_iac_scan_config_source_example;

    @BeforeAll
    static void beforeAll() {
        /* license scan */
        sechub_license_scan_config_binary_example = loadModel("sechub_license_scan_config_binary_example.json");
        sechub_license_scan_config_source_example = loadModel("sechub_license_scan_config_source_example.json");

        sechub_license_scan_config_source_and_binary_binary_used_example = loadModel("sechub_license_scan_config_source_and_binary_binary_used_example.json");
        sechub_license_scan_config_source_and_binary_both_used_example = loadModel("sechub_license_scan_config_source_and_binary_both_used_example.json");
        sechub_license_scan_config_source_and_binary_source_used_example = loadModel("sechub_license_scan_config_source_and_binary_source_used_example.json");

        /* secret scan */
        sechub_secret_scan_config_binary_example = loadModel("sechub_secret_scan_config_binary_example.json");
        sechub_secret_scan_config_source_example = loadModel("sechub_secret_scan_config_source_example.json");

        /* iac scan */
        sechub_iac_scan_config_source_example = loadModel("sechub_iac_scan_config_source_example.json");

        /* code scan */
        sechub_code_scan_config_binary_example = loadModel("sechub_code_scan_config_binary_example.json");
        sechub_code_scan_config_source_example = loadModel("sechub_code_scan_config_source_example.json");
        sechub_code_scan_config_source_embedded_def_example = loadModel("sechub_code_scan_config_source_embedded_def_example.json");

        /* web scan */
        sechub_web_scan_openapi_config_source_example = loadModel("sechub_web_scan_openapi_config_source_example.json");
        sechub_web_scan_header_values_from_file_config_source_example = loadModel("sechub_web_scan_header_values_from_file_config_source_example.json");
        sechub_web_scan_client_certificate_config_source_example = loadModel("sechub_web_scan_client_certificate_config_source_example.json");

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

    @Test
    void collectScanTypes_empty_model_returns_empty_set() {

        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        Set<ScanType> result = supportToTest.collectScanTypes(model);

        /* test */
        assertEquals(0, result.size());
    }

    @Test
    void collectScanTypes_sechub_license_and_code_scan_example3__has_scan_types_codescan_and_license_scan() {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example3;

        /* execute */
        Set<ScanType> result = supportToTest.collectScanTypes(model);

        /* test */
        assertEquals(2, result.size());
        assertTrue(result.contains(ScanType.CODE_SCAN));
        assertTrue(result.contains(ScanType.LICENSE_SCAN));
    }

    @Test
    void collectScanTypes_all_even_impossible_combinations_are_found() {

        /* prepare */
        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        SecHubCodeScanConfiguration codeScan = mock(SecHubCodeScanConfiguration.class);
        SecHubWebScanConfiguration webScan = mock(SecHubWebScanConfiguration.class);
        SecHubInfrastructureScanConfiguration infraScan = mock(SecHubInfrastructureScanConfiguration.class);
        SecHubLicenseScanConfiguration licenseScan = mock(SecHubLicenseScanConfiguration.class);
        SecHubSecretScanConfiguration secretScan = mock(SecHubSecretScanConfiguration.class);
        SecHubIacScanConfiguration iacScan = mock(SecHubIacScanConfiguration.class);

        when(model.getCodeScan()).thenReturn(Optional.of(codeScan));
        when(model.getInfraScan()).thenReturn(Optional.of(infraScan));
        when(model.getWebScan()).thenReturn(Optional.of(webScan));
        when(model.getLicenseScan()).thenReturn(Optional.of(licenseScan));
        when(model.getSecretScan()).thenReturn(Optional.of(secretScan));
        when(model.getIacScan()).thenReturn(Optional.of(iacScan));

        /* execute */
        Set<ScanType> result = supportToTest.collectScanTypes(model);

        /* test */

        // Here we check that ALL non internal scan types are found
        // if this fails, a new data scan type was introduced, but
        // forgotten to add into implementation of support!
        for (ScanType scanType : ScanType.values()) {
            if (scanType.isInternalScanType()) {
                if (result.contains(scanType)) {
                    fail("Internal scan type " + scanType + " may not be found. This is a support implementation failure!");
                }
            } else {
                if (!result.contains(scanType)) {
                    fail("Public scan type " + scanType
                            + " must be found, but wasn't.\nSeems there was a new scan type introduced, but not added to this test and/or to the support implementation!");
                }
            }
        }

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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
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
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
    void sechub_license_and_code_scan_example3__source_required_by_codescan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_license_and_code_scan_example3;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.LICENSE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
        assertEquals(needed, result, "Source zip needed must be:" + needed);
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
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
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
        boolean needed = ScanType.CODE_SCAN.equals(scanType) || ScanType.ANALYTICS.equals(scanType);
        ;
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................web scan ....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_openapi_config_source_examples__binary_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_openapi_config_source_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean binaryNeeded = false;
        assertEquals(binaryNeeded, result, "Binary needed must be:" + binaryNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_client_certificate_config_source_examples__binary_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_client_certificate_config_source_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean binaryNeeded = false;
        assertEquals(binaryNeeded, result, "Binary needed must be:" + binaryNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_header_values_from_file_config_source_example__binary_required_never(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_header_values_from_file_config_source_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean binaryNeeded = false;
        assertEquals(binaryNeeded, result, "Binary needed must be:" + binaryNeeded);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan__openapi_config_source_example__source_required_only_by_web_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_openapi_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.WEB_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan__client_certificate_config_source_example__source_required_only_by_web_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_client_certificate_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.WEB_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_web_scan_header_values_from_file_config_source_examples__source_required_only_by_web_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_web_scan_header_values_from_file_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.WEB_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be:" + needed);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ........secret scan (one definition)............ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_secret_scan_config_binary_example__binary_required_only_by_secret_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_secret_scan_config_binary_example;

        /* execute */
        boolean result = supportToTest.isBinaryRequired(scanType, model);

        /* test */
        boolean needed = ScanType.SECRET_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be: " + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_secret_scan_config_source_example__source_required_only_by_secret_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_secret_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.SECRET_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be: " + needed);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void sechub_iac_scan_config_source_example__source_required_only_by_iac_scan(ScanType scanType) {

        /* prepare */
        SecHubConfigurationModel model = sechub_iac_scan_config_source_example;

        /* execute */
        boolean result = supportToTest.isSourceRequired(scanType, model);

        /* test */
        boolean needed = ScanType.IAC_SCAN.equals(scanType);
        assertEquals(needed, result, "Needed must be: " + needed);
    }

    @ParameterizedTest
    @ArgumentsSource(SourceRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider.class)
    void isSourceRequired_returns_true_when_scan_type_uses_sourcecode_archive_root(String variant, ScanType check, List<ScanType> defined, List<String> use) {
        /* prepare */
        SecHubConfigurationModel model = createConfigurationUsingArchiveRootReferenceInTypes(defined, use);

        /* execute */
        boolean required = supportToTest.isSourceRequired(check, model);

        /* test */
        if (!required) {
            fail("Expected source required=true, but was not for configuration:\n" + JSONConverter.get().toJSON(model, true));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BinaryRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider.class)
    void isBinaryRequired_returns_true_when_scan_type_uses_binaries_archive_root(String variant, ScanType check, List<ScanType> defined, List<String> use) {
        /* prepare */
        SecHubConfigurationModel model = createConfigurationUsingArchiveRootReferenceInTypes(defined, use);

        /* execute */
        boolean required = supportToTest.isBinaryRequired(check, model);

        /* test */
        if (!required) {
            fail("Expected binaries required=true, but was not for configuration:\n" + JSONConverter.get().toJSON(model, true));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(NoSourceRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider.class)
    void isSourceRequired_returns_false_when_scan_type_not_uses_sourcecode_archive_root(String variant, ScanType check, List<ScanType> defined,
            List<String> use) {
        /* prepare */
        SecHubConfigurationModel model = createConfigurationUsingArchiveRootReferenceInTypes(defined, use);

        /* execute */
        boolean required = supportToTest.isSourceRequired(check, model);

        /* test */
        if (required) {
            fail("Expected source required=false, but was required for configuration:\n" + JSONConverter.get().toJSON(model, true));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(NoBinaryRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider.class)
    void isBinaryRequired_returns_false_when_scan_type_not_uses_binaries_archive_root(String variant, ScanType check, List<ScanType> defined,
            List<String> use) {
        /* prepare */
        SecHubConfigurationModel model = createConfigurationUsingArchiveRootReferenceInTypes(defined, use);

        /* execute */
        boolean required = supportToTest.isBinaryRequired(check, model);

        /* test */
        if (required) {
            fail("Expected binaries required=false, but was required for configuration:\n" + JSONConverter.get().toJSON(model, true));
        }
    }

    private static class SourceRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of("C1", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER) ),
                    Arguments.of("C2", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("C3", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER ) ),
                    Arguments.of("C4", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("S1", ScanType.SECRET_SCAN, List.of(ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S2", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("S3", ScanType.SECRET_SCAN, List.of(ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S4", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("L1", ScanType.LICENSE_SCAN, List.of(ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L2", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("L3", ScanType.LICENSE_SCAN, List.of(ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L4", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W1", ScanType.WEB_SCAN, List.of(ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W2", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W3", ScanType.WEB_SCAN, List.of(ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W4", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER))

              );
        }
        /* @formatter:on*/
    }

    private static class BinaryRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of("C1", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER) ),
              Arguments.of("C2", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("C3", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER ) ),
              Arguments.of("C4", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("S1", ScanType.SECRET_SCAN, List.of(ScanType.SECRET_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
              Arguments.of("S2", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("S3", ScanType.SECRET_SCAN, List.of(ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
              Arguments.of("S4", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("L1", ScanType.LICENSE_SCAN, List.of(ScanType.LICENSE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
              Arguments.of("L2", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("L3", ScanType.LICENSE_SCAN, List.of(ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
              Arguments.of("L4", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("W1", ScanType.WEB_SCAN, List.of(ScanType.WEB_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
              Arguments.of("W2", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.WEB_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

              Arguments.of("W3", ScanType.WEB_SCAN, List.of(ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
              Arguments.of("W4", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER))

              );
        }
        /* @formatter:on*/
    }

    private static class NoSourceRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of("C1", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER) ),
                    Arguments.of("C2", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("C3", ScanType.CODE_SCAN, List.of(ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER ) ),
                    Arguments.of("C4", ScanType.CODE_SCAN, List.of(ScanType.SECRET_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("S1", ScanType.SECRET_SCAN, List.of(ScanType.SECRET_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S2", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("S3", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S4", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("L1", ScanType.LICENSE_SCAN, List.of(ScanType.LICENSE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L2", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("L3", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L4", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN, ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W1", ScanType.WEB_SCAN, List.of(ScanType.WEB_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W2", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.WEB_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W3", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W4", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER))

                    );
        }
        /* @formatter:on*/
    }

    private static class NoBinaryRequiredConfigurationWithArchiveRootUsedByScanTypeArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of("C1", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER) ),
                    Arguments.of("C2", ScanType.CODE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("C3", ScanType.CODE_SCAN, List.of(ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER ) ),
                    Arguments.of("C4", ScanType.CODE_SCAN, List.of(ScanType.SECRET_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("S1", ScanType.SECRET_SCAN, List.of(ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S2", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("S3", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN), List.of(BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("S4", ScanType.SECRET_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("L1", ScanType.LICENSE_SCAN, List.of(ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L2", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("L3", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("L4", ScanType.LICENSE_SCAN, List.of(ScanType.CODE_SCAN, ScanType.SECRET_SCAN, ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W1", ScanType.WEB_SCAN, List.of(ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W2", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.WEB_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),

                    Arguments.of("W3", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER)),
                    Arguments.of("W4", ScanType.WEB_SCAN, List.of(ScanType.CODE_SCAN, ScanType.LICENSE_SCAN, ScanType.SECRET_SCAN), List.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER))

                    );
        }
        /* @formatter:on*/
    }

    private SecHubConfigurationModel createConfigurationUsingArchiveRootReferenceInTypes(List<ScanType> typesToHaveArchiveRootReference,
            List<String> usedReferences) {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        for (ScanType type : typesToHaveArchiveRootReference) {
            switch (type) {
            case CODE_SCAN:
                model.setCodeScan(withReferences(new SecHubCodeScanConfiguration(), usedReferences));
                break;
            case INFRA_SCAN:
                break;
            case LICENSE_SCAN:
                model.setLicenseScan(withReferences(new SecHubLicenseScanConfiguration(), usedReferences));
                break;
            case SECRET_SCAN:
                model.setSecretScan(withReferences(new SecHubSecretScanConfiguration(), usedReferences));
                break;
            case WEB_SCAN:
                SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
                ClientCertificateConfiguration x = withReferences(new ClientCertificateConfiguration(), usedReferences);
                webScan.setClientCertificate(Optional.of(x));
                model.setWebScan(webScan);
                break;
            default:
                throw new IllegalStateException(
                        "Test corrupt! given scan type '" + type + "' does either not offer usage references or this is not implemented inside test data");
            }
        }
        return model;
    }

    <T extends SecHubDataConfigurationUsageByName> T withReferences(T target, List<String> usedReferences) {
        target.getNamesOfUsedDataConfigurationObjects().addAll(usedReferences);
        return target;
    }

    static SecHubConfigurationModel loadModel(String testFileName) {
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/" + testFileName));
        return converter.fromJSON(SecHubConfigurationModel.class, json);
    }
}
