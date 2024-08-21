// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        invalidsecretValidatorPDSJobResult = new SecretValidatorPDSJobResult(invalidSarifFile);

        invalidProperties = new SecretValidatorProperties(invalidConfigFile, 5L);

        validSecretValidatorPDSJobResult = new SecretValidatorPDSJobResult(validSarifFile);

        validProperties = new SecretValidatorProperties(validConfigFile, 5L);
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
