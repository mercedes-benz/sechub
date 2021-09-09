// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

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
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerApproveJobService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerApproveJobService.class);

	@Autowired
	private SecHubJobRepository jobRepository;

	@Autowired
	ScheduleAssertService assertService;

	@Autowired
	UserInputAssertion assertion;

	@UseCaseUserApprovesJob(@Step(number = 2, name = "Try to find job annd update execution state", description = "When job is found and user has access job will be marked as ready for execution"))
	public void approveJob(String projectId, UUID jobUUID) {
		assertion.isValidProjectId(projectId);
		assertion.isValidJobUUID(jobUUID);

		assertService.assertUserHasAccessToProject(projectId);
		assertService.assertProjectAllowsWriteAccess(projectId);

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
