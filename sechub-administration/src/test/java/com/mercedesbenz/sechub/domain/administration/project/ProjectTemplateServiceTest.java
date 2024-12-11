// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectTemplateData;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;

class ProjectTemplateServiceTest {

    private static final String CORRECT_ASSIGN_TEMPLATE_RESULT_MESSAGE_ID = "RESULT_ASSIGN_TEMPLATE_TO_PROJECT";
    private static final String CORRECT_UNASSIGN_TEMPLATE_RESULT_MESSAGE_ID = "RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT";
    private static final String ASSIGNED_TEMPLATE_ID_A = "template-a";
    private static final String ASSIGNED_TEMPLATE_ID_B = "template-b";

    private static final String ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_1 = "template-after_change-1";
    private static final String ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_2 = "template-after-change-2";

    private static final String PROJECT_ID1 = "project1";
    private static final String TEMPLATE_ID1 = "templateId1";
    private ProjectTemplateService serviceToTest;
    private DomainMessageService eventBus;
    private ProjectRepository projectRepository;
    private ProjectTransactionService projectTansactionService;

    @BeforeEach
    void beforeEach() {

        eventBus = mock();
        projectRepository = mock();
        projectTansactionService = mock();

        serviceToTest = new ProjectTemplateService();
        serviceToTest.assertion = mock();
        serviceToTest.eventBus = eventBus;

        serviceToTest.projectRepository = projectRepository;
        serviceToTest.projectTansactionService = projectTansactionService;
    }

