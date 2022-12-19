// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

public class DeleteProductExecutionProfileServiceTest {

    private DeleteProductExecutionProfileService serviceToTest;
    private ProductExecutionProfileRepository repository;

    @BeforeEach
    void beforeEach() throws Exception {
        repository = mock(ProductExecutionProfileRepository.class);
        serviceToTest = new DeleteProductExecutionProfileService();

        serviceToTest.auditLogService = mock(AuditLogService.class);
        serviceToTest.repository = repository;
        serviceToTest.profileIdValidation = mock(ProductExecutionProfileIdValidation.class);

        when(serviceToTest.profileIdValidation.validate(any())).thenReturn(new ValidationResult());
    }

    @Test
    void existing_profile_can_be_deleted() {
        /* prepare */
        String profileId = "profileid1";

        when(repository.findById(profileId)).thenReturn(Optional.of(mock(ProductExecutionProfile.class)));

        /* execute */
        serviceToTest.deleteProductExecutionProfile(profileId);

        /* test */
        verify(repository).deleteById(profileId); // check delete method was called
    }

    @Test
    void non_existing_profile_cannot_be_deleted_and_throws_not_found_exception() {
        /* prepare */
        String profileId = "not-existing";

        when(repository.findById(any())).thenReturn(Optional.empty());

        /* execute + test exception */
        assertThrows(NotFoundException.class, () -> {
            serviceToTest.deleteProductExecutionProfile(profileId);
        });

        /* test */
        verify(repository).findById(profileId); // check findById was used
        verify(repository, never()).deleteById(any()); // check delete method was never called
    }

}
