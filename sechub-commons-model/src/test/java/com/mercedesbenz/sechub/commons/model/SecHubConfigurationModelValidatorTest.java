// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult.SecHubConfigurationModelValidationErrorData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator.SecHubConfigurationModelValidationException;
import com.mercedesbenz.sechub.test.TestFileReader;

class SecHubConfigurationModelValidatorTest {

    private static final String VALID_NAME_WITH_MAX_LENGTH = "---------1---------2---------3---------4---------5---------6---------7---------8";
    private static final String VALID_NAME_BUT_ONE_CHAR_TOO_LONG = VALID_NAME_WITH_MAX_LENGTH + "-";
    private SecHubConfigurationModelValidator validatorToTest;
    private SecHubConfigurationModelSupport modelSupport;
    private Set<ScanType> modelSupportCollectedScanTypes;

    @BeforeEach
    private void beforeEach() {

        modelSupportCollectedScanTypes = new LinkedHashSet<>();

        modelSupport = mock(SecHubConfigurationModelSupport.class);

        validatorToTest = new SecHubConfigurationModelValidator();
        validatorToTest.modelSupport = modelSupport;

        when(modelSupport.collectPublicScanTypes(any(SecHubConfigurationModel.class))).thenReturn(modelSupportCollectedScanTypes);
    }

