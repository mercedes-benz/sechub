// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.batch;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.domain.schedule.ExecutionResult;
import com.mercedesbenz.sechub.domain.schedule.SchedulingConstants;
import com.mercedesbenz.sechub.domain.schedule.batch.BatchConfiguration.BatchJobExecutionScope;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKey;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;

class ScanExecutionTasklet implements Tasklet {

    private final BatchJobExecutionScope scope;

    private static final Logger LOG = LoggerFactory.getLogger(ScanExecutionTasklet.class);

    ScanExecutionTasklet(BatchJobExecutionScope batchExecutionScope) {
        this.scope = batchExecutionScope;
    }

    @Override
    @UseCaseSchedulerStartsJob(@Step(number = 3, next = 5, name = "Batch Job", description = "usecases/job/scheduler_starts_job_tasklet.adoc"))
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Long batchJobId = contribution.getStepExecution().getJobExecution().getJobId();

        JobParameters jobParameters = this.scope.getJobExecution().getJobParameters();
        LOG.debug("executing with parameters:{}", jobParameters);

        String secHubJobUUIDAsString = jobParameters.getString(SchedulingConstants.BATCHPARAM_SECHUB_UUID);

        UUID secHubJobUUID = UUID.fromString(secHubJobUUIDAsString);
        ScheduleSecHubJob sechubJob = null;

        try {
            sechubJob = scope.getSecHubJobRepository().getById(secHubJobUUID);

            /* execute sechub job synchron */
            SynchronSecHubJobExecutor trigger = new SynchronSecHubJobExecutor(scope.getEventBusService(), scope.getSecHubJobUpdater());
            trigger.execute(sechubJob, batchJobId);

        } catch (Exception e) {
            LOG.error("Error happend at spring batch task execution:" + e.getMessage(), e);

            markSechHubJobFailed(secHubJobUUID);
            sendJobFailed(secHubJobUUID);

        }
        return RepeatStatus.FINISHED;
    }

    private void markSechHubJobFailed(UUID secHubJobUUID) {
        scope.getSecHubJobUpdater().safeUpdateOfSecHubJob(secHubJobUUID, ExecutionResult.FAILED, null,
                Arrays.asList(new SecHubMessage(SecHubMessageType.ERROR, "Job execution task failed because of an internal error")));

        LOG.info("marked sechub as failed:{}", secHubJobUUID);
    }

    @IsSendingAsyncMessage(MessageID.JOB_FAILED)
    private void sendJobFailed(UUID jobUUID) {
        sendJobInfo(MessageDataKeys.JOB_FAILED_DATA, jobUUID, MessageID.JOB_FAILED);
    }

    private void sendJobInfo(MessageDataKey<JobMessage> key, UUID jobUUID, MessageID id) {
        DomainMessage request = new DomainMessage(id);
        JobMessage message = createMessage(jobUUID);

        request.set(key, message);

        scope.getEventBusService().sendAsynchron(request);
    }

    private JobMessage createMessage(UUID jobUUID) {
        JobMessage message = new JobMessage();
        message.setJobUUID(jobUUID);
        message.setSince(LocalDateTime.now());
        return message;
    }

}