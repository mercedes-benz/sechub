// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SchedulerMessage;

@Service
public class SchedulerStatusService {

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    SchedulerConfigService configService;

    @IsSendingAsyncMessage(MessageID.SCHEDULER_STATUS_UPDATE)
    public void buildStatus() {
        DomainMessage message = DomainMessageFactory.createEmptyRequest(MessageID.SCHEDULER_STATUS_UPDATE);
        SchedulerMessage sm = new SchedulerMessage();

        long amountOfJobsAll = jobRepository.count();

        long amountOfInitializingJobs = jobRepository.countJobsInExecutionState(ExecutionState.INITIALIZING);
        long amountOfJobsReadyToStart = jobRepository.countJobsInExecutionState(ExecutionState.READY_TO_START);
        long amountOfJobsStarted = jobRepository.countJobsInExecutionState(ExecutionState.STARTED);
        long amountOfJobsCancelRequested = jobRepository.countJobsInExecutionState(ExecutionState.CANCEL_REQUESTED);
        long amountOfJobsCanceled = jobRepository.countJobsInExecutionState(ExecutionState.CANCELED);
        long amountOfJobsSuspended = jobRepository.countJobsInExecutionState(ExecutionState.SUSPENDED);
        long amountOfJobsEnded = jobRepository.countJobsInExecutionState(ExecutionState.ENDED);

        boolean processingEnabled = configService.isJobProcessingEnabled();

        sm.setJobProcessingEnabled(processingEnabled);

        sm.setAmountOfAllJobs(amountOfJobsAll);

        sm.setAmountOfInitializingJobs(amountOfInitializingJobs);
        sm.setAmountOfJobsReadyToStart(amountOfJobsReadyToStart);
        sm.setAmountOfJobsStarted(amountOfJobsStarted);
        sm.setAmountOfJobsCancelRequested(amountOfJobsCancelRequested);
        sm.setAmountOfJobsCanceled(amountOfJobsCanceled);
        sm.setAmountOfJobsEnded(amountOfJobsEnded);
        sm.setAmountOfJobsSuspended(amountOfJobsSuspended);

        message.set(MessageDataKeys.SCHEDULER_STATUS_DATA, sm);

        eventBus.sendAsynchron(message);
    }

}
