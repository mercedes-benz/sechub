// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.scheduler;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.administration.status.StatusEntry;
import com.mercedesbenz.sechub.domain.administration.status.StatusEntryRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SchedulerMessage;

@Component
public class SchedulerAdministrationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerAdministrationMessageHandler.class);

    @Autowired
    StatusEntryRepository repository;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case SCHEDULER_STATUS_UPDATE:
            handleSchedulerStatusChange(request);
            break;
        case SCHEDULER_JOB_PROCESSING_DISABLED:
            handleSchedulerJobProcessingDisabled(request);
            break;
        case SCHEDULER_JOB_PROCESSING_ENABLED:
            handleSchedulerJobProcessingEnabled(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_ENABLED)
    private void handleSchedulerJobProcessingEnabled(DomainMessage request) {
        updateSchedulerJobProcessingEnabled(true);
    }

    @IsReceivingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_DISABLED)
    private void handleSchedulerJobProcessingDisabled(DomainMessage request) {
        updateSchedulerJobProcessingEnabled(false);
    }

    @IsReceivingAsyncMessage(MessageID.SCHEDULER_STATUS_UPDATE)
    private void handleSchedulerStatusChange(DomainMessage request) {
        SchedulerMessage status = request.get(MessageDataKeys.SCHEDULER_STATUS_DATA);

        updateSchedulerJobProcessingEnabled(status.isJobProcessingEnabled());

        updateSchedulerJobInformation(status);

    }

    private void updateSchedulerJobProcessingEnabled(boolean processingEnabled) {
        StatusEntry enabled = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_ENABLED);
        enabled.setValue(Boolean.toString(processingEnabled));
        repository.save(enabled);
    }

    private void updateSchedulerJobInformation(SchedulerMessage status) {
        StatusEntry jobsAll = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_ALL);
        jobsAll.setValue(Long.toString(status.getAmountOfAllJobs()));

        StatusEntry jobsRunning = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_RUNNING);
        jobsRunning.setValue(Long.toString(status.getAmountOfRunningJobs()));

        StatusEntry jobsWaiting = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_WAITING);
        jobsWaiting.setValue(Long.toString(status.getAmountOfWaitingJobs()));

        /* persist */
        repository.save(jobsAll);
        repository.save(jobsRunning);
        repository.save(jobsWaiting);
    }

    private StatusEntry fetchOrCreateEntry(SchedulerStatusEntryKeys key) {
        Optional<StatusEntry> optional = repository.findByStatusEntryKey(key);
        if (optional.isPresent()) {
            return optional.get();
        }
        StatusEntry entry = new StatusEntry(key);
        return entry;

    }
}
