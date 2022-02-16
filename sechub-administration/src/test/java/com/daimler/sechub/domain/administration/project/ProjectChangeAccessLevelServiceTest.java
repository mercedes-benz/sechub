// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

class ProjectChangeAccessLevelServiceTest {

    private static final String PROJECT1_ID = "project1";

    private ProjectChangeAccessLevelService serviceToTest;
    private DomainMessageService eventBus;
    private ProjectTransactionService transactionService;
    private ProjectRepository repository;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ProjectChangeAccessLevelService();

        eventBus = mock(DomainMessageService.class);
        transactionService = mock(ProjectTransactionService.class);
        repository = mock(ProjectRepository.class);

        /* setup */
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.eventBus = eventBus;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);

        serviceToTest.transactionService = transactionService;
        serviceToTest.projectRepository = repository;

    }

    @Test
    void an_unecessary_change_from_full_access_to_full_access_does_not_trigger_any_event() {
        /* prepare */
        Project project = new Project();
        when(repository.findOrFailProject(PROJECT1_ID)).thenReturn(project);

        /* execute */
        serviceToTest.changeProjectAccessLevel(PROJECT1_ID, ProjectAccessLevel.FULL);

        /* test */
        verify(eventBus, never()).sendAsynchron(any());
        verify(eventBus, never()).sendSynchron(any());
    }

    @Test
    void a_change_from_full_access_to_read_only_does_trigger_one_async_event_containing_old_and_new_access_level() {
        /* prepare */
        Project project = new Project();
        when(repository.findOrFailProject(PROJECT1_ID)).thenReturn(project);

        /* execute */
        serviceToTest.changeProjectAccessLevel(PROJECT1_ID, ProjectAccessLevel.READ_ONLY);

        /* test */
        ArgumentCaptor<DomainMessage> eventCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBus).sendAsynchron(eventCaptor.capture());

        DomainMessage sentEvent = eventCaptor.getValue();
        assertEquals(MessageID.PROJECT_ACCESS_LEVEL_CHANGED, sentEvent.getMessageId());
        ProjectMessage eventProjectMessage = sentEvent.get(MessageDataKeys.PROJECT_ACCESS_LEVEL_CHANGE_DATA);
        assertNotNull(eventProjectMessage);

        assertEquals(ProjectAccessLevel.FULL, eventProjectMessage.getFormerAccessLevel());
        assertEquals(ProjectAccessLevel.READ_ONLY, eventProjectMessage.getNewAccessLevel());
    }

}
