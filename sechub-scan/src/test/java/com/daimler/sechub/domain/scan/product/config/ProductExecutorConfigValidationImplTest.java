// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class ProductExecutorConfigValidationImplTest {

    private ProductExecutorConfigValidationImpl validationToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetupValidation setupValidation;

    @Before
    public void before() throws Exception {
        setupValidation = mock(ProductExecutorConfigSetupValidation.class);
        config = mock(ProductExecutorConfig.class);

        validationToTest = new ProductExecutorConfigValidationImpl();
        validationToTest.setupValidation = setupValidation;
    }

    @Test
    public void when_all_parts_correct_set_no_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();

        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertTrue(result.isValid());
    }

    @Test
    public void when_all_parts_correct_set_but_setup_validationresult_not_valid_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError("ups!");
        when(setupValidation.validate(any())).thenReturn(validationResult);

        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
    }
    
    @Test
    public void when_all_parts_correct_set_but_name_length_is_21_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        String name = "name567890123456789012345678901";
        assertEquals(31,name.length());
        when(config.getName()).thenReturn(name);
        
        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
    }
    
    @Test
    public void when_all_parts_correct_set_but_name_length_is_2_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        String name = "12";
        assertEquals(2,name.length());
        when(config.getName()).thenReturn(name);
        
        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
    }
    
    @Test
    public void when_all_parts_correct_set_setup_is_null_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        when(config.getSetup()).thenReturn(null);
        
        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
    }
    
    @Test
    public void when_all_parts_correct_set_but_productIdentifier_is_null_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        when(config.getProductIdentifier()).thenReturn(null);
        
        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
    }
    
    @Test
    public void when_all_parts_correct_set_but_executorVersion_is_null_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        when(config.getExecutorVersion()).thenReturn(null);
        
        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
    }
    
    @Test
    public void when_all_parts_correct_set_but_name_length_is_3_no_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        String name = "123";
        when(config.getName()).thenReturn(name);
        
        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertTrue(result.isValid());
    }
    
    

    private void mockConfigWithValidValues() {
        when(config.getExecutorVersion()).thenReturn(Integer.valueOf(1));
        String name = "name5678901234567-901234567_90";
        assertEquals(30,name.length());
        when(config.getName()).thenReturn(name);
        when(config.getProductIdentifier()).thenReturn(ProductIdentifier.PDS_CODESCAN);
        when(config.getUUID()).thenReturn(UUID.randomUUID());
        when(config.getSetup()).thenReturn(new ProductExecutorConfigSetup());
    }

}
