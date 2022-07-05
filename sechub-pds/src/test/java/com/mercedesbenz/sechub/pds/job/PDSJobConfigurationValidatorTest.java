// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.config.PDSProductIdentifierValidator;
import com.mercedesbenz.sechub.pds.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class PDSJobConfigurationValidatorTest {
    private static final String CONFIGURED_SERVER_PRODUCT_ID = "productid1";

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private PDSJobConfigurationValidator validatorToTest;

    private PDSProductIdentifierValidator productIdentifierValidator;

    private PDSServerConfigurationService serverConfigurationService;

    private PDSProductSetup setup1;

    @Before
    public void before() throws Exception {

        productIdentifierValidator = mock(PDSProductIdentifierValidator.class);
        serverConfigurationService = mock(PDSServerConfigurationService.class);

        setup1 = new PDSProductSetup();
        when(serverConfigurationService.getProductSetupOrNull(CONFIGURED_SERVER_PRODUCT_ID)).thenReturn(setup1);

        validatorToTest = new PDSJobConfigurationValidator();
        validatorToTest.productIdentifierValidator = productIdentifierValidator;
        validatorToTest.serverConfigurationService = serverConfigurationService;
    }

    @Test
    public void when_a_server_configuration_does_contain_optional_parameters_and_jobconfig_not_than_no_exception_is_thrown() {
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
    public void when_a_server_configuration_does_contain_mandatory_parameters_and_jobconfig_not_than_an_exception_is_thrown() {
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("mandatory parameter not found");
        expected.expectMessage("the.necessary.key");

        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        PDSProductParameterDefinition mandatoryParameter = new PDSProductParameterDefinition();
        mandatoryParameter.setKey("the.necessary.key");
        setup1.getParameters().getMandatory().add(mandatoryParameter);

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }

    @Test
    public void when_a_server_configuration_does_not_contain_product_id_an_exception_is_thrown() {
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("does not support product identifier");

        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();
        config.setProductId("productid-notknown-by-server");

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }

    @Test
    public void null_configuration_throws_not_acceptable_with_message() {
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("may not be null");

        /* execute */
        validatorToTest.assertPDSConfigurationValid(null);
    }

    @Test
    public void configuration_without_sechub_job_UUID_throws_not_acceptable_with_message() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();
        config.setSechubJobUUID(null);

        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("sechub job UUID not set");

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }

    @Test
    public void configuration_with_necessary_parts_set_throws_no_exception() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);

        /* test */
        // just no exception
    }

    @Test
    public void configuration_with_necessary_parts_set_throws_no_exception_but_calls_productid_identifier() {
        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);

        /* test */
        verify(productIdentifierValidator).createValidationErrorMessage(CONFIGURED_SERVER_PRODUCT_ID);
    }

    @Test
    public void configuration_with_necessary_parts_but_productIdValidator_validates_as_invalid_productId_throws_exception() {
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("problem");

        /* prepare */
        PDSJobConfiguration config = prepareValidConfig();
        when(productIdentifierValidator.createValidationErrorMessage(CONFIGURED_SERVER_PRODUCT_ID)).thenReturn("problem");

        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }

    private PDSJobConfiguration prepareValidConfig() {
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setSechubJobUUID(UUID.randomUUID());
        config.setProductId(CONFIGURED_SERVER_PRODUCT_ID);

        return config;
    }

}
