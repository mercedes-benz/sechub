// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class ProjectUnassignUserServiceTest {

    private DomainMessageService eventBus;
    private ProjectRepository projectRepository;
    private UserRepository userRepository;
    private LogSanitizer logSanitizer;
    private AuditLogService auditLogService;
    private UserInputAssertion assertion;
    private ProjectTransactionService transactionService;
    private UserContextService userContextService;
    private ProjectUnassignUserService serviceToTest;

    @BeforeEach
    void beforeEach() {
        eventBus = mock(DomainMessageService.class);
        projectRepository = mock(ProjectRepository.class);
        userRepository = mock(UserRepository.class);
        logSanitizer = mock(LogSanitizer.class);
        auditLogService = mock(AuditLogService.class);
        assertion = mock(UserInputAssertion.class);
        transactionService = mock(ProjectTransactionService.class);
        userContextService = mock(UserContextService.class);

        serviceToTest = new ProjectUnassignUserService();
        serviceToTest.eventBus = eventBus;
        serviceToTest.projectRepository = projectRepository;
        serviceToTest.userRepository = userRepository;
        serviceToTest.logSanitizer = logSanitizer;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.assertion = assertion;
        serviceToTest.transactionService = transactionService;
        serviceToTest.userContextService = userContextService;
    }

    @Test
    void unassign_user_from_project_does_not_fail_for_owner() {
        /* prepare */
        User owner = mock(User.class);
        User assignedUser = mock(User.class);
        Project project = mock(Project.class);
        baseSetUp(owner, assignedUser, project);

        when(userContextService.getUserId()).thenReturn("owner");
        when(userContextService.isSuperAdmin()).thenReturn(false);

        /* execute */
        serviceToTest.unassignUserFromProject(assignedUser.getName(), project.getId());

        /* test */
        verify(transactionService).saveInOwnTransaction(project, assignedUser);
    }

    @Test
    void unassign_user_from_project_does_not_fail_for_superAdmin() {
        /* prepare */
        User owner = mock(User.class);
        User assignedUser = mock(User.class);
        Project project = mock(Project.class);
        baseSetUp(owner, assignedUser, project);

        when(userContextService.getUserId()).thenReturn("assignedUser");
        when(userContextService.isSuperAdmin()).thenReturn(true);

        /* execute */
        serviceToTest.unassignUserFromProject(owner.getName(), project.getId());

        /* test */
        verify(transactionService).saveInOwnTransaction(project, owner);
    }

    @Test
    void unassign_user_from_project_fails_for_non_superAdmin_and_non_owner() {
        /* prepare */
        User owner = mock(User.class);
        User assignedUser = mock(User.class);
        Project project = mock(Project.class);
        baseSetUp(owner, assignedUser, project);

        when(userContextService.getUserId()).thenReturn("notOwner");
        when(userContextService.isSuperAdmin()).thenReturn(false);

        /* execute + test */
        assertThatThrownBy(() -> {
            serviceToTest.unassignUserFromProject(owner.getName(), project.getId());
        }).isInstanceOf(AccessDeniedException.class).hasMessageContaining("not allowed to remove members from project");
    }

    @Test
    void unassign_user_from_project_fails_when_user_is_not_assigned() {
        /* prepare */
        User owner = mock(User.class);
        User assignedUser = mock(User.class);
        Project project = mock(Project.class);
        baseSetUp(owner, assignedUser, project);

        when(userContextService.getUserId()).thenReturn("owner");
        when(userContextService.isSuperAdmin()).thenReturn(false);

        /* execute + test */
        assertThatThrownBy(() -> {
            serviceToTest.unassignUserFromProject("notAssignedUser", project.getId());
        }).isInstanceOf(NotAcceptableException.class).hasMessageContaining("User is not assigned to this project!");
    }

    private void baseSetUp(User owner, User assignedUser, Project project) {

        String ownerId = "owner";
        String projectId = "project1";
        String assignedUserId = "assignedUser";

        when(owner.getName()).thenReturn(ownerId);
        when(assignedUser.getName()).thenReturn(assignedUserId);

        Set<User> users = new HashSet<>();
        users.add(assignedUser);
        users.add(owner);

        when(project.getId()).thenReturn(projectId);
        when(project.getUsers()).thenReturn(users);
        when(project.getOwner()).thenReturn(owner);

        when(userRepository.findOrFailUser(ownerId)).thenReturn(owner);
        when(userRepository.findOrFailUser(assignedUserId)).thenReturn(assignedUser);
        when(projectRepository.findOrFailProject(projectId)).thenReturn(project);
    }

}