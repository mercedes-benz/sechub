package com.daimler.sechub.pds.job;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.PDSProductIdentifierValidator;

public class PDSJobConfigurationValidatorTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();
   
    private PDSJobConfigurationValidator validatorToTest;

    private PDSProductIdentifierValidator productIdentifierValidator;

    @Before
    public void before() throws Exception {
        
        productIdentifierValidator=mock(PDSProductIdentifierValidator.class);

        
        validatorToTest = new PDSJobConfigurationValidator();
        validatorToTest.productIdentifierValidator=productIdentifierValidator;
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
        /* prepare*/
        PDSJobConfiguration config = new PDSJobConfiguration();
        
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("sechub job UUID not set");
        
        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }
    
    @Test
    public void configuration_with_necessary_parts_set_throws_no_exception() {
        /* prepare*/
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setSechubJobUUID(UUID.randomUUID());
        
        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
        
        /* test */
        // just no exception
    }
    
    @Test
    public void configuration_with_necessary_parts_set_throws_no_exception_but_calls_productid_identifier() {
        /* prepare*/
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setSechubJobUUID(UUID.randomUUID());
        config.setProductId("productid1");
        
        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
        
        /* test */
        verify(productIdentifierValidator).createValidationErrorMessage("productid1");
    }
    
    @Test
    public void configuration_with_necessary_parts_but_invalid_productId_throws_exception() {
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("problem");
        
        /* prepare*/
        when(productIdentifierValidator.createValidationErrorMessage("productid1")).thenReturn("problem");

        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setSechubJobUUID(UUID.randomUUID());
        config.setProductId("productid1");
        
        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }

}
