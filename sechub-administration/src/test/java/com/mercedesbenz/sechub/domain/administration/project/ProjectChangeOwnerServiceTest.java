// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectChangeOwnerServiceTest {

    private ProjectChangeOwnerService serviceToTest;
    private UserContextService userContext;
    private DomainMessageService eventBusService;
    private ProjectRepository projectRepository;
    private UserRepository userRepository;
    private ProjectTransactionService transactionService;

    @BeforeEach
    void beforeEach() throws Exception {
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
    void as_super_admin_assign_new_owner_to_project() {
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
        when(newOwner.getProjects()).thenReturn(new HashSet<>());
        when(userContext.isSuperAdmin()).thenReturn(true);

        /* execute */
        serviceToTest.changeProjectOwner(newOwner.getName(), project1.getId());

        /* test */
        verify(transactionService).saveInOwnTransaction(project1, newOwner, oldOwner);
    }

    @Test
    void as_owner_assign_new_owner_to_project() {
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
        when(newOwner.getProjects()).thenReturn(new HashSet<>());
        when(userContext.isSuperAdmin()).thenReturn(false);
        when(userContext.getUserId()).thenReturn("old");

        /* execute */
        serviceToTest.changeProjectOwner(newOwner.getName(), project1.getId());

        /* test */
        verify(transactionService).saveInOwnTransaction(project1, newOwner, oldOwner);
    }

    @Test
    void as_other_user_try_to_assign_new_owner_to_project_results_in_access_denied() {
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
        when(newOwner.getProjects()).thenReturn(new HashSet<>());
        when(userContext.isSuperAdmin()).thenReturn(false);
        when(userContext.getUserId()).thenReturn("other-user");

        /* execute + test */
        assertThatThrownBy(() -> {
            serviceToTest.changeProjectOwner(newOwner.getName(), project1.getId());
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void assign_same_owner_to_project__throws_already_exists_exception() {

        User oldOwner = mock(User.class);

        /* prepare */
        Project project1 = new Project();
        project1.id = "project1";
        project1.owner = oldOwner;

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(oldOwner.getName()).thenReturn("old");
        when(userRepository.findOrFailUser("old")).thenReturn(oldOwner);
        when(userContext.isSuperAdmin()).thenReturn(true);

        /* execute + test */
        assertThatThrownBy(() -> {
            serviceToTest.changeProjectOwner(oldOwner.getName(), project1.getId());
        }).isInstanceOf(AlreadyExistsException.class);
    }

}
