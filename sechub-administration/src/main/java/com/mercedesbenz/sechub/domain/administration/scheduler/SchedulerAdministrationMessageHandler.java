// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.scheduler;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.administration.project.ProjectTemplateService;
import com.mercedesbenz.sechub.domain.administration.status.StatusEntry;
import com.mercedesbenz.sechub.domain.administration.status.StatusEntryRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SchedulerMessage;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;

@Component
public class SchedulerAdministrationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerAdministrationMessageHandler.class);

    @Autowired
    StatusEntryRepository repository;

    @Autowired
    @Lazy
    ProjectTemplateService projectTemplateService;

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
        case TEMPLATE_DELETED:
            handleTemplateDeleted(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.TEMPLATE_DELETED)
    private void handleTemplateDeleted(DomainMessage request) {
        SecHubProjectToTemplate projectToTemplate = request.get(MessageDataKeys.PROJECT_TO_TEMPLATE);
        projectTemplateService.unassignTemplateFromAllProjects(projectToTemplate.getTemplateId());
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
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_ENABLED, processingEnabled);
    }

    private void updateSchedulerJobInformation(SchedulerMessage status) {
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_ALL, status.getAmountOfAllJobs());

        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_INITIALIZING, status.getAmountOfInitializingJobs());
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_READY_TO_START, status.getAmountOfJobsReadyToStart());
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_STARTED, status.getAmountOfJobsStarted());
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_CANCEL_REQUESTED, status.getAmountOfJobsCancelRequested());
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_CANCELED, status.getAmountOfJobsCanceled());
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_SUSPENDED, status.getAmountOfJobsSuspended());
        saveStatusEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_ENDED, status.getAmountOfJobsEnded());

    }

    private void saveStatusEntry(SchedulerStatusEntryKeys key, long value) {
        saveStatusEntry(key, Long.toString(value));
    }

    private void saveStatusEntry(SchedulerStatusEntryKeys key, boolean value) {
        saveStatusEntry(key, Boolean.toString(value));
    }

    private void saveStatusEntry(SchedulerStatusEntryKeys key, String value) {
        StatusEntry statusEntry = fetchOrCreateEntry(key);
        statusEntry.setValue(value);

        repository.save(statusEntry);
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
