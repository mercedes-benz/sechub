// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerRestartJobService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerRestartJobService.class);

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
    ScheduleJobLauncherService launcherService;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    SecHubEnvironment sechubEnvironment;

    @Autowired
    ScheduleAssertService scheduleAssertService;

    /**
     * This service will restart given JOB. There is NO check if current user has
     * access - this must be done before.
     *
     * @param jobUUID
     * @param ownerEmailAddress
     */
    @UseCaseAdminRestartsJobHard(@Step(number = 3, name = "Try to rstart job (hard)", description = "When job is found, a restart will be triggered. Existing batch jobs will be terminated"))
    public void restartJobHard(UUID jobUUID, String ownerEmailAddress) {
        restartJob(jobUUID, ownerEmailAddress, true);
    }

    /**
     * This service will restart given JOB. There is NO check if current user has
     * access - this must be done before.
     *
     * @param jobUUID
     * @param ownerEmailAddress
     */
    @UseCaseAdminRestartsJobHard(@Step(number = 3, name = "Try to restart job", description = "When job is found and job is not already finsihed, a restart will be triggered. Existing batch jobs will be terminated"))
    public void restartJob(UUID jobUUID, String ownerEmailAddress) {
        restartJob(jobUUID, ownerEmailAddress, false);
    }

    private void restartJob(UUID jobUUID, String ownerEmailAddress, boolean hard) {
        assertion.assertIsValidJobUUID(jobUUID);

        auditLogService.log("triggered restart of job:{}, variant:[hard={}]", jobUUID, hard);

        Optional<ScheduleSecHubJob> optJob = jobRepository.findById(jobUUID);
        if (!optJob.isPresent()) {
            LOG.warn("SecHub job {} not found, so not able to restart!", jobUUID);

            JobDataContext context = new JobDataContext();
            context.sechubJobUUID = jobUUID;
            context.ownerEmailAddress = ownerEmailAddress;
            context.info = "Restart canceled, because job not found!";

            sendJobRestartCanceled(context);
            throw new NotFoundException("Job not found or you have no access");
        }
        /* job exists, so can be restarted - hard or soft */
        ScheduleSecHubJob job = optJob.get();
        if (job.getExecutionResult().hasFinished()) {
            /* already done so just ignore */
            LOG.warn("SecHub job {} has already finished, so not able to restart!", jobUUID);
            sendJobRestartCanceled(job, ownerEmailAddress, "Restart canceled, because job already finished");
            throw new AlreadyExistsException("Job has already finished - restart not necessary");
        }

        if (hard) {
            sendPurgeJobResultsSynchronousRequest(job);
        }

        ScheduleSecHubJob secHubJob = optJob.get();
        markJobAsNewExecutedNow(secHubJob);

        sendJobRestartTriggered(secHubJob, ownerEmailAddress);
        launcherService.executeJob(secHubJob);
        String type = (hard ? "hard" : "normal");
        LOG.info("job {} has been {} restarted", jobUUID, type);

    }

    private void markJobAsNewExecutedNow(ScheduleSecHubJob secHubJob) {
        secHubJob.setExecutionState(ExecutionState.STARTED);
        secHubJob.setExecutionResult(ExecutionResult.NONE);
        secHubJob.setTrafficLight(null);
        secHubJob.setStarted(LocalDateTime.now());
        secHubJob.setEnded(null);
        jobRepository.save(secHubJob);
    }

    @IsSendingAsyncMessage(MessageID.JOB_RESTART_TRIGGERED)
    private void sendJobRestartTriggered(ScheduleSecHubJob secHubJob, String ownerEmailAddress) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.JOB_RESTART_TRIGGERED);

        JobMessage message = new JobMessage();
        message.setJobUUID(secHubJob.getUUID());
        message.setProjectId(secHubJob.getProjectId());
        message.setOwner(secHubJob.getOwner());
        message.setOwnerEmailAddress(ownerEmailAddress);

        request.set(MessageDataKeys.JOB_RESTART_DATA, message);
        request.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());
        eventBus.sendAsynchron(request);

    }

    @IsSendingSyncMessage(MessageID.REQUEST_PURGE_JOB_RESULTS)
    private void sendPurgeJobResultsSynchronousRequest(ScheduleSecHubJob secHubJob) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_PURGE_JOB_RESULTS);

        request.set(MessageDataKeys.SECHUB_JOB_UUID, secHubJob.getUUID());
        request.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());
        DomainMessageSynchronousResult result = eventBus.sendSynchron(request);
        if (result.hasFailed()) {
            throw new SecHubRuntimeException("Purge failed!");
        }

    }

    private void sendJobRestartCanceled(ScheduleSecHubJob secHubJob, String ownerEmailAddress, String cancelReason) {
        JobDataContext context = new JobDataContext();
        context.sechubJobUUID = secHubJob.getUUID();
        context.projectId = secHubJob.getProjectId();
        context.owner = secHubJob.getOwner();

        sendJobRestartCanceled(context);

    }

    @IsSendingAsyncMessage(MessageID.JOB_RESTART_CANCELED)
    private void sendJobRestartCanceled(JobDataContext context) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.JOB_RESTART_CANCELED);

        JobMessage message = new JobMessage();
        message.setJobUUID(context.sechubJobUUID);
        message.setProjectId(context.projectId);
        message.setOwner(context.owner);
        message.setOwnerEmailAddress(context.ownerEmailAddress);
        message.setInfo(context.info);

        request.set(MessageDataKeys.JOB_RESTART_DATA, message);
        request.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());
        eventBus.sendAsynchron(request);
    }

    private class JobDataContext {
        private UUID sechubJobUUID;
        private String projectId;
        private String owner;
        private String info;
        private String ownerEmailAddress;
    }

}
