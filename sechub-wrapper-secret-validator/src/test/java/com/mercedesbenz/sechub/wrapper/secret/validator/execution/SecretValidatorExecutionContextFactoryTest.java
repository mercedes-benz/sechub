// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorPDSJobResult;
import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorProperties;

class SecretValidatorExecutionContextFactoryTest {

    private SecretValidatorExecutionContextFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new SecretValidatorExecutionContextFactory();
    }

    @Test
    void not_existing_pds_job_result_file_throws_exception() {
        /* prepare */
        File notExisting = mock(File.class);
        when(notExisting.exists()).thenReturn(false);
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(notExisting);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("PDS job result file: " + notExisting + " does not exist!", exception.getMessage());
    }

    @Test
    void not_readable_pds_job_result_file_throws_exception() {
        /* prepare */
        File notReadable = mock(File.class);
        when(notReadable.exists()).thenReturn(true);
        when(notReadable.canRead()).thenReturn(false);
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(notReadable);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("PDS job result file: " + notReadable + " is not readable!", exception.getMessage());
    }

    @Test
    void invalid_sarif_pds_job_result_file_throws_exception() {
        /* prepare */
        File invalidFile = new File("src/test/resources/config-test-files/invalid-files/invalid-sarif.txt");
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(invalidFile);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Creating SARIF report model from: " + invalidFile + " failed!", exception.getMessage());
    }

    @Test
    void not_existing_secret_validator_config_file_throws_exception() {
        /* prepare */
        // we need a valid Sarif report because the pds job result is read first
        File validFile = new File("src/test/resources/config-test-files/valid-files/test-result.txt");
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(validFile);

        File notExisting = mock(File.class);
        when(notExisting.exists()).thenReturn(false);
        SecretValidatorProperties properties = new SecretValidatorProperties();
        properties.setConfigFile(notExisting);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;
        factoryToTest.properties = properties;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Secret validator configuration file: " + notExisting + " does not exist!", exception.getMessage());
    }

    @Test
    void not_readable_secret_validator_config_file_throws_exception() {
        /* prepare */
        // we need a valid Sarif report because the pds job result is read first
        File validFile = new File("src/test/resources/config-test-files/valid-files/test-result.txt");
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(validFile);

        File notReadable = mock(File.class);
        when(notReadable.exists()).thenReturn(true);
        when(notReadable.canRead()).thenReturn(false);
        SecretValidatorProperties properties = new SecretValidatorProperties();
        properties.setConfigFile(notReadable);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;
        factoryToTest.properties = properties;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Secret validator configuration file: " + notReadable + " is not readable!", exception.getMessage());
    }

    @Test
    void invalid_secret_validator_config_file_throws_exception() {
        /* prepare */
        // we need a valid Sarif report because the pds job result is read first
        File validFile = new File("src/test/resources/config-test-files/valid-files/test-result.txt");
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(validFile);

        File invalidFile = new File("src/test/resources/config-test-files/invalid-files/invalid-validator-config.txt");
        SecretValidatorProperties properties = new SecretValidatorProperties();
        properties.setConfigFile(invalidFile);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;
        factoryToTest.properties = properties;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Creating secret validator configuration from: " + invalidFile + " failed!", exception.getMessage());
    }

    @Test
    void valid_files_return_valid_execution_context() {
        /* prepare */
        // we need a valid Sarif report because the pds job result is read first
        File validSarifReportFile = new File("src/test/resources/config-test-files/valid-files/test-result.txt");
        SecretValidatorPDSJobResult secretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        secretValidatorPDSJobResult.setFile(validSarifReportFile);

        File validSecretValidatorCOnfigFile = new File("src/test/resources/config-test-files/valid-files/test-config.json");
        SecretValidatorProperties properties = new SecretValidatorProperties();
        properties.setConfigFile(validSecretValidatorCOnfigFile);

        factoryToTest.pdsResult = secretValidatorPDSJobResult;
        factoryToTest.properties = properties;

        /* execute */
        SecretValidatorExecutionContext secretValidatorExecutionContext = factoryToTest.create();

        assertNotNull(secretValidatorExecutionContext);
        assertNotNull(secretValidatorExecutionContext.getSarifReport());
        assertEquals(1, secretValidatorExecutionContext.getValidatorConfiguration().size());
    }

}
