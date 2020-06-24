package com.daimler.sechub.pds.job;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.pds.PDSNotAcceptableException;

public class PDSConfigurationValidatorTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();
   
    private PDSConfigurationValidator validatorToTest;

    @Before
    public void before() throws Exception {
        validatorToTest = new PDSConfigurationValidator();
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
        PDSConfiguration config = new PDSConfiguration();
        
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("sechub job UUID not set");
        
        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
    }
    
    @Test
    public void configuration_with_necessary_parts_set_throws_no_exception() {
        /* prepare*/
        PDSConfiguration config = new PDSConfiguration();
        config.setSechubJobUUID(UUID.randomUUID());
        
        /* execute */
        validatorToTest.assertPDSConfigurationValid(config);
        
        /* test */
        // just no exception
    }

}
