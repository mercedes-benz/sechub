// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.notification;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.notification.owner.InformOwnerThatProjectHasBeenDeletedNotificationService;
import com.mercedesbenz.sechub.domain.notification.owner.InformThatProjectHasNewOwnerNotificationService;
import com.mercedesbenz.sechub.domain.notification.superadmin.*;
import com.mercedesbenz.sechub.domain.notification.superadmin.InformAdminsThatNewSchedulerInstanceHasBeenStartedNotificationService;
import com.mercedesbenz.sechub.domain.notification.user.*;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.ClusterMemberMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;

@Component
public class NotificationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationMessageHandler.class);

    @Autowired
    UserDeletedNotificationService userDeletedNotificationService;

    @Autowired
    UserEmailAddressChangedNotificationService userEmailAddressChangedNotificationService;

    @Autowired
    NewAPITokenAppliedUserNotificationService newAPITokenAppliedUserNotificationService;

    @Autowired
    NewApiTokenRequestedUserNotificationService newApiTokenRequestedUserNotificationService;

    @Autowired
    SignUpRequestedAdminNotificationService signupRequestedAdminNotificationService;

    @Autowired
    InformUserThatUserBecomesAdminNotificationService informUserThatUserBecomesAdminNotificationService;

    @Autowired
    InformAdminsThatUserBecomesAdminNotificationService informAdminsThatUserBecomesAdminNotificationService;

    @Autowired
    InformUserThatUserNoLongerAdminNotificationService informUserThatUserNoLongerAdminNotificationService;

    @Autowired
    InformAdminsThatUserNoLongerAdminNotificationService informAdminsThatUserNoLongerAdminNotificationService;

    @Autowired
    InformAdminsThatSchedulerJobProcessingHasBeenDisabledService informAdminSchedulerDisabledService;

    @Autowired
    InformAdminsThatSchedulerJobProcessingHasBeenEnabledService informAdminSchedulerEnabledService;

    @Autowired
    InformAdminsThatJobResultsHaveBeenPurgedService informAdminsThatJobResultsHaveBeenPurgedService;

    /* +++++++++++++++++++++++++++++++++ */
    /* ++++++ job canceled +++++++++++++ */
    /* +++++++++++++++++++++++++++++++++ */
    @Autowired
    InformUserThatJobHasBeenCanceledService informUserThatJobHasBeenCanceledService;

    /* ++++++++++++++++++++++++++++++++ */
    /* ++++++ job restart +++++++++++++ */
    /* ++++++++++++++++++++++++++++++++ */
    @Autowired
    InformAdminsThatJobRestartWasCanceledService informAdminsThatRestartWasCanceledService;

    @Autowired
    InformAdminsThatJobRestartHasBeenTriggeredService informAdminsThatJobRestartHasBeenTriggeredService;

    /* +++++++++++++++++++++++++++++++++ */
    /* ++++++ project delete +++++++++++ */
    /* +++++++++++++++++++++++++++++++++ */
    @Autowired
    InformAdminsThatProjectHasBeenDeletedNotificationService informAdminsThatProjectHasBeenDeletedService;

    @Autowired
    InformThatProjectHasNewOwnerNotificationService informThatProjectHasNewOwnerService;

    @Autowired
    InformOwnerThatProjectHasBeenDeletedNotificationService informOwnerThatProjectHasBeenDeletedService;

    @Autowired
    InformUsersThatProjectHasBeenDeletedNotificationService informUsersThatProjectHasBeenDeletedService;

    @Autowired
    InformAdminsThatNewSchedulerInstanceHasBeenStartedNotificationService informAdminsThatNewSchedulerInstanceHasBeenStartedNotificationService;

    @Autowired
    SignUpRequestedUserNotificationService signupRequestedUserNotificationService;

    @Autowired
    UserEmailAddressChangeRequestNotificationService userEmailAddressChangeRequestNotificationService;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();

        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case USER_NEW_API_TOKEN_REQUESTED:
            handlNewAPITokenRequested(request.get(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO));
            break;
        case USER_API_TOKEN_CHANGED:
            handleUserApiTokenChanged(request.get(MessageDataKeys.USER_API_TOKEN_DATA));
            break;
        case USER_DELETED:
            handleUserDeleted(request.get(MessageDataKeys.USER_DELETE_DATA));
            break;
        case USER_SIGNUP_REQUESTED:
            handleSignupRequested(request.get(MessageDataKeys.USER_SIGNUP_DATA));
            break;
        case USER_BECOMES_SUPERADMIN:
            handleUserBecomesSuperAdmin(request.get(MessageDataKeys.USER_CONTACT_DATA), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case USER_NO_LONGER_SUPERADMIN:
            handleUserNoLongerSuperAdmin(request.get(MessageDataKeys.USER_CONTACT_DATA), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case SCHEDULER_JOB_PROCESSING_DISABLED:
            handleSchedulerJobProcessingDisabled(request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case SCHEDULER_JOB_PROCESSING_ENABLED:
            handleSchedulerJobProcessingEnabled(request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case PROJECT_DELETED:
            handleProjectDeleted(request.get(MessageDataKeys.PROJECT_DELETE_DATA), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case JOB_CANCELLATION_RUNNING:
            handleJobCancellationRunning(request.get(MessageDataKeys.JOB_CANCEL_DATA));
            break;
        case JOB_RESTART_CANCELED:
            handleRestartJobCanceled(request.get(MessageDataKeys.JOB_RESTART_DATA), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case JOB_RESTART_TRIGGERED:
            handleRestartJobTriggered(request.get(MessageDataKeys.JOB_RESTART_DATA), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case JOB_RESULTS_PURGED:
            handleJobResultsPurged(request.get(MessageDataKeys.SECHUB_JOB_UUID), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case SCHEDULER_STARTED:
            handleSchedulerStarted(request.get(MessageDataKeys.ENVIRONMENT_CLUSTER_MEMBER_STATUS), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case PROJECT_OWNER_CHANGED:
            handleOwnerChanged(request.get(MessageDataKeys.PROJECT_OWNER_CHANGE_DATA), request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
            break;
        case USER_EMAIL_ADDRESS_CHANGED:
            handleUserEmailChanged(request.get(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA));
            break;
        case USER_EMAIL_ADDRESS_CHANGE_REQUEST:
            handleUserEmailChangeRequest(request.get(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA));
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.USER_EMAIL_ADDRESS_CHANGED)
    private void handleUserEmailChanged(UserMessage userMessage) {
        userEmailAddressChangedNotificationService.notify(userMessage);
    }

    @IsReceivingAsyncMessage(MessageID.USER_EMAIL_ADDRESS_CHANGE_REQUEST)
    private void handleUserEmailChangeRequest(UserMessage userMessage) {
        userEmailAddressChangeRequestNotificationService.notify(userMessage);
    }

    @IsReceivingAsyncMessage(MessageID.SCHEDULER_STARTED)
    private void handleSchedulerStarted(ClusterMemberMessage clusterMemberMessage, String baseUrl) {
        informAdminsThatNewSchedulerInstanceHasBeenStartedNotificationService.notify(baseUrl, clusterMemberMessage);
    }

    @IsReceivingAsyncMessage(MessageID.JOB_RESULTS_PURGED)
    private void handleJobResultsPurged(UUID uuid, String baseUrl) {
        informAdminsThatJobResultsHaveBeenPurgedService.notify(uuid, baseUrl);

    }

    @IsReceivingAsyncMessage(MessageID.JOB_RESTART_TRIGGERED)
    private void handleRestartJobTriggered(JobMessage jobMessage, String baseUrl) {
        informAdminsThatJobRestartHasBeenTriggeredService.notify(jobMessage, baseUrl);
    }

    @IsReceivingAsyncMessage(MessageID.JOB_RESTART_CANCELED)
    private void handleRestartJobCanceled(JobMessage jobMessage, String baseURL) {
        informAdminsThatRestartWasCanceledService.notify(jobMessage, baseURL);

    }

    @IsReceivingAsyncMessage(MessageID.JOB_CANCELLATION_RUNNING)
    private void handleJobCancellationRunning(JobMessage jobMessage) {
        informUserThatJobHasBeenCanceledService.notify(jobMessage);
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_OWNER_CHANGED)
    private void handleOwnerChanged(ProjectMessage projectMessage, String baseUrl) {
        informThatProjectHasNewOwnerService.notify(projectMessage, baseUrl);
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_DELETED)
    private void handleProjectDeleted(ProjectMessage projectMessage, String baseUrl) {
        informAdminsThatProjectHasBeenDeletedService.notify(projectMessage, baseUrl);
        informOwnerThatProjectHasBeenDeletedService.notify(projectMessage, baseUrl);
        informUsersThatProjectHasBeenDeletedService.notify(projectMessage, baseUrl);
    }

    @IsReceivingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_DISABLED)
    private void handleSchedulerJobProcessingDisabled(String envBaseUrl) {
        informAdminSchedulerDisabledService.notify(envBaseUrl);
    }

    @IsReceivingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_ENABLED)
    private void handleSchedulerJobProcessingEnabled(String envBaseUrl) {
        informAdminSchedulerEnabledService.notify(envBaseUrl);
    }

    @IsReceivingAsyncMessage(MessageID.USER_NO_LONGER_SUPERADMIN)
    private void handleUserNoLongerSuperAdmin(UserMessage userMessage, String baseUrl) {
        informUserThatUserNoLongerAdminNotificationService.notify(userMessage, baseUrl);
        informAdminsThatUserNoLongerAdminNotificationService.notify(userMessage, baseUrl);
    }

    @IsReceivingAsyncMessage(MessageID.USER_BECOMES_SUPERADMIN)
    private void handleUserBecomesSuperAdmin(UserMessage userMessage, String baseURL) {
        informUserThatUserBecomesAdminNotificationService.notify(userMessage, baseURL);
        informAdminsThatUserBecomesAdminNotificationService.notify(userMessage, baseURL);
    }

    @IsReceivingAsyncMessage(MessageID.USER_DELETED)
    private void handleUserDeleted(UserMessage userMessage) {
        userDeletedNotificationService.notify(userMessage);
    }

    @IsReceivingAsyncMessage(MessageID.USER_API_TOKEN_CHANGED)
    private void handleUserApiTokenChanged(UserMessage userMessage) {
        newAPITokenAppliedUserNotificationService.notify(userMessage);
    }

    @IsReceivingAsyncMessage(MessageID.USER_NEW_API_TOKEN_REQUESTED)
    private void handlNewAPITokenRequested(UserMessage userMessage) {
        newApiTokenRequestedUserNotificationService.notify(userMessage);
    }

    @IsReceivingAsyncMessage(MessageID.USER_SIGNUP_REQUESTED)
    private void handleSignupRequested(UserMessage userMessage) {
        signupRequestedUserNotificationService.notify(userMessage);
        signupRequestedAdminNotificationService.notify(userMessage);
    }

}
