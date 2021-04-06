// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.access.ScheduleUserAccessToProjectValidationService;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.domain.schedule.whitelist.ProjectWhiteListSecHubConfigurationValidationService;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.error.NotFoundException;

@Service
public class ScheduleAssertService {

	@Autowired
	private SecHubJobRepository jobRepository;

	@Autowired
	ScheduleUserAccessToProjectValidationService userAccessValidation;

	@Autowired
	ProjectWhiteListSecHubConfigurationValidationService executionIsInWhiteListValidation;

	/**
	 * Assert current logged in user has access to project
	 * @param projectId
	 */
	public void assertUserHasAccessToProject(String projectId) {
		userAccessValidation.assertUserHasAccessToProject(projectId);
	}

	/**
	 * Asserts execution is allowed for given configuration
	 * @param configuration
	 */
	public void assertExecutionAllowed(@Valid SecHubConfiguration configuration) {
		executionIsInWhiteListValidation.assertAllowedForProject(configuration);
	}

	/**
	 * Asserts a job is existing and returns the job
	 * @param projectId
	 * @param jobUUID
	 * @return job, never <code>null</code>
	 */
	public ScheduleSecHubJob assertJob(String projectId, UUID jobUUID) {
		Optional<ScheduleSecHubJob> secHubJob = jobRepository.findForProject(projectId, jobUUID);
		if (!secHubJob.isPresent()) {
			// we say "... or you have no access - just to obfuscate... so it's not clear to
			// malicious actors they got a target...
			throw new NotFoundException("Job does not exist, or you have no access.");
		}
		return secHubJob.get();
	}

}
