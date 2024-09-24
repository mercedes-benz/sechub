// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemHandlesSIGTERM;

@Component
public class JobAdministrationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JobAdministrationMessageHandler.class);

    @Autowired
    JobInformationCreateService createService;

    @Autowired
    JobInformationDeleteService deleteService;

    @Autowired
    AdministrationConfigService configService;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case JOB_STARTED:
            handleJobStarted(request);
            break;
        case JOB_DONE:
            handleJobDone(request);
            break;
        case JOB_FAILED:
            handleJobFailed(request);
            break;
        case JOB_SUSPENDED:
            handleJobSuspended(request);
            break;
        case JOB_CANCELLATION_RUNNING:
            handleJobCancellationRunning(request);
            break;
        case AUTO_CLEANUP_CONFIGURATION_CHANGED:
            handleAutoCleanUpConfigurationChanged(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED)
    private void handleAutoCleanUpConfigurationChanged(DomainMessage request) {
        AdministrationConfigMessage message = request.get(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA);
        configService.updateAutoCleanupInDays(message.getAutoCleanupInDays());
    }

    @IsReceivingAsyncMessage(MessageID.JOB_CANCELLATION_RUNNING)
    private void handleJobCancellationRunning(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_CANCEL_DATA);
        // we do drop job info - we only hold running and waiting jobs!
        deleteService.delete(message.getJobUUID());
    }

    @IsReceivingAsyncMessage(MessageID.JOB_STARTED)
    private void handleJobStarted(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_STARTED_DATA);
        createService.createByMessage(message, JobStatus.RUNNING);
    }

    @IsReceivingAsyncMessage(MessageID.JOB_DONE)
    private void handleJobDone(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_DONE_DATA);
        deleteService.delete(message.getJobUUID());
    }

    @IsReceivingAsyncMessage(MessageID.JOB_FAILED)
    private void handleJobFailed(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_FAILED_DATA);
        deleteService.delete(message.getJobUUID());
    }

    @IsReceivingAsyncMessage(MessageID.JOB_SUSPENDED)
    @UseCaseSystemHandlesSIGTERM(@Step(number = 7, name = "Administration handles suspension", description = "Administration removes suspended  listeners about job suspension"))
    private void handleJobSuspended(DomainMessage request) {
        JobMessage message = request.get(MessageDataKeys.JOB_SUSPENDED_DATA);
        // we do drop job info - we only hold running and waiting jobs. The suspended
        // job will be restarted and appear later again.
        deleteService.delete(message.getJobUUID());
    }

}
