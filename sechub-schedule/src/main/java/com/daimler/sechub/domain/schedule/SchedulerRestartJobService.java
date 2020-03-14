// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.LogConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJobHard;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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

	
	/**
	 * This service will restart given JOB. There is NO check if current user has access - this must be done before.
	 * @param jobUUID
	 * @param ownerEmailAddress
	 */
	@UseCaseAdministratorRestartsJobHard(@Step(number = 3, name = "Try to find job and mark as being canceled", description = "When job is found and user has access the state will be updated and marked as canceled"))
	public void restartJob(UUID jobUUID, String ownerEmailAddress) {
	    assertion.isValidJobUUID(jobUUID);

	    Optional<ScheduleSecHubJob> optJob = jobRepository.findById(jobUUID);
        if (! optJob.isPresent()) {
            LOG.warn("Job {} not present, so not able to restart!",jobUUID);
            return;
        }
        MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, jobUUID.toString());

        ScheduleSecHubJob secHubJob = optJob.get();
        markJobAsNewExecutedNow(secHubJob);
        
        launcherService.executeJob(secHubJob);
        LOG.info("job {} has been hard restarted", jobUUID);
        
        sendJobRestarted(secHubJob,ownerEmailAddress);
    }
	

	private void markJobAsNewExecutedNow(ScheduleSecHubJob secHubJob) {
		secHubJob.setExecutionState(ExecutionState.STARTED);
		secHubJob.setExecutionResult(ExecutionResult.NONE);
		secHubJob.setTrafficLight(null);
		secHubJob.setStarted(LocalDateTime.now());
		secHubJob.setEnded(null);
		jobRepository.save(secHubJob);
	}

	@IsSendingAsyncMessage(MessageID.JOB_RESTARTED)
	private void sendJobRestarted(ScheduleSecHubJob secHubJob, String ownerEmailAddress) {
		DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.JOB_RESTARTED);

		JobMessage message = new JobMessage();
		message.setJobUUID(secHubJob.getUUID());;
		message.setProjectId(secHubJob.getProjectId());
		message.setOwner(secHubJob.getOwner());
		message.setOwnerEmailAddress(ownerEmailAddress);

		request.set(MessageDataKeys.JOB_RESTART_DATA, message);

		eventBus.sendAsynchron(request);

	}


}
