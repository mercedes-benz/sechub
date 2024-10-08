// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Test;

class SecretValidatorPropertiesTest {

    @Test
    void validator_config_file_is_null_throws_exception() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new SecretValidatorProperties(null, 5, 4L));
    }

    @Test
    void not_existing_validator_config_file_throws_exception() {
        /* prepare */
        File notExisting = mock(File.class);
        when(notExisting.exists()).thenReturn(false);

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new SecretValidatorProperties(notExisting, 5, 4L));
    }

    @Test
    void not_readable_validator_config_file_throws_exception() {
        /* prepare */
        File notReadable = mock(File.class);
        when(notReadable.exists()).thenReturn(true);
        when(notReadable.canRead()).thenReturn(false);

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new SecretValidatorProperties(notReadable, 5, 4L));
    }

    @Test
    void valid_properties_result_in_valid_configuration() {
        /* prepare */
        File validConfigFile = new File("src/test/resources/config-test-files/valid-files/test-config.json");

        /* execute */
        SecretValidatorProperties properties = new SecretValidatorProperties(validConfigFile, 5, 4L);

        /* test */
        assertEquals(validConfigFile, properties.getConfigFile());
        assertEquals(5L, properties.getMaximumRetries());
        assertEquals(4, properties.getTimeoutSeconds());
    }

}
