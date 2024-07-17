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

    private SecretValidatorPDSJobResult invalidsecretValidatorPDSJobResult;
    private SecretValidatorPDSJobResult validSecretValidatorPDSJobResult;
    private SecretValidatorProperties invalidProperties;
    private SecretValidatorProperties validProperties;

    private static final File invalidSarifFile = new File("src/test/resources/config-test-files/invalid-files/invalid-sarif.txt");
    private static final File invalidConfigFile = new File("src/test/resources/config-test-files/invalid-files/invalid-validator-config.txt");
    private static final File validSarifFile = new File("src/test/resources/config-test-files/valid-files/test-result.txt");
    private static final File validConfigFile = new File("src/test/resources/config-test-files/valid-files/test-config.json");

    @BeforeEach
    void beforeEach() {
        factoryToTest = new SecretValidatorExecutionContextFactory();

        invalidsecretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        invalidsecretValidatorPDSJobResult.setFile(invalidSarifFile);

        invalidProperties = new SecretValidatorProperties();
        invalidProperties.setConfigFile(invalidConfigFile);

        validSecretValidatorPDSJobResult = new SecretValidatorPDSJobResult();
        validSecretValidatorPDSJobResult.setFile(validSarifFile);

        validProperties = new SecretValidatorProperties();
        validProperties.setConfigFile(validConfigFile);
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
        factoryToTest.pdsResult = invalidsecretValidatorPDSJobResult;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Creating SARIF report model from: " + invalidsecretValidatorPDSJobResult.getFile() + " failed!", exception.getMessage());
    }

    @Test
    void not_existing_secret_validator_config_file_throws_exception() {
        /* prepare */
        File notExisting = mock(File.class);
        when(notExisting.exists()).thenReturn(false);
        SecretValidatorProperties properties = new SecretValidatorProperties();
        properties.setConfigFile(notExisting);

        factoryToTest.pdsResult = validSecretValidatorPDSJobResult;
        factoryToTest.properties = properties;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Secret validator configuration file: " + notExisting + " does not exist!", exception.getMessage());
    }

    @Test
    void not_readable_secret_validator_config_file_throws_exception() {
        /* prepare */
        File notReadable = mock(File.class);
        when(notReadable.exists()).thenReturn(true);
        when(notReadable.canRead()).thenReturn(false);
        SecretValidatorProperties properties = new SecretValidatorProperties();
        properties.setConfigFile(notReadable);

        factoryToTest.pdsResult = validSecretValidatorPDSJobResult;
        factoryToTest.properties = properties;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Secret validator configuration file: " + notReadable + " is not readable!", exception.getMessage());
    }

    @Test
    void invalid_secret_validator_config_file_throws_exception() {
        /* prepare */
        factoryToTest.pdsResult = validSecretValidatorPDSJobResult;
        factoryToTest.properties = invalidProperties;

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create());
        assertEquals("Creating secret validator configuration from: " + invalidProperties.getConfigFile() + " failed!", exception.getMessage());
    }

    @Test
    void valid_files_return_valid_execution_context() {
        /* prepare */
        factoryToTest.pdsResult = validSecretValidatorPDSJobResult;
        factoryToTest.properties = validProperties;

        /* execute */
        SecretValidatorExecutionContext secretValidatorExecutionContext = factoryToTest.create();

        /* test */
        assertNotNull(secretValidatorExecutionContext);
        assertNotNull(secretValidatorExecutionContext.getSarifReport());
        assertEquals(1, secretValidatorExecutionContext.getValidatorConfiguration().size());
    }

}
