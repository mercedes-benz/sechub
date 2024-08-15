// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Test;

class SecretValidatorPDSJobResultTest {

    @Test
    void pds_job_result_file_is_null_throws_exception() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new SecretValidatorPDSJobResult(null));
    }

    @Test
    void not_existing_pds_job_result_file_throws_exception() {
        /* prepare */
        File notExisting = mock(File.class);
        when(notExisting.exists()).thenReturn(false);

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new SecretValidatorPDSJobResult(notExisting));
    }

    @Test
    void not_readable_pds_job_result_file_throws_exception() {
        /* prepare */
        File notReadable = mock(File.class);
        when(notReadable.exists()).thenReturn(true);
        when(notReadable.canRead()).thenReturn(false);

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new SecretValidatorPDSJobResult(notReadable));
    }

    @Test
    void valid_properties_result_in_valid_configuration() {
        /* prepare */
        File validConfigFile = new File("src/test/resources/config-test-files/valid-files/test-result.txt");

        /* execute */
        SecretValidatorPDSJobResult pdsJobResult = new SecretValidatorPDSJobResult(validConfigFile);

        /* test */
        assertEquals(validConfigFile, pdsJobResult.getFile());
    }

}
