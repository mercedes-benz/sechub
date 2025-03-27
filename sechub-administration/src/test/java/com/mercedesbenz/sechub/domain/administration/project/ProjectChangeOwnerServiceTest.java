// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
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
        var projectCaptor = ArgumentCaptor.forClass(Project.class);

        verify(transactionService).saveInOwnTransaction(projectCaptor.capture(), eq(newOwner), eq(oldOwner));

        Project project = projectCaptor.getValue();
        assertThat(project.getOwner()).isEqualTo(newOwner);
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
        verifyNoInteractions(eventBusService);
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
        verifyNoInteractions(eventBusService);
    }

    @ParameterizedTest
    @ArgumentsSource(ChangeOwnerToProjectWithDifferentRolesSendsEventsArgumentsProvider.class)
    void assign_new_owner_to_project_sends_events(boolean user1asSuperAdmin, boolean user1asProjectOwner) {
        /* prepare */
        User user1 = mock(User.class);
        when(user1.getName()).thenReturn("user1");

        User user2 = mock(User.class);
        when(user2.getName()).thenReturn("user2");
        when(user2.getOwnedProjects()).thenReturn(new HashSet<>());

        User user3 = mock(User.class);
        when(user3.getName()).thenReturn("user3");

        Project project1 = new Project();
        project1.id = "project1";
        String oldOwnerId = null;
        if (user1asProjectOwner) {
            project1.owner = user1;
            oldOwnerId = "user1";
        } else {
            project1.owner = user3;
            oldOwnerId = "user3";
        }
        String newOwnerId = "user2";

        when(userContext.isSuperAdmin()).thenReturn(user1asSuperAdmin);
        when(userContext.getUserId()).thenReturn("user1");

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(userRepository.findOrFailUser("user2")).thenReturn(user2);
        when(user2.getProjects()).thenReturn(new HashSet<>());

        /* execute */
        serviceToTest.changeProjectOwner(user2.getName(), project1.getId());

        /* test */
        var messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBusService, times(3)).sendAsynchron(messageCaptor.capture());

        List<DomainMessage> sentMessages = messageCaptor.getAllValues();
        DomainMessage firstMessage = sentMessages.get(0);
        DomainMessage secondMessage = sentMessages.get(1);
        DomainMessage thirdMessage = sentMessages.get(2);

        assertThat(firstMessage.getMessageId()).isEqualTo(MessageID.PROJECT_OWNER_CHANGED);

        assertThat(secondMessage.getMessageId()).isEqualTo(MessageID.REQUEST_USER_ROLE_RECALCULATION);
        assertThat(secondMessage.get(MessageDataKeys.USER_ID_DATA).getUserId()).isEqualTo(oldOwnerId); // direct recalculation for old owner -> maybe no
        // longer role "owner"

        assertThat(thirdMessage.getMessageId()).isEqualTo(MessageID.ASSIGN_OWNER_AS_USER_TO_PROJECT); // assign owner as user to project --> will recalculate
        // roles
        assertThat(thirdMessage.get(MessageDataKeys.PROJECT_TO_USER_DATA).getUserId()).isEqualTo(newOwnerId);
    }

    private static class ChangeOwnerToProjectWithDifferentRolesSendsEventsArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                      /* admin, owner, expectEventSent */
              Arguments.of(false,true),
              Arguments.of(true,false),
              Arguments.of(true,true)
              );
        }
        /* @formatter:on*/
    }

}
