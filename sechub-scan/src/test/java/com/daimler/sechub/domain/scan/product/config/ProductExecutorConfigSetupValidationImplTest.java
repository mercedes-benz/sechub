// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class ProductExecutorConfigSetupValidationImplTest {

    private ProductExecutorConfigSetupValidationImpl validationToTest;
    private ProductExecutorConfigSetup setup;
    private List<ProductExecutorConfigSetupJobParameter> parameters;
    private ProductExecutorConfigSetupCredentials credentials;

    @Before
    public void before() throws Exception {
        validationToTest = new ProductExecutorConfigSetupValidationImpl();
        setup = mock(ProductExecutorConfigSetup.class);
    }

    @Test
    public void when_all_parts_correct_set_no_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        when(credentials.getUser()).thenReturn("fakeuser");
        when(credentials.getPassword()).thenReturn("fakepwd");
        
        /* execute */
        ValidationResult result = validationToTest.validate(setup);

        /* test */
        assertTrue(result.isValid());
    }
    
    @Test
    public void when_all_parts_correct_set_but_credential_pwd_and_user_are_null_no_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        when(credentials.getUser()).thenReturn(null);
        when(credentials.getPassword()).thenReturn(null);

        /* execute */
        ValidationResult result = validationToTest.validate(setup);

        /* test */
        assertTrue(result.isValid());
    }
    
    @Test
    public void when_all_parts_correct_set_except_missing_baseurl_one_validation_problems() {
        /* prepare */
        mockConfigWithValidValues();
        when(setup.getBaseURL()).thenReturn(null);

        /* execute */
        ValidationResult result = validationToTest.validate(setup);

        /* test */
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
    }

    private void mockConfigWithValidValues() {
        when(setup.getBaseURL()).thenReturn("https://productsetup.baseurl.example.com");
        credentials = mock(ProductExecutorConfigSetupCredentials.class);
        when(setup.getCredentials()).thenReturn(credentials);
        parameters = new ArrayList<>();
        when(setup.getJobParameters()).thenReturn(parameters);
    }

}
