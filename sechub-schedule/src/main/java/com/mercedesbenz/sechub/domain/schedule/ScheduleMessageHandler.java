// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleDeleteAllProjectAcessService;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleGrantUserAccessToProjectService;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleRevokeUserAccessAtAllService;
import com.mercedesbenz.sechub.domain.schedule.access.ScheduleRevokeUserAccessFromProjectService;
import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.config.SchedulerProjectConfigService;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionRotationService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobTransactionService;
import com.mercedesbenz.sechub.domain.schedule.status.SchedulerStatusService;
import com.mercedesbenz.sechub.domain.schedule.whitelist.ProjectWhiteListUpdateService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorChangesProjectAccessLevel;

@Component
public class ScheduleMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleMessageHandler.class);

    @Autowired
    ScheduleGrantUserAccessToProjectService grantService;

    @Autowired
    ScheduleRevokeUserAccessFromProjectService revokeUserFromProjectService;

    @Autowired
    ScheduleRevokeUserAccessAtAllService revokeUserService;

    @Autowired
    ProjectWhiteListUpdateService projectWhiteListUpdateService;

    @Autowired
    SchedulerConfigService configService;

    @Autowired
    SchedulerStatusService statusService;

    @Autowired
    ScheduleDeleteAllProjectAcessService deleteAllProjectAccessService;

    @Autowired
    SchedulerCancelJobService cancelJobService;

    @Autowired
    SchedulerRestartJobService restartJobService;

    @Autowired
    SchedulerProjectConfigService projectConfigService;

    @Autowired
    SecHubJobTransactionService jobTransactionService;

    @Autowired
    ScheduleEncryptionRotationService encryptionRotatonService;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case USER_ADDED_TO_PROJECT:
            handleUserAddedToProject(request);
            break;
        case USER_REMOVED_FROM_PROJECT:
            handleUserRemovedFromProject(request);
            break;
        case USER_DELETED:
            handleUserDeleted(request);
            break;
        case PROJECT_CREATED:
            handleProjectCreated(request);
            break;
        case PROJECT_DELETED:
            handleProjectDeleted(request);
            break;
        case PROJECT_WHITELIST_UPDATED:
            handleProjectWhiteListUpdated(request);
            break;
        case REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING:
            handleDisableSchedulerJobProcessingRequest(request);
            break;
        case REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING:
            handleEnableSchedulerJobProcessingRequest(request);
            break;
        case REQUEST_SCHEDULER_STATUS_UPDATE:
            handleSchedulerStatusRefreshRequest(request);
            break;
        case REQUEST_JOB_CANCELLATION:
            handleCancelJobRequested(request);
            break;
        case REQUEST_JOB_RESTART:
            handleJobRestartRequested(request);
            break;
        case REQUEST_JOB_RESTART_HARD:
            handleJobRestartHardRequested(request);
            break;
        case PROJECT_ACCESS_LEVEL_CHANGED:
            handleProcessAccessLevelChanged(request);
            break;
        case AUTO_CLEANUP_CONFIGURATION_CHANGED:
            handleAutoCleanUpConfigurationChanged(request);
            break;
        case PRODUCT_EXECUTOR_CANCEL_OPERATIONS_DONE:
            handleProductExecutorCancelOperationsDone(request);
            break;
        case START_ENCRYPTION_ROTATION:
            handleEncryptionRotation(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.PRODUCT_EXECUTOR_CANCEL_OPERATIONS_DONE)
    private void handleProductExecutorCancelOperationsDone(DomainMessage request) {
        JobMessage jobCancelData = request.get(MessageDataKeys.JOB_CANCEL_DATA);
        UUID jobUUID = jobCancelData.getJobUUID();
        jobTransactionService.updateExecutionStateInOwnTransaction(jobUUID, ExecutionState.CANCELED);

    }

    @IsReceivingAsyncMessage(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED)
    private void handleAutoCleanUpConfigurationChanged(DomainMessage request) {
        AdministrationConfigMessage message = request.get(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA);
        configService.updateAutoCleanupInDays(message.getAutoCleanupInDays());
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_ACCESS_LEVEL_CHANGED)
    @UseCaseAdministratorChangesProjectAccessLevel(@Step(number = 4, name = "Event handler", description = "Receives change project access level event"))
    private void handleProcessAccessLevelChanged(DomainMessage request) {
        ProjectMessage data = request.get(MessageDataKeys.PROJECT_ACCESS_LEVEL_CHANGE_DATA);

        String projectId = data.getProjectId();
        ProjectAccessLevel formerAccessLevel = data.getFormerAccessLevel();
        ProjectAccessLevel newAccessLevel = data.getNewAccessLevel();

        projectConfigService.changeProjectAccessLevel(projectId, newAccessLevel, formerAccessLevel);
    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_JOB_RESTART)
    private void handleJobRestartRequested(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_RESTART_DATA);
        UUID jobUUID = message.getJobUUID();

        restartJobService.restartJob(jobUUID, message.getOwnerEmailAddress());
    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_JOB_RESTART_HARD)
    private void handleJobRestartHardRequested(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_RESTART_DATA);
        UUID jobUUID = message.getJobUUID();

        restartJobService.restartJobHard(jobUUID, message.getOwnerEmailAddress());
    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_JOB_CANCELLATION)
    private void handleCancelJobRequested(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_CANCEL_DATA);
        cancelJobService.cancelJob(message.getJobUUID(), message.getOwnerEmailAddress());
    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_SCHEDULER_STATUS_UPDATE)
    private void handleSchedulerStatusRefreshRequest(DomainMessage request) {
        statusService.buildStatus();

    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING)
    private void handleEnableSchedulerJobProcessingRequest(DomainMessage request) {
        configService.enableJobProcessing();

    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING)
    private void handleDisableSchedulerJobProcessingRequest(DomainMessage request) {
        configService.disableJobProcessing();
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_CREATED)
    private void handleProjectCreated(DomainMessage request) {
        ProjectMessage data = request.get(MessageDataKeys.PROJECT_CREATION_DATA);
        updateWhiteList(data);
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_WHITELIST_UPDATED)
    private void handleProjectWhiteListUpdated(DomainMessage request) {
        ProjectMessage data = request.get(MessageDataKeys.PROJECT_WHITELIST_UPDATE_DATA);
        updateWhiteList(data);
    }

    @IsReceivingAsyncMessage(MessageID.USER_ADDED_TO_PROJECT)
    private void handleUserAddedToProject(DomainMessage request) {
        UserMessage data = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
        grantService.grantUserAccessToProject(data.getUserId(), data.getProjectId());
    }

    @IsReceivingAsyncMessage(MessageID.USER_REMOVED_FROM_PROJECT)
    private void handleUserRemovedFromProject(DomainMessage request) {
        UserMessage data = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
        revokeUserFromProjectService.revokeUserAccessFromProject(data.getUserId(), data.getProjectId());
    }

    @IsReceivingAsyncMessage(MessageID.USER_DELETED)
    private void handleUserDeleted(DomainMessage request) {
        UserMessage data = request.get(MessageDataKeys.USER_DELETE_DATA);
        revokeUserService.revokeUserAccess(data.getUserId());
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_DELETED)
    private void handleProjectDeleted(DomainMessage request) {
        ProjectMessage data = request.get(MessageDataKeys.PROJECT_DELETE_DATA);

        String projectId = data.getProjectId();

        deleteAllProjectAccessService.deleteAnyAccessDataForProject(projectId);
        projectConfigService.deleteProjectConfiguration(projectId);
    }

    @IsReceivingAsyncMessage(MessageID.START_ENCRYPTION_ROTATION)
    private void handleEncryptionRotation(DomainMessage request) {
        SecHubEncryptionData data = request.get(MessageDataKeys.SECHUB_ENCRYPT_ROTATION_DATA);
        encryptionRotatonService.rotateEncryption(data);
    }

    private void updateWhiteList(ProjectMessage data) {
        projectWhiteListUpdateService.update(data.getProjectId(), data.getWhitelist());
    }

}
