// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDeleteProject;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScheduleDeleteAllProjectAcessService {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduleDeleteAllProjectAcessService.class);


	@Autowired
	ScheduleAccessRepository repository;

	@Autowired
	UserInputAssertion assertion;

	@Transactional
	@UseCaseAdministratorDeleteProject(@Step(number=6,name="Update authorization parts - remove entries for deleted project"))
	public void deleteAnyAccessDataForProject(String projectId) {
		assertion.isValidProjectId(projectId);

		repository.deleteAnyAccessForProject(projectId);

		LOG.info("Removed any access entry for project:{}",projectId);
	}


}
