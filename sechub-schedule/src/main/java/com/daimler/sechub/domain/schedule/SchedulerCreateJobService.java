// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static com.daimler.sechub.domain.schedule.job.SecHubJobTraceLogID.*;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobFactory;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerCreateJobService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerCreateJobService.class);

	@Autowired
	private SecHubJobRepository jobRepository;

	@Autowired
	private SecHubJobFactory secHubJobFactory;

	@Autowired
	ScheduleAssertService assertService;

	@Autowired
	UserInputAssertion assertion;

	@Validated
	@UseCaseUserCreatesNewJob(@Step(number = 2, name = "Persistence and result", description = "Persist a new job entry and return Job UUID"))
	public SchedulerResult createJob(String projectId, @Valid SecHubConfiguration configuration) {
		assertion.isValidProjectId(projectId);

		/* we set the project id into configuration done by used url! */
		configuration.setProjectId(projectId);

		assertService.assertUserHasAccessToProject(projectId);
		assertService.assertExecutionAllowed(configuration);

		ScheduleSecHubJob secHubJob = secHubJobFactory.createJob(configuration);
		jobRepository.save(secHubJob);

		if (LOG.isInfoEnabled()) {
			LOG.info("New job added:{}", traceLogID(secHubJob));
		}
		return new SchedulerResult(secHubJob.getUUID());
	}




}