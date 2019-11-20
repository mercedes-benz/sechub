// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorAssignsUserToProject;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScheduleGrantUserAccessToProjectService {


private static final Logger LOG = LoggerFactory.getLogger(ScheduleGrantUserAccessToProjectService.class);

	@Autowired
	ScheduleAccessRepository repository;

	@Autowired
	UserInputAssertion assertion;

	@UseCaseAdministratorAssignsUserToProject(@Step(number=2,name="Update schedule authorization parts"))
	public void grantUserAccessToProject(String userId, String projectId) {
		assertion.isValidUserId(userId);
		assertion.isValidProjectId(projectId);

		ScheduleAccess scheduleAccess = new ScheduleAccess(userId,projectId);
		Optional<ScheduleAccess> potentialAlreadyFound = repository.findById(scheduleAccess.getKey());
		if (potentialAlreadyFound.isPresent()) {
			LOG.debug("User {} has already acces to {} so skipped",userId,projectId);
			return;
		}
		LOG.debug("User {} has now gained acces to {}",userId,projectId);
		repository.save(scheduleAccess);
	}


}
