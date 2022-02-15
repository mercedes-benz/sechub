// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class DeleteProductExecutionProfileServiceTest {

    private DeleteProductExecutionProfileService serviceToTest;
    private ProductExecutionProfileRepository repository;

    @Before
    public void before() throws Exception {
        serviceToTest = new DeleteProductExecutionProfileService();

        serviceToTest.profileIdValidation = mock(ProductExecutionProfileIdValidation.class);
        ValidationResult validResult = new ValidationResult();
        when(serviceToTest.profileIdValidation.validate(any())).thenReturn(validResult);
        repository = mock(ProductExecutionProfileRepository.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);
        serviceToTest.repository = repository;
    }

    @Test
    public void existing_profile_can_be_deleted() {
        /* prepare */
        ProductExecutionProfile profile = new ProductExecutionProfile();
        profile.id = "profileid1";
        profile.description = "d1";
        profile.projectIds.add("project1");
        profile.enabled = false;
        Optional<ProductExecutionProfile> opt = Optional.of(profile);
        when(repository.findById("profileid1")).thenReturn(opt);

        /* execute */
        serviceToTest.deleteProductExecutionProfile(profile.id);

        /* test */
        verify(repository).deleteById(profile.id);
    }
    
    @Test(expected = NotFoundException.class)
    public void non_existing_profile_cannot_be_deleted_and_throws_not_found_exception() {
        /* prepare */
        when(repository.findById("profileid1")).thenReturn(Optional.empty());

        /* execute */
        serviceToTest.deleteProductExecutionProfile("not-existing");

        /* test*/
        // test done by epxected part
    }

}
