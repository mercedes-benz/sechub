// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.access;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDeleteProject;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScanDeleteAnyAccessToProjectAtAllService {

	private static final Logger LOG = LoggerFactory.getLogger(ScanDeleteAnyAccessToProjectAtAllService.class);


	@Autowired
	ScanAccessRepository repository;

	@Autowired
	UserInputAssertion assertion;

	@Autowired
	LogSanitizer logSanitizer;

	@Transactional
	@UseCaseAdministratorDeleteProject(@Step(number=7,name="revoke any scan access from project"))
	public void deleteAnyAccessDataForProject(String projectId) {
		assertion.isValidProjectId(projectId);

		repository.deleteAnyAccessForProject(projectId);

		LOG.info("Deleted any access at all for project:{}",logSanitizer.sanitize(projectId, 30));
	}


}
