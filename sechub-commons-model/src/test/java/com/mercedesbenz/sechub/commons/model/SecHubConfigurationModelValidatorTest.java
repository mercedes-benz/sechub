// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult.SecHubConfigurationModelValidationErrorData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator.SecHubConfigurationModelValidationException;

class SecHubConfigurationModelValidatorTest {

    private static final String VALID_NAME_WITH_MAX_LENGTH = "---------1---------2---------3---------4---------5---------6---------7---------8";
    private static final String VALID_NAME_BUT_ONE_CHAR_TOO_LONG = VALID_NAME_WITH_MAX_LENGTH + "-";
    private SecHubConfigurationModelValidator validatorToTest;
    private SecHubConfigurationModelSupport modelSupport;
    private Set<ScanType> modelSupportCollectedScanTypes;

    @BeforeEach
    private void beforeEach() {

        modelSupportCollectedScanTypes = new LinkedHashSet<>();
        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN); // just one

        modelSupport = mock(SecHubConfigurationModelSupport.class);

        validatorToTest = new SecHubConfigurationModelValidator();
        validatorToTest.modelSupport = modelSupport;

        when(modelSupport.collectPublicScanTypes(any(SecHubConfigurationModel.class))).thenReturn(modelSupportCollectedScanTypes);
    }

    @Test
    void when_modelcollector_collects_no_scan_types_the_validation_fails_with_modulegroup_unclear_and_no_public_scantypes_found() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        modelSupportCollectedScanTypes.clear();

        /* check precondition */
        assertNull(ModuleGroup.resolveModuleGroupOrNull(modelSupportCollectedScanTypes));

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.MODULE_GROUP_UNCLEAR);
        assertHasError(result, SecHubConfigurationModelValidationError.NO_PUBLIC_SCANTYPES_DETECTED);
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void when_modelcollector_collects_two_scan_types_which_are_not_in_same_group_the_validation_fails_with_modulegroup_unclear() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        modelSupportCollectedScanTypes.clear();
        modelSupportCollectedScanTypes.add(ScanType.CODE_SCAN);
        modelSupportCollectedScanTypes.add(ScanType.WEB_SCAN);

        /* check precondition */
        assertNull(ModuleGroup.resolveModuleGroupOrNull(modelSupportCollectedScanTypes));

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.MODULE_GROUP_UNCLEAR);
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void when_modelcollector_collects_two_scan_types_which_are_in_same_group_the_validation_has_no_errors() {
        /* prepare */
        SecHubConfigurationModel model = createDefaultValidModel();
        modelSupportCollectedScanTypes.clear();
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

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertFalse(result.hasErrors());

    }

    @Test
    void null_model_results_in_one_error() {
        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(null);

        /* test */
        assertHasError(result, SecHubConfigurationModelValidationError.MODEL_NULL);
        assertEquals(1, result.getErrors().size());

    }

    @Test
    void api_version_set_but_no_scan_configuration_results_in_error() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");

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

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            sb.append("x");
        }
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();
        config1.setUniqueName(sb.toString());
        data.getSources().add(config1);

        model.setData(data);

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

        /* execute */
        SecHubConfigurationModelValidationResult result = validatorToTest.validate(model);

        /* test */
        assertHasNoErrors(result);
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
        assertFalse(result.hasErrors());
        assertEquals(0, result.getErrors().size());
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
