// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSProductIdentifierValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;

public class PDSJobConfigurationValidatorTest {
    private static final String CONFIGURED_SERVER_PRODUCT_ID = "productid1";

    private PDSJobConfigurationValidator validatorToTest;

    private PDSProductIdentifierValidator productIdentifierValidator;

    private PDSServerConfigurationService serverConfigurationService;

    private PDSProductSetup setup1;

    @BeforeEach
    void before() throws Exception {

        productIdentifierValidator = mock(PDSProductIdentifierValidator.class);
        serverConfigurationService = mock(PDSServerConfigurationService.class);

        setup1 = new PDSProductSetup();
        when(serverConfigurationService.getProductSetupOrNull(CONFIGURED_SERVER_PRODUCT_ID)).thenReturn(setup1);

        validatorToTest = new PDSJobConfigurationValidator();
        validatorToTest.productIdentifierValidator = productIdentifierValidator;
        validatorToTest.serverConfigurationService = serverConfigurationService;
    }

    @Test
    void when_a_server_configuration_does_contain_optional_parameters_and_jobconfig_not_than_no_exception_is_thrown() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        PDSProductParameterDefinition optional = new PDSProductParameterDefinition();
        optional.setKey("the.optional.key");
        setup1.getParameters().getOptional().add(optional);

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);

        /* test */
        // just no exception
    }

    @Test
    void when_a_server_configuration_does_contain_mandatory_parameters_and_jobconfig_not_than_an_exception_is_thrown() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        PDSProductParameterDefinition mandatoryParameter = new PDSProductParameterDefinition();
        mandatoryParameter.setKey("the.necessary.key");
        setup1.getParameters().getMandatory().add(mandatoryParameter);

        /* execute + test */
        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> validatorToTest.assertPDSConfigurationValid(config));
        String message = exception.getMessage();
        assertTrue(message.contains("mandatory parameter not found"));
        assertTrue(message.contains("the.necessary.key"));

    }

    @Test
    void when_a_server_configuration_does_contain_mandatory_param_with_default__and_jobconfig_not_than_still_valid() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        PDSProductParameterDefinition mandatoryParameter = new PDSProductParameterDefinition();
        mandatoryParameter.setKey("the.necessary.key");
        mandatoryParameter.setDefault("default-value");
        setup1.getParameters().getMandatory().add(mandatoryParameter);

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }

    @Test
    void when_a_server_configuration_does_not_contain_product_id_an_exception_is_thrown() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();
        config.setProductId("productid-notknown-by-server");

        /* execute + test */
        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> validatorToTest.assertPDSConfigurationValid(config));
        String message = exception.getMessage();
        assertTrue(message.contains("does not support product identifier"));
    }

    @Test
    void null_configuration_throws_not_acceptable_with_message() {
        /* prepare */
        PDSJobConfiguration config = null;

        /* execute + test */
        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> validatorToTest.assertPDSConfigurationValid(config));
        String message = exception.getMessage();
        assertTrue(message.contains("may not be nul"));
    }

    @Test
    void configuration_without_sechub_job_UUID_throws_not_acceptable_with_message() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();
        config.setSechubJobUUID(null);

        /* execute + test */
        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> validatorToTest.assertPDSConfigurationValid(config));
        String message = exception.getMessage();
        assertTrue(message.contains("sechub job UUID not set"));
    }

    @Test
    void configuration_with_necessary_parts_set_throws_no_exception() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);

        /* test */
        // just no exception
    }

    @Test
    void configuration_with_necessary_parts_set_throws_no_exception_but_calls_productid_identifier() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);

        /* test */
        verify(productIdentifierValidator).createValidationErrorMessage(CONFIGURED_SERVER_PRODUCT_ID);
    }

    @Test
    void configuration_with_necessary_parts_but_productIdValidator_validates_as_invalid_productId_throws_exception() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();
        when(productIdentifierValidator.createValidationErrorMessage(CONFIGURED_SERVER_PRODUCT_ID)).thenReturn("problem");

        /* execute + test */
        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> validatorToTest.assertPDSConfigurationValid(config));
        String message = exception.getMessage();
        assertTrue(message.contains("problem"));
    }

    private PDSJobConfiguration prepareValidConfig() {
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setSechubJobUUID(UUID.randomUUID());
        config.setProductId(CONFIGURED_SERVER_PRODUCT_ID);

        return config;
    }

}
