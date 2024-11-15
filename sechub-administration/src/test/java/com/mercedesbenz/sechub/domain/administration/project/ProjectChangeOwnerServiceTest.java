// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectChangeOwnerServiceTest {

    private ProjectChangeOwnerService serviceToTest;
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

        serviceToTest = new ProjectChangeOwnerService();
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
        /* prepare */
        User oldOwner = mock(User.class);
        User newOwner = mock(User.class);

        Project project1 = new Project();
        project1.id = "project1";
        project1.owner = oldOwner;

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(oldOwner.getName()).thenReturn("old");
        when(newOwner.getName()).thenReturn("new");
        when(userRepository.findOrFailUser("new")).thenReturn(newOwner);
        when(newOwner.getProjects()).thenReturn(new HashSet<Project>());

        /* execute */
        serviceToTest.changeProjectOwner(newOwner.getName(), project1.getId());

        /* test */
        verify(transactionService).saveInOwnTransaction(project1, newOwner, oldOwner);
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
            serviceToTest.changeProjectOwner(oldOwner.getName(), project1.getId());
        });
    }

}