    @Test
    void when_no_scan_type_is_set_validation_fails_with_CONTAINS_NO_SCAN_CONFIGURATION() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, CONTAINS_NO_SCAN_CONFIGURATION);
    }

    @Test
    void when_scan_type_codescan_validation_fails_NOT_with_CONTAINS_NO_SCAN_CONFIGURATION() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setCodeScan(new SecHubCodeScanConfiguration());

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNotError(result, CONTAINS_NO_SCAN_CONFIGURATION);
    }

    @Test
    void when_scan_type_licensescan_validation_fails_NOT_with_CONTAINS_NO_SCAN_CONFIGURATION() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setLicenseScan(new SecHubLicenseScanConfiguration());

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNotError(result, CONTAINS_NO_SCAN_CONFIGURATION);
    }

    @Test
    void when_scan_type_webscan_validation_fails_NOT_with_CONTAINS_NO_SCAN_CONFIGURATION() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setWebScan(new SecHubWebScanConfiguration());

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNotError(result, CONTAINS_NO_SCAN_CONFIGURATION);
    }

    @Test
    void when_scan_type_secretscan_validation_fails_NOT_with_CONTAINS_NO_SCAN_CONFIGURATION() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setSecretScan(new SecHubSecretScanConfiguration());

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNotError(result, CONTAINS_NO_SCAN_CONFIGURATION);
    }

    @Test
    void when_scan_type_infrascan_validation_fails_NOT_with_CONTAINS_NO_SCAN_CONFIGURATION() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setInfraScan(new SecHubInfrastructureScanConfiguration());

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNotError(result, CONTAINS_NO_SCAN_CONFIGURATION);
    }

    @Test
    void when_modelcollector_collects_no_scan_types_the_validation_fails_with_nonunique_modulegroup_and_no_public_scantypes_found() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();

        /* check precondition */
        assertNull(ModuleGroup.resolveModuleGroupOrNull(modelSupportCollectedScanTypes));

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.NO_MODULE_GROUP_DETECTED);
        assertHasError(result, SecHubConfigurationModelValidationError.NO_PUBLIC_SCAN_TYPES_DETECTED);
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void when_modelcollector_collects_two_scan_types_which_are_not_in_same_group_the_validation_fails_with_modulegroup_unclear() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN);
        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* check precondition */
        assertNull(ModuleGroup.resolveModuleGroupOrNull(modelSupportCollectedScanTypes));

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.MULTIPLE_MODULE_GROUPS_DETECTED);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void when_modelcollector_collects_two_scan_types_which_are_in_same_group_the_validation_has_no_errors() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN);
        modelSupportCollectedScanTypes.add(ScanType.LICENSE_SCAN);

        /* check precondition */
        assertNotNull(ModuleGroup.resolveModuleGroupOrNull(modelSupportCollectedScanTypes));

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void a_configuration_which_has_code_scan_and_license_scan_will_NOT_fail_because_only_one_group() {
        /* prepare */
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();

        SecHubConfigurationModel model = new SecHubConfigurationModel();

        model.setApiVersion("1.0");
        model.setCodeScan(codeScan);
        model.setLicenseScan(licenseScan);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* setup data */
        SecHubSourceDataConfiguration dataConfiguration = new SecHubSourceDataConfiguration();
        dataConfiguration.setUniqueName("config-object-name");
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        data.getSources().add(dataConfiguration);

        model.setData(data);

        model.getCodeScan().get().getNamesOfUsedDataConfigurationObjects().add("config-object-name");
        model.getLicenseScan().get().getNamesOfUsedDataConfigurationObjects().add("config-object-name");

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void empty_webscanconfig_results_in_error() throws Exception {
        /* prepare */
        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_HAS_NO_URL_DEFINED);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void license_scan__empty_config_results_in_error() throws Exception {
        /* prepare */
        SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setLicenseScan(licenseScan);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.NO_DATA_CONFIG_SPECIFIED_FOR_SCAN);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void license_scan__config_with_data() throws Exception {
        /* prepare */
        String dataName = "data-reference-1";

        SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
        licenseScan.getNamesOfUsedDataConfigurationObjects().add(dataName);

        SecHubSourceDataConfiguration dataSource = new SecHubSourceDataConfiguration();
        dataSource.setUniqueName(dataName);

        SecHubDataConfiguration dataConfiguration = new SecHubDataConfiguration();
        dataConfiguration.getSources().add(dataSource);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setLicenseScan(licenseScan);
        model.setData(dataConfiguration);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void webscanconfig_with_https_uri_set_has_no_error() throws Exception {
        /* prepare */
        SecHubWebScanConfiguration webScan = mock(SecHubWebScanConfiguration.class);
        URI uri = createURIforSchema("https");
        when(webScan.getUrl()).thenReturn(uri);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertFalse(result.hasErrors());
    }

    @Test
    void webscanconfig_with_ftp_uri_set_has_error() throws Exception {
        /* prepare */
        SecHubWebScanConfiguration webScan = mock(SecHubWebScanConfiguration.class);
        URI uri = createURIforSchema("ftp");
        when(webScan.getUrl()).thenReturn(uri);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void empty_infrascanconfig_results_in_error() throws Exception {
        /* prepare */
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setInfraScan(infraScan);

        modelSupportCollectedScanTypes.add(ScanType.INFRA_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.INFRA_SCAN_HAS_NO_URIS_OR_IPS_DEFINED);
        assertEquals(1, result.getErrors().size());

    }

    @Test
    void infrascanconfig_with_one_uri_results_in_no_error() throws Exception {
        /* prepare */
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
        infraScan.getUris().add(createURIforSchema("https"));

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setInfraScan(infraScan);

        modelSupportCollectedScanTypes.add(ScanType.INFRA_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);

    }

    @Test
    void infrascanconfig_with_one_ip_results_in_no_error() throws Exception {
        /* prepare */
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
        infraScan.getIps().add(mock(InetAddress.class));

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setInfraScan(infraScan);

        modelSupportCollectedScanTypes.add(ScanType.INFRA_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertFalse(result.hasErrors());

    }

    @Test
    void null_model_results_in_one_error() {
        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(null);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.MODEL_NULL);
        assertEquals(1, result.getErrors().size());

    }

    @Test
    void api_version_set_but_no_scan_configuration_results_in_error() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.CONTAINS_NO_SCAN_CONFIGURATION);
        assertEquals(1, result.getErrors().size());

    }

    @Test
    void api_version_null_in_model_results_in_one_error() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setCodeScan(new SecHubCodeScanConfiguration());

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.API_VERSION_NULL);
        assertEquals(1, result.getErrors().size());

    }

    @Test
    void api_version_unsupported_results_in_one_error() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("0.1");

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.API_VERSION_NOT_SUPPORTED);
        assertEquals(1, result.getErrors().size());

    }

    @ParameterizedTest
    @CsvSource({ "n", "_", "-", "1", "name1", "referenced-name_", "the_name_with_slashes", VALID_NAME_WITH_MAX_LENGTH })
    void model_having_a_data_configuration_with_valid_name_has_no_error(String name) {
        /* prepare */
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName(name);
        data.getSources().add(config1);
        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @CsvSource({ "$", "n$me-", "a@e_", "t§st-1", "config-object-name$1", VALID_NAME_BUT_ONE_CHAR_TOO_LONG })
    void model_having_a_data_configuration_with_invalid_name_has_error(String name) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName(name);
        data.getSources().add(config1);
        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertTrue(result.hasErrors());
    }

    @Test
    void model_having_a_code_scan_configuration_which_references_a_wellknown_data_object_results_in_no_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("config-object-name");
        data.getSources().add(config1);
        model.setData(data);

        model.getCodeScan().get().getNamesOfUsedDataConfigurationObjects().add("config-object-name");

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void model_having_a_code_scan_configuration_which_references_an_unknown_data_object_results_in_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        model.getCodeScan().get().getNamesOfUsedDataConfigurationObjects().add("config-object-not-existing1");

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, "config-object-not-existing1", SecHubConfigurationModelValidationError.REFERENCED_DATA_CONFIG_OBJECT_NAME_NOT_EXISTING);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_a_web_scan_configuration_with_open_api_which_references_an_unknown_data_object_results_in_error() throws Exception {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        model.setWebScan(webScan);

        SecHubWebScanApiConfiguration openApi = new SecHubWebScanApiConfiguration();
        webScan.api = Optional.of(openApi);
        webScan.url = createURIforSchema("https");

        openApi.getNamesOfUsedDataConfigurationObjects().add("unknown-configuration");

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, "unknown-configuration", SecHubConfigurationModelValidationError.REFERENCED_DATA_CONFIG_OBJECT_NAME_NOT_EXISTING);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_a_web_scan_configuration_with_open_api_which_references_a_wellknown_data_object_results_in_no_error() throws Exception {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("referenced-open-api-file");
        data.getSources().add(config1);
        model.setData(data);

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        model.setWebScan(webScan);

        SecHubWebScanApiConfiguration openApi = new SecHubWebScanApiConfiguration();
        webScan.api = Optional.of(openApi);
        webScan.url = createURIforSchema("https");

        openApi.getNamesOfUsedDataConfigurationObjects().add("referenced-open-api-file");

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertFalse(result.hasErrors());
    }

    @Test
    void model_having_two_elements_in_source_data_section_with_same_name_results_in_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("same-name");

        SecHubSourceDataConfiguration config2 = new SecHubSourceDataConfiguration();
        config2.setUniqueName("same-name");

        data.getSources().add(config1);
        data.getSources().add(config2);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute + test */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_one_elements_with_name_with_length101_has_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("x".repeat(101));
        data.getSources().add(config1);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_LONG);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_one_elements_with_name_with_length0_has_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("");
        data.getSources().add(config1);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_SHORT);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_one_elements_with_name_null_has_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName(null);
        data.getSources().add(config1);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_IS_NULL);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_one_element_with_name_not_set_has_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        data.getSources().add(config1);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_IS_NULL);
        assertEquals(1, result.getErrors().size());

    }

    @Test
    void model_having_two_elements_in_binary_data_section_with_same_name_has_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubBinaryDataConfiguration config1 = new SecHubBinaryDataConfiguration();
        config1.setUniqueName("same-name");

        SecHubBinaryDataConfiguration config2 = new SecHubBinaryDataConfiguration();
        config2.setUniqueName("same-name");

        data.getBinaries().add(config1);
        data.getBinaries().add(config2);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */

        assertHasError(result, SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_having_two_elements_in_binary_and_source_data_section_with_same_name_has_error_containing_name_and_description() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("same-name1");

        SecHubBinaryDataConfiguration config2 = new SecHubBinaryDataConfiguration();
        config2.setUniqueName("same-name1");

        data.getSources().add(config1);
        data.getBinaries().add(config2);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertEquals(1, result.getErrors().size());

        assertHasError(result, "same-name1", SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE);
        assertHasError(result, "is not unique", SecHubConfigurationModelValidationError.DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE);

    }

    @Test
    void model_having_two_elements_in_binary_and_source_data_section_with_different_names_does_not_throw_exception()
            throws SecHubConfigurationModelValidationException {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("name1");

        SecHubBinaryDataConfiguration config2 = new SecHubBinaryDataConfiguration();
        config2.setUniqueName("name2");

        data.getSources().add(config1);
        data.getBinaries().add(config2);

        model.setData(data);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void secret_scan__empty_config_results_in_error() throws Exception {
        /* prepare */
        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setSecretScan(secretScan);

        modelSupportCollectedScanTypes.add(ScanType.SECRET_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.NO_DATA_CONFIG_SPECIFIED_FOR_SCAN);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void secret_scan__config_with_data() throws Exception {
        /* prepare */
        String dataName = "data-reference-1";

        SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
        secretScan.getNamesOfUsedDataConfigurationObjects().add(dataName);

        SecHubSourceDataConfiguration dataSource = new SecHubSourceDataConfiguration();
        dataSource.setUniqueName(dataName);

        SecHubDataConfiguration dataConfiguration = new SecHubDataConfiguration();
        dataConfiguration.getSources().add(dataSource);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setSecretScan(secretScan);
        model.setData(dataConfiguration);

        modelSupportCollectedScanTypes.add(ScanType.SECRET_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertFalse(result.hasErrors());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 20 })
    void model_with_given_amount_of__valid_metadata_labels_has_no_error(int amount) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        model.setMetaData(metaData);
        for (int i = 0; i < amount; i++) {
            metaData.getLabels().put("long-but-valid" + i, "valid value:" + i);
        }

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertFalse(result.hasErrors());
        assertEquals(0, result.getErrors().size());
    }

    @ParameterizedTest
    @ValueSource(ints = { 21, 100 })
    void model_with_given_amount_of__valid_metadata_labels_has_error(int amount) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        model.setMetaData(metaData);
        for (int i = 0; i < amount; i++) {
            metaData.getLabels().put("long-but-valid" + i, "valid value:" + i);
        }

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertTrue(result.hasErrors());
        assertHasError(result, SecHubConfigurationModelValidationError.METADATA_TOO_MANY_LABELS);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_with_metadata_label_key_length31_has_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put("123456789-123456789-123456789-1", "valid value");
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.METADATA_LABEL_KEY_TOO_LONG);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void model_with_metadata_label_key_length30_has_no_error() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put("123456789-123456789-123456789-", "valid value");
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 151, 250 })
    void model_with_metadata_label_value_length_has_error(int length) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put("valid-key", "a".repeat(length));
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.METADATA_LABEL_VALUE_TOO_LONG);
        assertEquals(1, result.getErrors().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "$variable1", "!something", "<html>", "label:with:colon", "Äpfel", "🦊" })
    void model_with_metadata_label_key_not_allowed_character_inside_has_error_code_scan(String key) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put(key, "valid value");
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.METADATA_LABEL_KEY_CONTAINS_ILLEGAL_CHARACTERS);
        assertEquals(1, result.getErrors().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "$variable1", "!something", "<html>", "label:with:colon" })
    void model_with_metadata_label_key_not_allowed_character_inside_has_error_license_scan(String key) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put(key, "valid value");
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.LICENSE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.METADATA_LABEL_KEY_CONTAINS_ILLEGAL_CHARACTERS);
        assertEquals(1, result.getErrors().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "variable1", "Variable", "UPPERCASED_ONLY", "something", "var-with-slash", "underscore_is_possible",
            "label.extra.with.dot.inside" })
    void model_with_metadata_label_key_allowed_character_inside_has_no_error_codeScan(String key) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put(key, "valid value");
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "variable1", "Variable", "UPPERCASED_ONLY", "something", "var-with-slash", "underscore_is_possible",
            "label.extra.with.dot.inside" })
    void model_with_metadata_label_key_allowed_character_inside_has_no_error_webScan(String key) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put(key, "valid value");
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 150 })
    void model_with_metadata_label_value_length_has_no_error(int length) {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        SecHubDataConfiguration data = new SecHubDataConfiguration();

        // define at least one data config (valid here)
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName("i-am-unique");
        data.getSources().add(config1);

        model.setData(data);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put("valid-key", "a".repeat(length));
        model.setMetaData(metaData);

        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // simulate correct module group found

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com", "https://example.com/admin", "https://example.com/<*>/profile", "https://example.com/blog/<*>",
            "https://example.com/<*>/profile/<*>/test" })
    void model_has_valid_urls_for_headers_specified_has_no_error(String onlyForUrl) {
        /* prepare */
        SecHubWebScanConfiguration webScan = createWebScanConfigurationWithHeader("https://example.com/", onlyForUrl);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com,https://example.com/admin",
            "https://example.com,https://example.com/admin,https://example.com/admin/search/<*>",
            "https://example.com/<*>/profile,https://example.com/blog/<*>,https://example.com/<*>/profile/<*>/test" })
    void model_has_valid_only_for_urls_and_multiple_headers_for_headers_specified_has_no_error(String onlyForUrls) {
        /* prepare */
        String[] splittedOnlyForUrls = onlyForUrls.split(",");
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndMultipleOnlyForUrl("Authorization", "secret-key",
                Arrays.asList(splittedOnlyForUrls));
        httpHeaders.addAll(createListWithOneHeaderAndMultipleOnlyForUrl("API-Key", "12345", Arrays.asList(splittedOnlyForUrls)));
        httpHeaders.addAll(createListWithOneHeaderAndMultipleOnlyForUrl("X-file-size", "4444", Arrays.asList(splittedOnlyForUrls)));

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com,https://example.com/admin",
            "https://example.com,https://example.com/admin,https://example.com/admin/search/<*>",
            "https://example.com/<*>/profile,https://example.com/blog/<*>,https://example.com/<*>/profile/<*>/test" })
    void model_has_valid_only_for_urls_and_one_header_for_headers_specified_has_no_error(String onlyForUrls) {
        /* prepare */
        String[] splittedOnlyForUrls = onlyForUrls.split(",");
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndMultipleOnlyForUrl("Authorization", "secret-key",
                Arrays.asList(splittedOnlyForUrls));

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com/admin", "https://example.com/<*>/profile", "https://example.com/blog/<*>" })
    void model_has_valid_urls_for_headers_specified_but_different_target_url_has_error(String onlyForUrl) {
        /* prepare */
        SecHubWebScanConfiguration webScan = createWebScanConfigurationWithHeader("https://otherwebapp.com/", onlyForUrl);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_DOES_NOT_CONTAIN_TARGET_URL);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com/{profile}", "https://example.com/blog/{}" })
    void model_has_invalid_url_for_headers_specified_has_error(String onlyForUrl) {
        /* prepare */
        SecHubWebScanConfiguration webScan = createWebScanConfigurationWithHeader("https://example.com", onlyForUrl);

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_IS_NOT_A_VALID_URL);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com/valid,https://example.com/blog/{invalid}",
            "https://example.com/blog/{invalid},https://example.com/valid,https://example.com/blog/another/valid" })
    void model_has_multiple_only_for_urls_with_at_least_one_invalid_for_headers_specified_has_error(String onlyForUrls) {
        /* prepare */
        String[] splittedOnlyForUrls = onlyForUrls.split(",");
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndMultipleOnlyForUrl("Authorization", "secret-key",
                Arrays.asList(splittedOnlyForUrls));

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_IS_NOT_A_VALID_URL);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com/valid,https://example.com/blog/{invalid}",
            "https://example.com/blog/{invalid},https://example.com/valid,https://example.com/blog/another/valid" })
    void model_has_multiple_only_for_urls_in_multiple_headers_with_at_least_one_invalid_for_headers_specified_has_error(String onlyForUrls) {
        /* prepare */
        String[] splittedOnlyForUrls = onlyForUrls.split(",");
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndMultipleOnlyForUrl("Authorization", "secret-key",
                Arrays.asList(splittedOnlyForUrls));
        httpHeaders.addAll(createListWithOneHeaderAndMultipleOnlyForUrl("API-Key", "12345", Arrays.asList(splittedOnlyForUrls)));
        httpHeaders.addAll(createListWithOneHeaderAndMultipleOnlyForUrl("X-file-size", "4444", Arrays.asList(splittedOnlyForUrls)));

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_IS_NOT_A_VALID_URL);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void model_has_no_header_names_specified_has_error(String missingHeaderName) {
        /* prepare */
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndOneOnlyForUrl(missingHeaderName, "secret-key", "https://example.com");

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_NO_HEADER_NAME_DEFINED);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void model_has_no_header_values_specified_has_error(String missingHeaderValue) {
        /* prepare */
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndOneOnlyForUrl("Authorization", missingHeaderValue, "https://example.com");

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_NO_HEADER_VALUE_DEFINED);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void model_has_header_value_only_from_file_ref_specified_has_no_error(String explicitHeaderValue) {
        /* prepare */
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndOneOnlyForUrl("Authorization", explicitHeaderValue, "https://example.com");
        // add header file ref
        httpHeaders.get(0).getNamesOfUsedDataConfigurationObjects().add("header-file-ref");

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void model_has_multiple_header_values_from_file_ref_and_direct_value_specified_has_error() {
        /* prepare */
        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndOneOnlyForUrl("Authorization", "test-value", "https://example.com");
        // add header file ref
        httpHeaders.get(0).getNamesOfUsedDataConfigurationObjects().add("header-file-ref");

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create("https://example.com");

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setWebScan(webScan);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_MULTIPLE_HEADER_VALUES_DEFINED);
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub_config_web_scan_no_intersection_of_urls_of_same_header.json" })
    void explicit_definitions_for_the_same_header_for_certain_urls_but_list_of_urls_have_no_intersections_has_no_errors(String testFilePath) {
        /* prepare */
        String json = TestFileReader.loadTextFile(testFilePath);
        SecHubScanConfiguration sechubConfiguration = SecHubScanConfiguration.createFromJSON(json);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub_config_web_scan_valid_headers_mixed_upper_and_lower_case.json",
            "src/test/resources/sechub_config_web_scan_not_duplicated_without_wildcard.json" })
    void explicit_definitions_for_the_same_header_for_certain_urls_but_list_of_urls_have_no_intersections_with_lower_and_upper_cases_has_no_errors(
            String testFilePath) {
        /* prepare */
        String json = TestFileReader.loadTextFile(testFilePath);
        SecHubScanConfiguration sechubConfiguration = SecHubScanConfiguration.createFromJSON(json);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub_config_web_scan_default_and_explicit_definitions_for_urls_for_header.json" })
    void default_for_a_header_with_explicit_definitions_for_the_same_header_for_certain_urls_has_no_errors(String testFilePath) {
        /* prepare */
        String json = TestFileReader.loadTextFile(testFilePath);
        SecHubScanConfiguration sechubConfiguration = SecHubScanConfiguration.createFromJSON(json);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub_config_web_scan_duplicated_default.json",
            "src/test/resources/sechub_config_web_scan_duplicated_default_with_upper_case.json" })
    void duplicated_default_for_the_same_header_has_error(String testFilePath) {
        /* prepare */
        String json = TestFileReader.loadTextFile(testFilePath);
        SecHubScanConfiguration sechubConfiguration = SecHubScanConfiguration.createFromJSON(json);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_NON_UNIQUE_HEADER_CONFIGURATION);
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub_config_web_scan_intersection_of_urls_of_same_header.json",
            "src/test/resources/sechub_config_web_scan_intersection_of_urls_of_same_header_missing_slash.json" })
    void explicit_definitions_for_the_same_header_for_certain_urls_but_list_of_urls_do_have_intersections_has_error(String testFilePath) {
        /* prepare */
        String json = TestFileReader.loadTextFile(testFilePath);
        SecHubScanConfiguration sechubConfiguration = SecHubScanConfiguration.createFromJSON(json);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_NON_UNIQUE_HEADER_CONFIGURATION);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "/", "<*>", "/<*>", "<*>/<*>", "/en/contacts", "/en/contacts/<*>", "<*>/en/contacts/<*>", "<*>/en/<*>/contacts/<*>",
            "<*>/en/<*>/<*>/contacts/<*>", "<*>/en<*><*>contacts/<*>", "en/contacts/<*>", "en/contacts", "en/contacts/" })
    void valid_include_and_exclude_has_no_errors(String includeExcludeEntry) {
        /* prepare */
        List<String> entryAsList = Arrays.asList(includeExcludeEntry);
        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();

        sechubConfiguration.getWebScan().get().excludes = Optional.of(entryAsList);
        sechubConfiguration.getWebScan().get().includes = Optional.of(entryAsList);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasNoErrors(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "//en/contacts", "/en//contacts", "/en/contacts//", "/en/ contacts/" })
    void double_slashes_include_exclude_has_errors(String includeExcludeEntry) {
        /* prepare */
        List<String> entryAsList = Arrays.asList(includeExcludeEntry);
        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();
        sechubConfiguration.getWebScan().get().excludes = Optional.of(entryAsList);
        sechubConfiguration.getWebScan().get().includes = Optional.of(entryAsList);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);
        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_EXCLUDE_INVALID);
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_INCLUDE_INVALID);
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", " /en/contacts", "/en/ contacts/", "/en/contacts " })
    void spaces_in_include_exclude_has_errors(String includeExcludeEntry) {
        /* prepare */
        List<String> entryAsList = Arrays.asList(includeExcludeEntry);
        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();
        sechubConfiguration.getWebScan().get().excludes = Optional.of(entryAsList);
        sechubConfiguration.getWebScan().get().includes = Optional.of(entryAsList);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);
        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_EXCLUDE_INVALID);
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_INCLUDE_INVALID);
    }

    @Test
    void too_many_excludes_results_in_error() {
        List<String> excludes = createListWithTooManyIncludesOrExcludes();

        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();
        sechubConfiguration.getWebScan().get().excludes = Optional.of(excludes);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_EXCLUDE_INVALID);
    }

    @Test
    void too_many_includes_results_in_error() {
        /* prepare */
        List<String> includes = createListWithTooManyIncludesOrExcludes();

        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();
        sechubConfiguration.getWebScan().get().includes = Optional.of(includes);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_INCLUDE_INVALID);
    }

    @Test
    void exclude_too_long_results_in_error() {
        /* prepare */
        List<String> excludes = createTooLongIncludeOrExcludeEntry();

        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();
        sechubConfiguration.getWebScan().get().excludes = Optional.of(excludes);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_EXCLUDE_INVALID);
    }

    @Test
    void include_too_long_results_in_error() {
        /* prepare */
        List<String> includes = createTooLongIncludeOrExcludeEntry();

        SecHubScanConfiguration sechubConfiguration = createSecHubConfigurationWithWebScanPart();
        sechubConfiguration.getWebScan().get().includes = Optional.of(includes);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.WEB_SCAN_INCLUDE_INVALID);
    }

    @Test
    void can_read_sechub_web_scan_config_with_wildcards() {
        /* prepare */
        String json = TestFileReader.loadTextFile("src/test/resources/sechub_config_web_scan_includes_excludes_with_wildcards.json");
        SecHubScanConfiguration sechubConfiguration = SecHubScanConfiguration.createFromJSON(json);

        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(sechubConfiguration);

        /* test */
        assertHasNoErrors(result);
    }

    @Test
    void when_sechub_config_too_large_validation_fails_with_SECHUB_CONFIGURATION_TOO_LARGE() {
        /* prepare */
        SecHubConfigurationModel model = createSecHubConfigModelWithExactly8193Characters();

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SECHUB_CONFIGURATION_TOO_LARGE);
    }

    @Test
    void when_sechub_config_has_exactly_maximum_size_allowed_error_SECHUB_CONFIGURATION_TOO_LARGE_does_not_occur() {
        /* prepare */
        SecHubConfigurationModel model = createSecHubConfigModelWithExactly8193Characters();
        // remove 1 characters so we are exactly at the limit of
        model.setApiVersion(model.getApiVersion().substring(1));

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNotError(result, SECHUB_CONFIGURATION_TOO_LARGE);
    }

    private SecHubConfigurationModel createSecHubConfigModelWithExactly8193Characters() {
        // 128*64 = 8192, so we take 127 because of the overhead of the JSON model:
        // {"apiVersion":""} = 17 characters so we need to add 48 characters afterwards
        String apiVersion = "abcdefghijklmnopqrstuvwxyz012345abcdefghijklmnopqrstuvwxyz012345".repeat(127);

        // add the remaining 48 characters to reach 8193
        apiVersion += "abcdefghijklmnopqrstuvwxyz012345abcdefghijklmnop";

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion(apiVersion);

        return model;
    }

    private SecHubScanConfiguration createSecHubConfigurationWithWebScanPart() {
        SecHubWebScanConfiguration webScanConfig = new SecHubWebScanConfiguration();
        webScanConfig.url = URI.create("https://www.gamechanger.example.org/");

        SecHubScanConfiguration sechubConfiguration = new SecHubScanConfiguration();
        sechubConfiguration.setApiVersion("1.0");
        sechubConfiguration.setWebScan(webScanConfig);
        return sechubConfiguration;
    }

    private List<String> createListWithTooManyIncludesOrExcludes() {
        List<String> list = new LinkedList<>();
        for (int i = 1; i <= 501; i++) {
            list.add("/myapp" + i);
        }
        return list;
    }

    private List<String> createTooLongIncludeOrExcludeEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append("/");

        for (int i = 0; i < 64; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz012345");
        }

        List<String> list = new LinkedList<>();
        list.add(sb.toString());
        return list;
    }

    private SecHubWebScanConfiguration createWebScanConfigurationWithHeader(String targetUrl, String onlyForUrl) {
        String headerName = "Authorization";
        String headerValue = "secret-key";

        List<HTTPHeaderConfiguration> httpHeaders = createListWithOneHeaderAndOneOnlyForUrl(headerName, headerValue, onlyForUrl);

        SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
        webScan.headers = Optional.ofNullable(httpHeaders);
        webScan.url = URI.create(targetUrl);

        return webScan;
    }

    private List<HTTPHeaderConfiguration> createListWithOneHeaderAndOneOnlyForUrl(String headerName, String headerValue, String onlyForUrl) {
        HTTPHeaderConfiguration httpHeader = new HTTPHeaderConfiguration();
        httpHeader.setName(headerName);
        httpHeader.setValue(headerValue);
        httpHeader.setOnlyForUrls(Optional.ofNullable(Arrays.asList(onlyForUrl)));
        List<HTTPHeaderConfiguration> httpHeaders = new ArrayList<>();
        httpHeaders.add(httpHeader);

        return httpHeaders;
    }

    private List<HTTPHeaderConfiguration> createListWithOneHeaderAndMultipleOnlyForUrl(String headerName, String headerValue, List<String> onlyForUrl) {
        HTTPHeaderConfiguration httpHeader = new HTTPHeaderConfiguration();
        httpHeader.setName(headerName);
        httpHeader.setValue(headerValue);
        httpHeader.setOnlyForUrls(Optional.ofNullable(onlyForUrl));
        List<HTTPHeaderConfiguration> httpHeaders = new ArrayList<>();
        httpHeaders.add(httpHeader);

        return httpHeaders;
    }

    private URI createURIforSchema(String schema) {
        // why mocking a URI? Because of name look ups and more
        // this slows tests - using a mock increases performance here
        URI uri = mock(URI.class);
        when(uri.getScheme()).thenReturn(schema);
        return uri;
    }

    private SecHubConfigurationModel createDefaultValidModel() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        model.setCodeScan(codeScan);
        return model;
    }

    private void assertHasNoErrors(SecHubConfigurationModelValidationResult result) {
        if (!result.hasErrors()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("The validation result contains errors:\n");
        for (SecHubConfigurationModelValidationErrorData errorData : result.getErrors()) {
            sb.append(errorData.toString());
            sb.append("\n");
        }
        fail(sb.toString());
    }

    private void assertHasNotError(SecHubConfigurationModelValidationResult result, SecHubConfigurationModelValidationError expectedToBeNotContained) {
        if (result.hasError(expectedToBeNotContained)) {
            fail("The result DOES contain error:" + expectedToBeNotContained);
        }
    }

    private void assertHasError(SecHubConfigurationModelValidationResult result, SecHubConfigurationModelValidationError error) {
        assertHasError(result, -1, null, error);
    }

    private void assertHasError(SecHubConfigurationModelValidationResult result, String messagePart, SecHubConfigurationModelValidationError error) {
        assertHasError(result, -1, messagePart, error);
    }

    private void assertHasError(SecHubConfigurationModelValidationResult result, int indexToCheck, String messagePart,
            SecHubConfigurationModelValidationError error) {
        if (result.hasError(error)) {

            if (indexToCheck < 0) {
                indexToCheck = 0;
            }

            if (messagePart == null) {
                return;
            }
            int errorSize = result.getErrors().size();
            if (errorSize <= indexToCheck) {
                fail("The result has no error at index: " + indexToCheck + ". Size is only: " + errorSize);
            }
            String message = result.getErrors().get(indexToCheck).getMessage();
            if (!message.contains(messagePart)) {
                fail("Message problem.\nError: " + error.name() + " was found, but \nthe message did not contain: '" + messagePart + "' but was:\n" + message);
            }

            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Result has not expected error:\n");
        sb.append("-");
        sb.append(error.name());
        sb.append("\n but:\n");
        for (SecHubConfigurationModelValidationErrorData data : result.getErrors()) {
            appendInfo(sb, data);
            sb.append("\n");
        }
        fail(sb.toString());
    }

    private void appendInfo(StringBuilder sb, SecHubConfigurationModelValidationErrorData data) {
        sb.append("-");
        sb.append(data.getError().name());
        sb.append(":");
        sb.append(data.getMessage());
    }

}
