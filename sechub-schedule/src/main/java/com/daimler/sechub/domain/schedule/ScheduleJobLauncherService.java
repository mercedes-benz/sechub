// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.batch.AsyncJobLauncher;
import com.daimler.sechub.domain.schedule.batch.SecHubBatchJobParameterBuilder;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;

/**
 * This service is only responsible of job execution for given
 * {@link ScheduleSecHubJob}
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScheduleJobLauncherService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleJobLauncherService.class);

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    @Autowired
    AsyncJobLauncher jobLauncher;

    @Autowired
    Job job;

    @Autowired
    SecHubBatchJobParameterBuilder parameterBuilder;

    @UseCaseSchedulerStartsJob(@Step(number = 2, next = { 3,
            4 }, name = "Execution", description = "Starts a spring boot batch job which does execute the scan asynchronous. If spring boot batch job cannot be started the next steps will not be executed."))
    public void executeJob(ScheduleSecHubJob secHubJob) {
        UUID secHubJobUUID = secHubJob.getUUID();

        LOG.debug("Execute job:{}", secHubJobUUID);

        try {
            /* prepare batch job */
            JobParameters jobParameters = parameterBuilder.buildParams(secHubJobUUID);

            /* launch batch job */
            LOG.debug("Trigger batch job launch :{}", secHubJobUUID);
            JobExecution execution = jobLauncher.run(job, jobParameters);

            /* job is launched - inspect batch job internal id */
            Long batchJobId = execution.getJobId();
            LOG.debug("Execution triggered: {} has batch-ID:{}", secHubJobUUID, batchJobId);

            /* send domain event */
            sendJobStarted(secHubJob.getProjectId(), secHubJobUUID, secHubJob.getJsonConfiguration(), secHubJob.getOwner());

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            /*
             * we do not need to send a "jobEnded" event, because in this case job was never
             * started
             */
            LOG.error("Not able to run batch job for sechhub :{}", secHubJobUUID);
            throw new ScheduleFailedException(e);
        }

    }

    @IsSendingAsyncMessage(MessageID.JOB_STARTED)
    private void sendJobStarted(String projectId, UUID jobUUID, String configuration, String owner) {
        DomainMessage request = new DomainMessage(MessageID.JOB_STARTED);
        JobMessage message = new JobMessage();
        message.setProjectId(projectId);
        message.setJobUUID(jobUUID);
        message.setConfiguration(configuration);
        message.setOwner(owner);
        message.setSince(LocalDateTime.now());

        request.set(MessageDataKeys.JOB_STARTED_DATA, message);

        eventBus.sendAsynchron(request);
    }

}
