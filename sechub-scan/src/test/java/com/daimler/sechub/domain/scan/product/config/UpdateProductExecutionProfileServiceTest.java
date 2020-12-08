// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class UpdateProductExecutionProfileServiceTest {

    private UpdateProductExecutionProfileService serviceToTest;
    private ProductExecutionProfileRepository repository;

    @Before
    public void before() throws Exception {
        serviceToTest = new UpdateProductExecutionProfileService();

        serviceToTest.profileValidation = mock(ProductExecutionProfileValidation.class);
        serviceToTest.profileIdValidation = mock(ProductExecutionProfileIdValidation.class);
        serviceToTest.projectIdValidation = mock(ProjectIdValidation.class);
        ValidationResult validResult = new ValidationResult();
        when(serviceToTest.profileValidation.validate(any())).thenReturn(validResult);
        when(serviceToTest.profileIdValidation.validate(any())).thenReturn(validResult);
        when(serviceToTest.projectIdValidation.validate(any())).thenReturn(validResult);
        repository = mock(ProductExecutionProfileRepository.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);
        serviceToTest.repository = repository;
    }

    @Test
    public void service_does_not_change_project_relations_on_update_but_enabled_and_description() {
        /* prepare */
        ProductExecutionProfile profile = new ProductExecutionProfile();
        profile.id = "profileid1";
        profile.description = "d1";
        profile.projectIds.add("project1");
        profile.enabled = false;
        Optional<ProductExecutionProfile> opt = Optional.of(profile);
        when(repository.findById("profileid1")).thenReturn(opt);

        ProductExecutionProfile userProfile = new ProductExecutionProfile();
        userProfile.id = profile.id;
        userProfile.description = "d1-changed";
        userProfile.enabled = true;

        ArgumentCaptor<ProductExecutionProfile> captor = ArgumentCaptor.forClass(ProductExecutionProfile.class);

        /* execute */
        serviceToTest.updateExecutionProfile(userProfile.id, userProfile);

        /* test */
        verify(repository).save(captor.capture());
        ProductExecutionProfile stored = captor.getValue();
        assertEquals("d1-changed", stored.getDescription());
        assertEquals(true, stored.enabled);
        // but not this has changed:
        assertEquals(1, stored.getProjectIds().size());
        assertEquals("project1", stored.getProjectIds().iterator().next());
    }

    @Test
    public void when_no_relation_ship_exists_add_will_do_call_repo_add() {
        /* prepare */
        when(repository.countRelationShipEntries("profile1", "project1")).thenReturn(0);

        /* execute */
        serviceToTest.addProjectToProfileRelation("profile1", "project1");

        /* test */
        verify(repository).createProfileRelationToProject("profile1", "project1");

    }

    @Test
    public void when_1_relation_ship_exists_add_will_NOT_call_repo_add() {
        /* prepare */
        when(repository.countRelationShipEntries("profile1", "project1")).thenReturn(1);

        /* execute */
        serviceToTest.addProjectToProfileRelation("profile1", "project1");

        /* test */
        verify(repository, never()).createProfileRelationToProject("profile1", "project1");

    }

}
