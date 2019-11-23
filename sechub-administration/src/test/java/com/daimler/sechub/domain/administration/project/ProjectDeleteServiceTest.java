// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectDeleteServiceTest {

	private ProjectDeleteService serviceToTest;
	private UserContextService userContext;
	private DomainMessageService eventBusService;
	private ProjectRepository projectRepository;

	@Rule
	public ExpectedException expected = ExpectedException.none();
	private AuditLogService auditLogService;

	@Before
	public void before() throws Exception {
		eventBusService = mock(DomainMessageService.class);
		userContext = mock(UserContextService.class);
		projectRepository=mock(ProjectRepository.class);
		auditLogService=mock(AuditLogService.class);

		serviceToTest= new ProjectDeleteService();
		serviceToTest.eventBusService=eventBusService;
		serviceToTest.userContext=userContext;
		serviceToTest.projectRepository=projectRepository;
		serviceToTest.auditLogService=auditLogService;
		serviceToTest.logSanitizer=mock(LogSanitizer.class);
		serviceToTest.assertion=mock(UserInputAssertion.class);
		serviceToTest.sechubEnvironment=mock(SecHubEnvironment.class);
	}



	@Test
	public void when_a_project_is_found_it_will_be_deleted() {

		/* prepare */
		Project project1 = new Project();
		project1.id="project1";

		when(projectRepository.findOrFailProject("project1")).thenReturn(project1);

		/* execute */
		serviceToTest.deleteProject("project1");

		/* test */
		verify(projectRepository).delete(project1);

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
	public void when_a_project_is_found_an_event_will_be_triggered_containing_project_id() {

		/* prepare */
		ArgumentCaptor<DomainMessage> captorMessage = ArgumentCaptor.forClass(DomainMessage.class);

		Project project1 = new Project();
		project1.id="project1";

		when(projectRepository.findOrFailProject("project1")).thenReturn(project1);

		/* execute */
		serviceToTest.deleteProject("project1");

		/* test */
		verify(eventBusService).sendAsynchron(captorMessage.capture());
		DomainMessage value = captorMessage.getValue();
		assertEquals(MessageID.PROJECT_DELETED, value.getMessageId());
		assertEquals("project1", value.get(MessageDataKeys.PROJECT_DELETE_DATA).getProjectId());
	}


}