    @Test
    void assignTemplateToProject_sends_assign_request_synchronous_with_expected_data() {

        /* prepare */
        mockEventBusSynchronResultWithMessageId(MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT);

        /* execute */
        serviceToTest.assignTemplateToProject(TEMPLATE_ID1, PROJECT_ID1);

        /* test */
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.captor();
        verify(eventBus).sendSynchron(messageCaptor.capture());

        DomainMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getMessageId()).isEqualTo(MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT);
        SecHubProjectToTemplate sentMessageData = sentMessage.get(MessageDataKeys.PROJECT_TO_TEMPLATE);
        assertThat(sentMessageData).isNotNull();
        assertThat(sentMessageData.getProjectId()).isEqualTo(PROJECT_ID1);
        assertThat(sentMessageData.getTemplateId()).isEqualTo(TEMPLATE_ID1);

    }

    @Test
    void assignTemplateToProject_updates_template_by_synchronous_event_result() {

        /* prepare */
        mockEventBusSynchronResultWithMessageId(MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT);

        /* execute */
        serviceToTest.assignTemplateToProject(TEMPLATE_ID1, PROJECT_ID1);

        /* test */
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectTansactionService).saveInOwnTransaction(projectCaptor.capture());
        Project projectSaved = projectCaptor.getValue();
        assertThat(projectSaved.getTemplateIds()).hasSize(2).describedAs("project templates must be changed by result data")
                .contains(ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_1).contains(ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_2);

    }

    @Test
    void unassignTemplateFromProject_sends_assign_request_synchronous_with_expected_data() {

        /* prepare */
        mockEventBusSynchronResultWithMessageId(MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT);

        /* execute */
        serviceToTest.unassignTemplateFromProject(TEMPLATE_ID1, PROJECT_ID1);

        /* test */
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.captor();
        verify(eventBus).sendSynchron(messageCaptor.capture());

        DomainMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getMessageId()).isEqualTo(MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT);
        SecHubProjectToTemplate sentMessageData = sentMessage.get(MessageDataKeys.PROJECT_TO_TEMPLATE);
        assertThat(sentMessageData).isNotNull();
        assertThat(sentMessageData.getProjectId()).isEqualTo(PROJECT_ID1);
        assertThat(sentMessageData.getTemplateId()).isEqualTo(TEMPLATE_ID1);
    }

    @Test
    void unassignTemplateFromProject_updates_template_by_synchronous_event_result() {

        /* prepare */
        mockEventBusSynchronResultWithMessageId(MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT);

        /* execute */
        serviceToTest.unassignTemplateFromProject(TEMPLATE_ID1, PROJECT_ID1);

        /* test */
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectTansactionService).saveInOwnTransaction(projectCaptor.capture());
        Project projectSaved = projectCaptor.getValue();
        assertThat(projectSaved.getTemplateIds()).hasSize(2).describedAs("project templates must be changed by result data")
                .contains(ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_1).contains(ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_2);

    }

    @ParameterizedTest
    @EnumSource(value = MessageID.class, mode = Mode.EXCLUDE, names = CORRECT_UNASSIGN_TEMPLATE_RESULT_MESSAGE_ID)
    void unassignTemplateFromProject_when_synchronous_event_result_has_unsupported_message_throws_invalid_exception(MessageID wrongMessageId) {
        /* prepare */
        mockEventBusSynchronResultWithMessageId(wrongMessageId);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.unassignTemplateFromProject(TEMPLATE_ID1, PROJECT_ID1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Result message id not supported");

    }

    @ParameterizedTest
    @EnumSource(value = MessageID.class, mode = Mode.EXCLUDE, names = CORRECT_ASSIGN_TEMPLATE_RESULT_MESSAGE_ID)
    void assignTemplateToProject_when_synchronous_event_result_has_unsupported_message_throws_invalid_exception(MessageID wrongMessageId) {
        mockEventBusSynchronResultWithMessageId(wrongMessageId);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.assignTemplateToProject(TEMPLATE_ID1, PROJECT_ID1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Result message id not supported");

    }

    @Test
    void assignTemplateToProject_when_event_result_failed_exception_is_thrown() {
        /* prepare */
        DomainMessageSynchronousResult mockedResultMessage = mockEventBusSynchronResultWithMessageId(
                MessageID.valueOf(CORRECT_UNASSIGN_TEMPLATE_RESULT_MESSAGE_ID));
        when(mockedResultMessage.hasFailed()).thenReturn(true);

        assertThatThrownBy(() -> serviceToTest.assignTemplateToProject(TEMPLATE_ID1, PROJECT_ID1)).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("Was not able to change template to project assignment");
    }

    @Test
    void unassignTemplateFromProject_when_event_result_failed_exception_is_thrown() {
        /* prepare */
        DomainMessageSynchronousResult mockedResultMessage = mockEventBusSynchronResultWithMessageId(
                MessageID.valueOf(CORRECT_UNASSIGN_TEMPLATE_RESULT_MESSAGE_ID));
        when(mockedResultMessage.hasFailed()).thenReturn(true);

        assertThatThrownBy(() -> serviceToTest.unassignTemplateFromProject(TEMPLATE_ID1, PROJECT_ID1)).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("Was not able to change template to project assignment");
    }

    private DomainMessageSynchronousResult mockEventBusSynchronResultWithMessageId(MessageID resultMessageId) {
        Project project1 = new Project();
        project1.getTemplateIds().addAll(Set.of(ASSIGNED_TEMPLATE_ID_A, ASSIGNED_TEMPLATE_ID_B));

        when(projectRepository.findOrFailProject(PROJECT_ID1)).thenReturn(project1);

        SecHubProjectTemplateData mockedResultData = mock();
        when(mockedResultData.getProjectId()).thenReturn("result-project");
        when(mockedResultData.getTemplateIds()).thenReturn(List.of(ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_1, ASSIGNED_TEMPLATE_ID_AFTER_CHANGE_2));
        DomainMessageSynchronousResult mockedResultMessage = mock();
        when(mockedResultMessage.getMessageId()).thenReturn(resultMessageId);
        when(mockedResultMessage.get(MessageDataKeys.PROJECT_TEMPLATES)).thenReturn(mockedResultData);

        when(eventBus.sendSynchron(any(DomainMessage.class))).thenReturn(mockedResultMessage);

        return mockedResultMessage;
    }

}
