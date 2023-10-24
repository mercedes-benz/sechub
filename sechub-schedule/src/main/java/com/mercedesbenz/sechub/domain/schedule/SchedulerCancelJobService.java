// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJobMessagesSupport;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerCancelJobService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerCancelJobService.class);

    @Autowired
    private SecHubJobRepository jobRepository;

    @Autowired
    ScheduleAssertService assertService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    private ScheduleSecHubJobMessagesSupport jobMessageSupport = new ScheduleSecHubJobMessagesSupport();

    /**
     * This service will cancel given JOB. There is NO check if current user has
     * access - this must be done before.
     *
     * @param jobUUID
     * @param ownerEmailAddress
     */
    @UseCaseAdminCancelsJob(@Step(number = 3, name = "Try to find job and mark as being canceled", description = "When job is found and user has access the state will be updated and marked as canceled"))
    public void cancelJob(UUID jobUUID, String ownerEmailAddress) {
        assertion.assertIsValidJobUUID(jobUUID);

        Optional<ScheduleSecHubJob> optJob = jobRepository.findById(jobUUID);
        if (!optJob.isPresent()) {
            LOG.warn("Job {} not present, so not able to cancel!", jobUUID);
            return;
        }
        ScheduleSecHubJob secHubJob = optJob.get();

        markJobAsCanceledRequested(secHubJob);

        sendJobCancellationRunning(secHubJob, ownerEmailAddress);

    }

    private void markJobAsCanceledRequested(ScheduleSecHubJob secHubJob) {
        ExecutionState state = secHubJob.getExecutionState();
        if (ExecutionState.ENDED.equals(state)) {
            throw new NotAcceptableException("Not able to cancel because job has already ended!");
        }
        /*
         * Only when execution result is NONE the further execution is possible -
         * otherwise the job has been already processed (done/failed or canceled)
         */
        if (!ExecutionResult.NONE.equals(secHubJob.getExecutionResult())) {
            throw new NotAcceptableException("Not able to cancel because job has already a result:" + secHubJob.getExecutionResult());
        }

        /*
         * se the job in a state where the processing from client side can break very
         * fast
         */
        secHubJob.setExecutionState(ExecutionState.CANCEL_REQUESTED);
        secHubJob.setExecutionResult(ExecutionResult.FAILED); // we mark job as failed, because canceled and so did not work. So we need no
                                                              // special execution result like "CANCELED". The job simply did not work and the
                                                              // reason can be found in execution state
        secHubJob.setEnded(LocalDateTime.now());

        /* add message about the cancellation - so available in status and report */
        jobMessageSupport.addMessages(secHubJob, Arrays.asList(new SecHubMessage(SecHubMessageType.INFO, "Job execution was canceled by user")));

        jobRepository.save(secHubJob);

        LOG.info("Persisted SecHub job {} with sexecution state: {} and result: {}", secHubJob.getUUID(), secHubJob.getExecutionState(),
                secHubJob.getExecutionResult());
    }

    @IsSendingAsyncMessage(MessageID.JOB_CANCELLATION_RUNNING)
    private void sendJobCancellationRunning(ScheduleSecHubJob secHubJob, String ownerEmailAddress) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.JOB_CANCELLATION_RUNNING);

        JobMessage message = new JobMessage();
        message.setJobUUID(secHubJob.getUUID());

        message.setProjectId(secHubJob.getProjectId());
        message.setOwner(secHubJob.getOwner());
        message.setOwnerEmailAddress(ownerEmailAddress);

        request.set(MessageDataKeys.JOB_CANCEL_DATA, message);

        eventBus.sendAsynchron(request);

    }

}
