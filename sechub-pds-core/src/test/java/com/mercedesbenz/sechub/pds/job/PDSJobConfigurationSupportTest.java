// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;

class PDSJobConfigurationSupportTest {

    private static final String FOUND_KEY = "test.key1";
    private static final String UNKNOWN_KEY = "test.unknown_key1";

    private PDSJobConfiguration config;
    private PDSJobConfigurationSupport supportToTest;
    private List<PDSExecutionParameterEntry> parameterList;

    @BeforeEach
    void beforeEach() {
        config = mock(PDSJobConfiguration.class);
        parameterList = new ArrayList<>();
        when(config.getParameters()).thenReturn(parameterList);

        supportToTest = new PDSJobConfigurationSupport(config);
    }

    @Test
    void get_string_parameter_key_found_value_not_null() {

        /* prepare */
        addParameter(FOUND_KEY, "value");

        /* execute */
        String result = supportToTest.getStringParameterOrNull(FOUND_KEY);

        /* test */
        assertEquals("value", result);
    }

    @Test
    void get_sechub_storage_path_uses_parameter_key() {

        /* prepare */
        addParameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH, "path/somewhere");

        /* execute */
        String result = supportToTest.getSecHubStoragePath();

        /* test */
        assertEquals("path/somewhere", result);
    }

    @Test
    void get_string_parameter_key_not_found_value_null_reason_not_same_key() {

        /* prepare */
        addParameter(FOUND_KEY, "false");

        /* execute */
        String result = supportToTest.getStringParameterOrNull(UNKNOWN_KEY);

        /* test */
        assertNull(result);
    }

    @Test
    void get_string_parameter_key_not_found_value_null_reason_nothing_else() {

        /* execute */
        String result = supportToTest.getStringParameterOrNull(UNKNOWN_KEY);

        /* test */
        assertNull(result);
    }

    @Test
    void is_enabled_parameter_key_not_found_is_false() {

        /* execute */
        boolean result = supportToTest.isEnabled(UNKNOWN_KEY);

        /* test */
        assertFalse(result);
    }

    @Test
    void is_enabled_parameter_key_found_but_false_returns__false() {

        /* prepare */
        addParameter(FOUND_KEY, "false");

        /* execute */
        boolean result = supportToTest.isEnabled(FOUND_KEY);

        /* test */
        assertFalse(result);
    }

    @Test
    void is_enabled_parameter_key_found_but_xyz_returns__false() {

        /* prepare */
        addParameter(FOUND_KEY, "xyz");

        /* execute */
        boolean result = supportToTest.isEnabled(FOUND_KEY);

        /* test */
        assertFalse(result);
    }

    @Test
    void is_enabled_parameter_key_found_and_true_returns__true() {

        /* prepare */
        addParameter(FOUND_KEY, "true");

        /* execute */
        boolean result = supportToTest.isEnabled(FOUND_KEY);

        /* test */
        assertTrue(result);
    }

    @Test
    void is_sechub_storage_enabled_uses_parameter_key() {

        /* prepare */
        addParameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "true");

        /* execute */
        boolean result = supportToTest.isSecHubStorageEnabled();

        /* test */
        assertTrue(result);
    }

    @Test
    void resolve_sechub_model_returns_null_when_parameter_not_set() {
        assertNull(supportToTest.resolveSecHubConfigurationModel());
    }

    @Test
    void get_sechub_model_json_returns_null_when_parameter_not_set() {
        assertNull(supportToTest.getSecHubConfigurationModelAsJson());
    }

    @Test
    void resolve_sechub_model_returns_model_when_parameter_defined() {
        /* prepare */
        SecHubScanConfiguration config = new SecHubScanConfiguration();
        config.setProjectId("a-cool-project-id");
        String json = config.toJSON();
        addParameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, json);

        /* execute */
        SecHubConfigurationModel model = supportToTest.resolveSecHubConfigurationModel();

        /* test */
        assertNotNull(model);
        assertEquals("a-cool-project-id", model.getProjectId());
    }

    @Test
    void resolve_sechub_model_fails_with_json_converter_exception_when_parameter_is_invalid_json() {
        /* prepare */
        addParameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, "{");

        /* execute + test */
        assertThrows(JSONConverterException.class, () -> supportToTest.resolveSecHubConfigurationModel());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void addParameter(String key, String value) {
        PDSExecutionParameterEntry entry = new PDSExecutionParameterEntry();
        entry.setKey(key);
        entry.setValue(value);

        parameterList.add(entry);
    }

}
