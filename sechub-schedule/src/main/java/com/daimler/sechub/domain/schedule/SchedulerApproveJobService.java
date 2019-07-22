// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserApprovesJob;

@Service
public class SchedulerApproveJobService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerApproveJobService.class);

	@Autowired
	private SecHubJobRepository jobRepository;

	@Autowired
	ScheduleAssertService assertService;
	
	@UseCaseUserApprovesJob(@Step(number = 2, name = "Try to find project annd update execution state", description = "When project is found and user has access and job is initializing the state will be updated and marked as ready for execution"))
	public void approveJob(String projectId, UUID jobUUID) {
		notEmpty(projectId, "Project id may not be empty!");
		notNull(jobUUID, "jobUUID may not be null!");

		assertService.assertUserHasAccessToProject(projectId);

		ScheduleSecHubJob secHubJob = assertService.assertJob(projectId, jobUUID);
		ExecutionState state = secHubJob.getExecutionState();
		if (! ExecutionState.INITIALIZING.equals(state)) {
			throw new NotAcceptableException("Not in correct state");
		}
		secHubJob.setExecutionState(ExecutionState.READY_TO_START);
		jobRepository.save(secHubJob);
		LOG.info("job {} now approved", jobUUID);
	}

	
}
