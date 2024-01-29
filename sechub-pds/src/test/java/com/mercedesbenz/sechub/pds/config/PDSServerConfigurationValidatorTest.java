// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;

public class PDSServerConfigurationValidatorTest {

    private PDSServerConfigurationValidator validatorToTest;
    private PDSServerConfiguration configuration;
    private PDSProductIdentifierValidator productIdValidator;
    private PDSPathExecutableValidator pathExecutableValidator;
    private PDSServerIdentifierValidator serverIdValidator;

    @Before
    public void before() throws Exception {
        productIdValidator = mock(PDSProductIdentifierValidator.class);
        serverIdValidator = mock(PDSServerIdentifierValidator.class);
        pathExecutableValidator = mock(PDSPathExecutableValidator.class);

        configuration = new PDSServerConfiguration();

        validatorToTest = new PDSServerConfigurationValidator();
        validatorToTest.productIdValidator = productIdValidator;
        validatorToTest.serverIdValidator = serverIdValidator;
        validatorToTest.pathExecutableValidator = pathExecutableValidator;
    }

    @Test
    public void nullConfigurationIsNotValid() {
        assertNotValid(validatorToTest.createValidationErrorMessage(null));
    }

    @Test
    public void anEmptyConfigurationIsNotValid() {
        assertNotValid(validatorToTest.createValidationErrorMessage(configuration));
    }

    @Test
    public void validConfigurationIsValid() {
        /* prepare */
        prepareValidConfiguration();

        /* execute + test */
        assertValid(validatorToTest.createValidationErrorMessage(configuration));
    }

    @Test
    public void whenProduct1HasNoValidIdSet_itsNotValid() {
        /* prepare */
        prepareValidConfiguration();
        when(productIdValidator.createValidationErrorMessage("productid1")).thenReturn("invalid-reason");

        /* execute + test */
        assertNotValid(validatorToTest.createValidationErrorMessage(configuration));
    }

    @Test
    public void whenProduct1_has_invalid_path_itsNotValid() {
        /* prepare */
        prepareValidConfiguration();
        when(pathExecutableValidator.createValidationErrorMessage("path1")).thenReturn("invalid-reason");

        /* execute + test */
        assertNotValid(validatorToTest.createValidationErrorMessage(configuration));
    }

    @Test
    public void whenProduct2_has_invalid_path_itsNotValid() {
        /* prepare */
        prepareValidConfiguration();
        when(pathExecutableValidator.createValidationErrorMessage("path2")).thenReturn("invalid-reason");

        /* execute + test */
        assertNotValid(validatorToTest.createValidationErrorMessage(configuration));
    }

    @Test
    public void when_serverId_is_not_valid__its_not_valid() {
        /* prepare */
        prepareValidConfiguration();
        // server id is also validated by product id validator
        when(serverIdValidator.createValidationErrorMessage("server-id")).thenReturn("invalid-reason");

        /* execute + test */
        assertNotValid(validatorToTest.createValidationErrorMessage(configuration));
    }

    private void prepareValidConfiguration() {
        PDSProductSetup setup1 = new PDSProductSetup();
        setup1.setScanType(ScanType.CODE_SCAN);
        setup1.setId("productid1");
        setup1.setPath("path1");

        PDSProductSetup setup2 = new PDSProductSetup();
        setup2.setScanType(ScanType.INFRA_SCAN);
        setup2.setId("productid2");
        setup2.setPath("path2");

        configuration.setApiVersion("1.0");
        configuration.getProducts().add(setup1);
        configuration.getProducts().add(setup2);

        configuration.setServerId("server-id");

    }

    private void assertValid(String message) {
        /* validation message being null means its valid */
        assertNull(message);
    }

    private void assertNotValid(String message) {
        /* validation message not being null means its not valid */
        assertNotNull(message);
    }

}
