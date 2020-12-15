// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.batch.SchedulerCancelBatchJobService;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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

    @Autowired
    JobExplorer explorer;

    @Autowired
    JobOperator operator;

    @Autowired
    SchedulerCancelBatchJobService cancelBatchJobService;
    
    /**
     * This service will cancel given JOB. There is NO check if current user has
     * access - this must be done before.
     * 
     * @param jobUUID
     * @param ownerEmailAddress
     */
    @UseCaseAdminCancelsJob(@Step(number = 3, name = "Try to find job and mark as being canceled", description = "When job is found and user has access the state will be updated and marked as canceled"))
    public void cancelJob(UUID jobUUID, String ownerEmailAddress) {
        assertion.isValidJobUUID(jobUUID);

        Optional<ScheduleSecHubJob> optJob = jobRepository.findById(jobUUID);
        if (!optJob.isPresent()) {
            LOG.warn("Job {} not present, so not able to cancel!", jobUUID);
            return;
        }
        ScheduleSecHubJob secHubJob = optJob.get();

        cancelBatchJobService.stopAllRunningBatchJobsForSechubJobUUID(jobUUID);
        markJobAsCanceled(secHubJob);

        LOG.info("job {} has been canceled", jobUUID);

        sendJobCanceled(secHubJob, ownerEmailAddress);

    }

    private void markJobAsCanceled(ScheduleSecHubJob secHubJob) {
        ExecutionState state = secHubJob.getExecutionState();
        if (ExecutionState.ENDED.equals(state)) {
            throw new NotAcceptableException("Not able to cancel because job has already ended!");
        }
        if (!ExecutionResult.NONE.equals(secHubJob.getExecutionResult())) {
            throw new NotAcceptableException("Not able to cancel because job has already a result:" + secHubJob.getExecutionResult());
        }
        secHubJob.setExecutionState(ExecutionState.CANCEL_REQUESTED);
        secHubJob.setExecutionResult(ExecutionResult.FAILED); // we mark job as failed, because canceled and so did not work. So we need no
                                                              // special result like "CANCELED". Is simply did not work, reason can be found
                                                              // in execution state
        secHubJob.setEnded(LocalDateTime.now());
        jobRepository.save(secHubJob);
    }

    @IsSendingAsyncMessage(MessageID.JOB_CANCELED)
    private void sendJobCanceled(ScheduleSecHubJob secHubJob, String ownerEmailAddress) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.JOB_CANCELED);

        JobMessage message = new JobMessage();
        message.setJobUUID(secHubJob.getUUID());
        
        message.setProjectId(secHubJob.getProjectId());
        message.setOwner(secHubJob.getOwner());
        message.setOwnerEmailAddress(ownerEmailAddress);

        request.set(MessageDataKeys.JOB_CANCEL_DATA, message);

        eventBus.sendAsynchron(request);

    }

}
