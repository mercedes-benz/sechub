// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserChecksJobStatus;

@Service
public class SchedulerGetJobStatusService {

	@Autowired
	ScheduleAssertService assertService;
	
	@Validated
	@UseCaseUserChecksJobStatus(@Step(number = 2, name = "Try to find project annd fail or return job status"))
	public ScheduleJobStatus getJobStatus(String projectId, UUID jobUUID) {
		notEmpty(projectId, "Project id may not be empty!");
		notNull(jobUUID, "jobUUID may not be null!");

		assertService.assertUserHasAccessToProject(projectId);

		ScheduleSecHubJob secHubJob = assertService.assertJob(projectId, jobUUID);

		return new ScheduleJobStatus(secHubJob);
	}
	

	
}
