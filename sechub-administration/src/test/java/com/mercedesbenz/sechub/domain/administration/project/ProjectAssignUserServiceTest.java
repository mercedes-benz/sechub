// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.access.AccessDeniedException;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectAssignUserServiceTest {

    private ProjectAssignUserService serviceToTest;
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

        serviceToTest = new ProjectAssignUserService();
        serviceToTest.eventBus = eventBusService;
        serviceToTest.projectRepository = projectRepository;
        serviceToTest.userRepository = userRepository;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.userContextService = userContext;
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.transactionService = transactionService;
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void assign_new_user_to_project(boolean failOnExistingAssignment) {
        User newUser = mock(User.class);
        User owner = mock(User.class);

        /* prepare */
        Project project1 = new Project();
        project1.id = "project1";
        project1.owner = owner;

        when(newUser.getName()).thenReturn("new");
        when(owner.getName()).thenReturn("owner");
        when(userContext.getUserId()).thenReturn("owner");
        when(userRepository.findOrFailUser("owner")).thenReturn(owner);
        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(userRepository.findOrFailUser("new")).thenReturn(newUser);

        /* execute */
        serviceToTest.assignUserToProjectAsUser(newUser.getName(), project1.getId(), failOnExistingAssignment);

        /* test */
        verify(transactionService).saveInOwnTransaction(project1, newUser);
    }

    @Test
    void assign_already_added_user_to_project__throws_already_exists_exception_when_fail_wanted() {

        User existingUser = mock(User.class);
        User owner = mock(User.class);

        /* prepare */
        Project project1 = new Project();
        project1.owner = owner;
        when(owner.getName()).thenReturn("owner");
        when(userContext.getUserId()).thenReturn("owner");
        project1.id = "project1";
        project1.users.add(existingUser);

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(existingUser.getName()).thenReturn("existing");
        when(userRepository.findOrFailUser("existing")).thenReturn(existingUser);

        /* execute + test */
        assertThatThrownBy(() -> {
            serviceToTest.assignUserToProjectAsUser(existingUser.getName(), project1.getId(), true);
        }).isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void assign_already_added_user_to_project__not_throws_any_exception_when_fail_not_wanted() {

        User existingUser = mock(User.class);
        User owner = mock(User.class);

        /* prepare */
        Project project1 = new Project();
        project1.owner = owner;
        when(owner.getName()).thenReturn("owner");
        when(userContext.getUserId()).thenReturn("owner");
        project1.id = "project1";
        project1.users.add(existingUser);

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(existingUser.getName()).thenReturn("existing");
        when(userRepository.findOrFailUser("existing")).thenReturn(existingUser);

        /* execute + test */
        assertDoesNotThrow(() -> serviceToTest.assignUserToProjectAsUser(existingUser.getName(), project1.getId(), false));
    }

    @Test
    void assign_user_to_project_does_not_fail_when_user_is_owner() {
        User owner = mock(User.class);
        User newUser = mock(User.class);

        /* prepare */
        when(owner.getName()).thenReturn("owner");
        when(userContext.getUserId()).thenReturn("owner");

        Project project1 = new Project();
        project1.owner = owner;
        project1.id = "project1";
        when(projectRepository.findOrFailProject(project1.id)).thenReturn(project1);
        when(userRepository.findOrFailUser(newUser.getName())).thenReturn(newUser);
        when(owner.getProjects()).thenReturn(Set.of(project1));

        /* execute */
        serviceToTest.assignUserToProjectAsUser(newUser.getName(), project1.getId(), false);

        /* test */
        verify(transactionService).saveInOwnTransaction(project1, newUser);
    }

    @Test
    void assign_user_to_project_fails_when_user_is_not_superuser_or_owner() {
        User owner = mock(User.class);
        User newUser = mock(User.class);

        /* prepare */
        when(owner.getName()).thenReturn("owner");
        when(userContext.getUserId()).thenReturn("someOtherUser");
        when(userContext.isSuperAdmin()).thenReturn(false);

        Project project1 = new Project();
        project1.owner = owner;
        project1.id = "project1";
        when(projectRepository.findOrFailProject(project1.id)).thenReturn(project1);
        when(userRepository.findOrFailUser(newUser.getName())).thenReturn(newUser);
        when(owner.getProjects()).thenReturn(Set.of(project1));

        /* @formatter:off */
        /* execute + test */
        assertThatThrownBy(() -> {
            serviceToTest.assignUserToProjectAsUser(newUser.getName(), project1.getId(), false);
        }).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed");
        /* @formatter:on */
    }

}
