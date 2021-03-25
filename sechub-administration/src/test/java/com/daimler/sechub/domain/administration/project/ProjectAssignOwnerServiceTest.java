// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.AlreadyExistsException;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectAssignOwnerServiceTest {

    private ProjectAssignOwnerService serviceToTest;
    private UserContextService userContext;
    private DomainMessageService eventBusService;
    private ProjectRepository projectRepository;
    private UserRepository userRepository;
    private ProjectTransactionService transactionService;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    @Before
    public void before() throws Exception {
        eventBusService = mock(DomainMessageService.class);
        projectRepository = mock(ProjectRepository.class);
        userRepository = mock(UserRepository.class);
        userContext = mock(UserContextService.class);

        transactionService = mock(ProjectTransactionService.class);

        serviceToTest = new ProjectAssignOwnerService();
        serviceToTest.eventBus = eventBusService;
        serviceToTest.projectRepository = projectRepository;
        serviceToTest.userRepository = userRepository;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.userContextService = userContext;
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.transactionService = transactionService;
    }

    @Test
    public void assign_new_owner_to_project() {
        
        User oldOwner = mock(User.class);
        User newOwner = mock(User.class);
                
        /* prepare */
        Project project1 = new Project();
        project1.id = "project1";
        project1.owner = oldOwner;

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(oldOwner.getName()).thenReturn("old");
        when(newOwner.getName()).thenReturn("new");
        when(userRepository.findOrFailUser("new")).thenReturn(newOwner);
        when(newOwner.getProjects()).thenReturn(new HashSet<Project>());
        
        /* execute */
        serviceToTest.assignOwnerToProject(newOwner.getName(), project1.getId());
        
        /* test */
        verify(transactionService).saveInOwnTransaction(project1, newOwner);
    }
    
    @Test
    public void assign_same_owner_to_project__throws_already_exists_exception() {
        
        User oldOwner = mock(User.class);
                        
        /* prepare */
        Project project1 = new Project();
        project1.id = "project1";
        project1.owner = oldOwner;

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(oldOwner.getName()).thenReturn("old");
        when(userRepository.findOrFailUser("old")).thenReturn(oldOwner);
        
        /* execute */
        /* test */
        assertThrows(AlreadyExistsException.class, () -> {
            serviceToTest.assignOwnerToProject(oldOwner.getName(), project1.getId());
        });
    }

}
