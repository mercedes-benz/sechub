// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import com.mercedesbenz.sechub.domain.scan.access.ScanDeleteAnyAccessToProjectAtAllService;
import com.mercedesbenz.sechub.domain.scan.access.ScanGrantUserAccessToProjectService;
import com.mercedesbenz.sechub.domain.scan.access.ScanRevokeUserAccessAtAllService;
import com.mercedesbenz.sechub.domain.scan.access.ScanRevokeUserAccessFromProjectService;
import com.mercedesbenz.sechub.domain.scan.config.ScanConfigService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigAccessLevelService;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DummyEventInspector;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

/**
 * The test does not only test the message handler but also the domain message
 * service recognition of asynchronous event resolving by annotations.
 *
 * @author Albert Tregnaghi
 *
 */
class ScanMessageHandlerTest {

    private ScanMessageHandler messageHandlerToTest;
    private FakeDomainMessageService fakeDomainMessageService;

    @BeforeEach
    void before() {
        messageHandlerToTest = new ScanMessageHandler();

        messageHandlerToTest.grantService = mock(ScanGrantUserAccessToProjectService.class);
        messageHandlerToTest.revokeUserFromProjectService = mock(ScanRevokeUserAccessFromProjectService.class);
        messageHandlerToTest.revokeUserService = mock(ScanRevokeUserAccessAtAllService.class);
        messageHandlerToTest.deleteAllProjectAccessService = mock(ScanDeleteAnyAccessToProjectAtAllService.class);
        messageHandlerToTest.projectDataDeleteService = mock(ProjectDataDeleteService.class);
        messageHandlerToTest.projectAccessLevelService = mock(ScanProjectConfigAccessLevelService.class);
        messageHandlerToTest.configService = mock(ScanConfigService.class);

        List<AsynchronMessageHandler> injectedAsynchronousHandlers = new ArrayList<>();
        injectedAsynchronousHandlers.add(messageHandlerToTest);
        List<SynchronMessageHandler> injectedSynchronousHandlers = new ArrayList<>();

        fakeDomainMessageService = new FakeDomainMessageService(injectedSynchronousHandlers, injectedAsynchronousHandlers);

    }

    @Test
    void handler_receiving_auto_cleanup_calls_config_serice_with_message_data() {
        /* prepare */
        long days = System.nanoTime();
        AdministrationConfigMessage configMessage = new AdministrationConfigMessage();
        configMessage.setAutoCleanupInDays(days);
        DomainMessage message = new DomainMessage(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED);

        message.set(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA, configMessage);

        /* execute */
        messageHandlerToTest.receiveAsyncMessage(message);

        /* test */
        verify(messageHandlerToTest.configService).updateAutoCleanupInDays(days);
    }

    @Test
    void when_sending_message_id_PROJECT_ACCESS_LEVEL_CHANGED_changeProjectAccessLevel_is_called() {
        /* prepare */
        ProjectAccessLevel newAccessLevel = ProjectAccessLevel.NONE;
        ProjectAccessLevel formerAccessLevel = ProjectAccessLevel.READ_ONLY;

        DomainMessage request = new DomainMessage(MessageID.PROJECT_ACCESS_LEVEL_CHANGED);
        ProjectMessage content = new ProjectMessage();
        content.setProjectId("projectId1");
        content.setFormerAccessLevel(formerAccessLevel);
        content.setNewAccessLevel(newAccessLevel);

        request.set(MessageDataKeys.PROJECT_ACCESS_LEVEL_CHANGE_DATA, content);

        /* execute */
        simulateEventSend(request, messageHandlerToTest);

        /* test */
        verify(messageHandlerToTest.projectAccessLevelService).changeProjectAccessLevel("projectId1", newAccessLevel, formerAccessLevel);

    }

    @Test
    void when_sending_message_id_PROJECT_DELETED_the_deleteAllDataForProject_is_called() {
        /* prepare */
        DomainMessage request = new DomainMessage(MessageID.PROJECT_DELETED);
        ProjectMessage content = new ProjectMessage();
        content.setProjectId("projectId1");
        request.set(MessageDataKeys.PROJECT_DELETE_DATA, content);

        /* execute */
        simulateEventSend(request, messageHandlerToTest);

        /* test */
        verify(messageHandlerToTest.projectDataDeleteService).deleteAllDataForProject("projectId1");

    }

    @Test
    void when_sending_message_id_PROJECT_DELETED_the_deleteAllProjectAccessService_is_called() {
        /* prepare */
        DomainMessage request = new DomainMessage(MessageID.PROJECT_DELETED);
        ProjectMessage content = new ProjectMessage();
        content.setProjectId("projectId1");
        request.set(MessageDataKeys.PROJECT_DELETE_DATA, content);

        /* execute */
        simulateEventSend(request, messageHandlerToTest);

        /* test */
        verify(messageHandlerToTest.deleteAllProjectAccessService).deleteAnyAccessDataForProject("projectId1");

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
