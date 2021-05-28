// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.pds.execution.PDSExecutionParameterEntry;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

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
        addParameter(PDSJobConfigurationSupport.PARAM_KEY_SECHUB_STORAGE_PATH, "path/somewhere");
        
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
        addParameter(PDSJobConfigurationSupport.PARAM_KEY_USE_SECHUB_STORAGE, "true");
        
        /* execute */
        boolean result = supportToTest.isSecHubStorageEnabled();
        
        /* test */
        assertTrue(result);
    }


    private void addParameter(String key, String value) {
        PDSExecutionParameterEntry entry = new PDSExecutionParameterEntry();
        entry.setKey(key);
        entry.setValue(value);

        parameterList.add(entry);
    }

}
