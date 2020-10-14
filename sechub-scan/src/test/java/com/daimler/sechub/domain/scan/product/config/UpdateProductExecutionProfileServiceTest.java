package com.daimler.sechub.domain.scan.product.config;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.validation.ProductExecutionProfileIdValidation;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class UpdateProductExecutionProfileServiceTest {

    private UpdateProductExecutionProfileService serviceToTest;
    private ProductExecutionProfileRepository repository;

    @Before
    public void before() throws Exception {
        serviceToTest=new UpdateProductExecutionProfileService();

        serviceToTest.profileIdValidation=mock(ProductExecutionProfileIdValidation.class);
        serviceToTest.projectIdValidation=mock(ProjectIdValidation.class);
        ValidationResult validResult = new ValidationResult();
        when(serviceToTest.profileIdValidation.validate(any())).thenReturn(validResult);        
        when(serviceToTest.projectIdValidation.validate(any())).thenReturn(validResult);        
        repository=mock(ProductExecutionProfileRepository.class);
        serviceToTest.auditLogService=mock(AuditLogService.class);
        serviceToTest.repository=repository;
    }

    @Test
    public void when_no_relation_ship_exists_add_will_do_call_repo_add() {
        /* prepare */
        when(repository.countRelationShipEntries("profile1","project1")).thenReturn(0);
        
        /* execute */
        serviceToTest.addProjectToProfileRelation("profile1", "project1");
        
        /* test */
        verify(repository).createProfileRelationToProject("profile1", "project1");
        
    }
    
    @Test
    public void when_1_relation_ship_exists_add_will_NOT_call_repo_add() {
        /* prepare */
        when(repository.countRelationShipEntries("profile1","project1")).thenReturn(1);
        
        /* execute */
        serviceToTest.addProjectToProfileRelation("profile1", "project1");
        
        /* test */
        verify(repository,never()).createProfileRelationToProject("profile1", "project1");
        
    }

}
