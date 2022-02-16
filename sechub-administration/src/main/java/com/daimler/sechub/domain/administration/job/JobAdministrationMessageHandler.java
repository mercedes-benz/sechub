// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;

@Component
public class JobAdministrationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JobAdministrationMessageHandler.class);

    @Autowired
    JobInformationCreateService createService;

    @Autowired
    JobInformationDeleteService deleteService;

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
        case JOB_CANCELED:
            handleJobCanceled(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.JOB_CANCELED)
    private void handleJobCanceled(DomainMessage request) {
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

}
