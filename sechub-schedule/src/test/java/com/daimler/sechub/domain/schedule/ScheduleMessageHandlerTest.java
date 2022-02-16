// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;

import com.daimler.sechub.domain.schedule.access.ScheduleDeleteAllProjectAcessService;
import com.daimler.sechub.domain.schedule.access.ScheduleGrantUserAccessToProjectService;
import com.daimler.sechub.domain.schedule.access.ScheduleRevokeUserAccessAtAllService;
import com.daimler.sechub.domain.schedule.access.ScheduleRevokeUserAccessFromProjectService;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.DummyEventInspector;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

/**
 * The test does not only test the message handler but also the domain message
 * service recognition of asynchronous event resolving by annotations.
 *
 * @author Albert Tregnaghi
 *
 */
public class ScheduleMessageHandlerTest {

    private ScheduleMessageHandler scheduleHandlerToTest;
    private FakeDomainMessageService fakeDomainMessageService;

    @Before
    public void before() {
        scheduleHandlerToTest = new ScheduleMessageHandler();

        scheduleHandlerToTest.grantService = mock(ScheduleGrantUserAccessToProjectService.class);
        scheduleHandlerToTest.revokeUserFromProjectService = mock(ScheduleRevokeUserAccessFromProjectService.class);
        scheduleHandlerToTest.revokeUserService = mock(ScheduleRevokeUserAccessAtAllService.class);
        scheduleHandlerToTest.deleteAllProjectAccessService = mock(ScheduleDeleteAllProjectAcessService.class);

        List<AsynchronMessageHandler> injectedAsynchronousHandlers = new ArrayList<>();
        injectedAsynchronousHandlers.add(scheduleHandlerToTest);
        List<SynchronMessageHandler> injectedSynchronousHandlers = new ArrayList<>();

        fakeDomainMessageService = new FakeDomainMessageService(injectedSynchronousHandlers, injectedAsynchronousHandlers);

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

    @Test
    public void when_sending_message_id_USER_ADDED_TO_PROJECT_the_addUserToProjectService_is_called() {
        /* prepare */
        DomainMessage request = new DomainMessage(MessageID.USER_ADDED_TO_PROJECT);
        UserMessage content = new UserMessage();
        content.setProjectId("projectId1");
        content.setUserId("userId1");
        request.set(MessageDataKeys.PROJECT_TO_USER_DATA, content);

        /* execute */
        simulateEventSend(request, scheduleHandlerToTest);

        /* test */
        verify(scheduleHandlerToTest.grantService).grantUserAccessToProject("userId1", "projectId1");

    }

    @Test
    public void when_sending_message_id_USER_REMOVED_FROM_PROJECT_the_revokeUserFromProjectService_is_called() {
        /* prepare */
        DomainMessage request = new DomainMessage(MessageID.USER_REMOVED_FROM_PROJECT);
        UserMessage content = new UserMessage();
        content.setProjectId("projectId1");
        content.setUserId("userId1");
        request.set(MessageDataKeys.PROJECT_TO_USER_DATA, content);

        /* execute */
        simulateEventSend(request, scheduleHandlerToTest);

        /* test */
        verify(scheduleHandlerToTest.revokeUserFromProjectService).revokeUserAccessFromProject("userId1", "projectId1");

    }

    @Test
    public void when_sending_message_id_USER_DELETED_the_revokeUserService_is_called() {
        /* prepare */
        DomainMessage request = new DomainMessage(MessageID.USER_DELETED);
        UserMessage content = new UserMessage();
        content.setUserId("userId1");
        request.set(MessageDataKeys.USER_DELETE_DATA, content);

        /* execute */
        simulateEventSend(request, scheduleHandlerToTest);

        /* test */
        verify(scheduleHandlerToTest.revokeUserService).revokeUserAccess("userId1");

    }

    private void simulateEventSend(DomainMessage request, AsynchronMessageHandler handler) {
        fakeDomainMessageService.sendAsynchron(request);
    }

    private class FakeDomainMessageService extends DomainMessageService {

        public FakeDomainMessageService(List<SynchronMessageHandler> injectedSynchronousHandlers, List<AsynchronMessageHandler> injectedAsynchronousHandlers) {
            super(injectedSynchronousHandlers, injectedAsynchronousHandlers);
            this.taskExecutor = new TestTaskExecutor();
            this.eventInspector = new DummyEventInspector();
        }

    }

    private class TestTaskExecutor implements TaskExecutor {

        @Override
        public void execute(Runnable task) {
            task.run();
        }

    }

}
