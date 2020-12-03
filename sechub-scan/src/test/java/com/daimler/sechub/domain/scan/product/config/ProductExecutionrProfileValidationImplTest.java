// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.validation.ProfileDescriptionValidation;
import com.daimler.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class ProductExecutionrProfileValidationImplTest {

    private ProductExecutionrProfileValidationImpl validationToTest;

    @Before
    public void before() throws Exception {
        validationToTest = new ProductExecutionrProfileValidationImpl();
        validationToTest.profileIdValidation=mock(ProductExecutionProfileIdValidation.class);
        validationToTest.descriptionValidation=mock(ProfileDescriptionValidation.class);
        validationToTest.projectIdValidation=mock(ProjectIdValidation.class);
        
        when(validationToTest.profileIdValidation.validate(any())).thenReturn(oneError("profile"));
        when(validationToTest.descriptionValidation.validate(any())).thenReturn(oneError("description"));
        when(validationToTest.projectIdValidation.validate(any())).thenReturn(oneError("project"));
    }
    
    private ValidationResult oneError(String message) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(message);
        return validationResult;
    }

    @Test
    public void null_profile_is_invalid() {
        assertFalse(validationToTest.validate((ProductExecutionProfile)null).isValid());
        
    }
    @Test
    public void calls_subvalidators_with_values_and_missign_uuid_in_config_is_also_recognized_so_5_errors() {
        /* prepare */
        ProductExecutionProfile profile = new ProductExecutionProfile();
        profile.id="pid";
        profile.description="desc";
        
        ProductExecutorConfig configuration = mock(ProductExecutorConfig.class);
        profile.configurations.add(configuration);
        
        profile.projectIds.add("1234");
        profile.projectIds.add("54333");
        
        /* execute */
        ValidationResult result = validationToTest.validate(profile);
        
        /* test */
        verify(validationToTest.profileIdValidation,times(1)).validate("pid");
        verify(validationToTest.descriptionValidation,times(1)).validate("desc");
        verify(validationToTest.projectIdValidation).validate("1234");
        verify(validationToTest.projectIdValidation).validate("54333");
        
        /* fith error si because of missing uuid */
        if (result.getErrors().size()!=5) {
            fail("Expected 5 error, but got:"+result.getErrors());
        }
        
    }
    @Test
    public void calls_subvalidators_with_values_when_uuid_is_set_we_got_only_4_errors() {
        /* prepare */
        ProductExecutionProfile profile = new ProductExecutionProfile();
        profile.id="pid";
        profile.description="desc";
        
        ProductExecutorConfig configuration = mock(ProductExecutorConfig.class);
        when(configuration.getUUID()).thenReturn(UUID.randomUUID());
        profile.configurations.add(configuration);
        
        profile.projectIds.add("1234");
        profile.projectIds.add("54333");
        
        /* execute */
        ValidationResult result = validationToTest.validate(profile);
        
        /* test */
        verify(validationToTest.profileIdValidation,times(1)).validate("pid");
        verify(validationToTest.descriptionValidation,times(1)).validate("desc");
        verify(validationToTest.projectIdValidation).validate("1234");
        verify(validationToTest.projectIdValidation).validate("54333");
        
        /* fith error si because of missing uuid */
        if (result.getErrors().size()!=4) {
            fail("Expected 4 error, but got:"+result.getErrors());
        }
        
    }

}
