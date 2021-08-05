// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;

import com.daimler.sechub.domain.scan.access.ScanDeleteAnyAccessToProjectAtAllService;
import com.daimler.sechub.domain.scan.access.ScanGrantUserAccessToProjectService;
import com.daimler.sechub.domain.scan.access.ScanRevokeUserAccessAtAllService;
import com.daimler.sechub.domain.scan.access.ScanRevokeUserAccessFromProjectService;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigAccessLevelService;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.DummyEventInspector;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;

/**
 * The test does not only test the message handler but also the domain message service recognition
 * of asynchronous event resolving by annotations.
 * @author Albert Tregnaghi
 *
 */
public class ScanMessageHandlerTest {

	private ScanMessageHandler scheduleHandlerToTest;
	private FakeDomainMessageService fakeDomainMessageService;

	@Before
	public void before() {
		scheduleHandlerToTest = new ScanMessageHandler();

		scheduleHandlerToTest.grantService= mock(ScanGrantUserAccessToProjectService.class);
		scheduleHandlerToTest.revokeUserFromProjectService=mock(ScanRevokeUserAccessFromProjectService.class);
		scheduleHandlerToTest.revokeUserService=mock(ScanRevokeUserAccessAtAllService.class);
		scheduleHandlerToTest.deleteAllProjectAccessService=mock(ScanDeleteAnyAccessToProjectAtAllService.class);
		scheduleHandlerToTest.projectDataDeleteService=mock(ProjectDataDeleteService.class);
		scheduleHandlerToTest.projectAccessLevelService=mock(ScanProjectConfigAccessLevelService.class);

		List<AsynchronMessageHandler> injectedAsynchronousHandlers = new ArrayList<>();
		injectedAsynchronousHandlers.add(scheduleHandlerToTest);
		List<SynchronMessageHandler> injectedSynchronousHandlers = new ArrayList<>();

		fakeDomainMessageService = new FakeDomainMessageService(injectedSynchronousHandlers, injectedAsynchronousHandlers);

	}

	
	@Test
    public void when_sending_message_id_PROJECT_ACCESS_LEVEL_CHANGED_changeProjectAccessLevel_is_called() {
        /* prepare */
	    ProjectAccessLevel newAccessLevel=ProjectAccessLevel.NONE;
	    ProjectAccessLevel formerAccessLevel=ProjectAccessLevel.READ_ONLY;

	    DomainMessage request = new DomainMessage(MessageID.PROJECT_ACCESS_LEVEL_CHANGED);
        ProjectMessage content = new ProjectMessage();
        content.setProjectId("projectId1");
        content.setFormerAccessLevel(formerAccessLevel);
        content.setNewAccessLevel(newAccessLevel);
        
        request.set(MessageDataKeys.PROJECT_ACCESS_LEVEL_CHANGE_DATA, content);

        /* execute */
        simulateEventSend(request, scheduleHandlerToTest);

        /* test */
        verify(scheduleHandlerToTest.projectAccessLevelService).changeProjectAccessLevel("projectId1",newAccessLevel,formerAccessLevel);

    }


	@Test
	public void when_sending_message_id_PROJECT_DELETED_the_deleteAllDataForProject_is_called() {
		/* prepare */
		DomainMessage request = new DomainMessage(MessageID.PROJECT_DELETED);
		ProjectMessage content = new ProjectMessage();
		content.setProjectId("projectId1");
		request.set(MessageDataKeys.PROJECT_DELETE_DATA, content);

		/* execute */
		simulateEventSend(request, scheduleHandlerToTest);

		/* test */
		verify(scheduleHandlerToTest.projectDataDeleteService).deleteAllDataForProject("projectId1");

	}

	@Test
	public void when_sending_message_id_PROJECT_DELETED_the_deleteAllProjectAccessService_is_called() {
		/* prepare */
		DomainMessage request = new DomainMessage(MessageID.PROJECT_DELETED);
		ProjectMessage content = new ProjectMessage();
		content.setProjectId("projectId1");
		request.set(MessageDataKeys.PROJECT_DELETE_DATA, content);

		/* execute */
		simulateEventSend(request, scheduleHandlerToTest);

		/* test */
		verify(scheduleHandlerToTest.deleteAllProjectAccessService).deleteAnyAccessDataForProject("projectId1");

	}

	private void simulateEventSend(DomainMessage request,  AsynchronMessageHandler handler) {
		fakeDomainMessageService.sendAsynchron(request);
	}

	private class FakeDomainMessageService extends DomainMessageService{

		public FakeDomainMessageService(List<SynchronMessageHandler> injectedSynchronousHandlers,
				List<AsynchronMessageHandler> injectedAsynchronousHandlers) {
			super(injectedSynchronousHandlers, injectedAsynchronousHandlers);
			this.taskExecutor=new TestTaskExecutor();
			this.eventInspector=new DummyEventInspector();
		}

	}
	private class TestTaskExecutor implements TaskExecutor{

		@Override
		public void execute(Runnable task) {
			task.run();
		}

	}

}
