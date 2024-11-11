// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectDeleteServiceTest {

    private ProjectDeleteService serviceToTest;
    private UserContextService userContext;
    private DomainMessageService eventBusService;
    private ProjectRepository projectRepository;
    private ProjectTransactionService transactionService;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    private AuditLogService auditLogService;

    @Before
    public void before() throws Exception {
        eventBusService = mock(DomainMessageService.class);
        userContext = mock(UserContextService.class);
        projectRepository = mock(ProjectRepository.class);
        auditLogService = mock(AuditLogService.class);
        transactionService = mock(ProjectTransactionService.class);

        serviceToTest = new ProjectDeleteService();
        serviceToTest.eventBusService = eventBusService;
        serviceToTest.userContext = userContext;
        serviceToTest.projectRepository = projectRepository;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.transactionService = transactionService;

        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.sechubEnvironment = mock(SecHubEnvironment.class);
    }

    @Test
    public void when_a_project_is_found_it_will_be_deleted() {

        /* prepare */
        Project project1 = new Project();
        project1.id = "project1";

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);

        /* execute */
        serviceToTest.deleteProject("project1");

        /* test */
        verify(transactionService).deleteWithAssociationsInOwnTransaction(project1.getId());

    }

    @Test
    public void when_a_project_is_not_found_nothing_not_found_exception_will_forwarded() {

        /* test */
        expected.expect(NotFoundException.class);

        /* prepare */
        when(projectRepository.findOrFailProject("project1")).thenThrow(new NotFoundException());

        /* execute */
        serviceToTest.deleteProject("project1");

    }

    @Test
    public void when_a_project_is_found_2_events_will_be_triggered_first_project_deleted_second_owner_recalc() {

        /* prepare */
        ArgumentCaptor<DomainMessage> captorMessage = ArgumentCaptor.forClass(DomainMessage.class);

        Project project1 = new Project();
        project1.id = "project1";
        User owner1 = mock(User.class);
        when(owner1.getName()).thenReturn("owner1");
        project1.owner = owner1;

        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);

        /* execute */
        serviceToTest.deleteProject("project1");

        /* test */
        verify(eventBusService, times(2)).sendAsynchron(captorMessage.capture());
        List<DomainMessage> allMessages = captorMessage.getAllValues();

        DomainMessage value1 = allMessages.get(0);
        assertEquals(MessageID.PROJECT_DELETED, value1.getMessageId());
        assertEquals("project1", value1.get(MessageDataKeys.PROJECT_DELETE_DATA).getProjectId());

        DomainMessage value2 = allMessages.get(1);
        assertEquals(MessageID.REQUEST_USER_ROLE_RECALCULATION, value2.getMessageId());
        assertEquals("owner1", value2.get(MessageDataKeys.USER_ID_DATA).getUserId());
    }

}
