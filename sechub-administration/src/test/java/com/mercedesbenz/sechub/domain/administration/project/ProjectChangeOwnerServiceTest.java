// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    void assign_new_owner_to_project_stores_project_old_owner_old_user() {
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

        /* execute */
        serviceToTest.changeProjectOwner(newOwner.getName(), project1.getId());

        /* test */
        var projectCaptor = ArgumentCaptor.forClass(Project.class);

        verify(transactionService).saveInOwnTransaction(projectCaptor.capture(), eq(newOwner), eq(oldOwner));

        Project project = projectCaptor.getValue();
        assertThat(project.getOwner()).isEqualTo(newOwner);
    }

    @Test
    void assign_new_owner_to_project_sends_events() {
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

        /* execute */
        serviceToTest.changeProjectOwner(newOwner.getName(), project1.getId());

        /* test */
        var messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBusService, times(3)).sendAsynchron(messageCaptor.capture());

        List<DomainMessage> sentMessages = messageCaptor.getAllValues();
        DomainMessage firstMessage = sentMessages.get(0);
        DomainMessage secondMessage = sentMessages.get(1);
        DomainMessage thirdMessage = sentMessages.get(2);

        assertThat(firstMessage.getMessageId()).isEqualTo(MessageID.PROJECT_OWNER_CHANGED);

        assertThat(secondMessage.getMessageId()).isEqualTo(MessageID.REQUEST_USER_ROLE_RECALCULATION);
        assertThat(secondMessage.get(MessageDataKeys.USER_ID_DATA).getUserId()).isEqualTo(oldOwner.getName()); // direct recalculation for old owner -> maybe no
                                                                                                               // longer role "owner"

        assertThat(thirdMessage.getMessageId()).isEqualTo(MessageID.ASSIGN_OWNER_AS_USER_TO_PROJECT); // assign owner as user to project --> will recalculate
                                                                                                      // roles
        assertThat(thirdMessage.get(MessageDataKeys.PROJECT_TO_USER_DATA).getUserId()).isEqualTo(newOwner.getName());
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

        /* execute */
        /* test */
        assertThatThrownBy(() -> {
            serviceToTest.changeProjectOwner(oldOwner.getName(), project1.getId());
        }).isInstanceOf(AlreadyExistsException.class);
    }

}
