// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

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
public class ProjectScanLogDeleteService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectScanLogDeleteService.class);


	@Autowired
	ProjectScanLogRepository repository;

	@Autowired
	UserInputAssertion assertion;

	@Autowired
	LogSanitizer logSanitizer;

	@Transactional
	@UseCaseAdministratorDeleteProject(@Step(number=8,name="delete all log scan data"))
	public void deleteAllLogDataForProject(String projectId) {
		assertion.isValidProjectId(projectId);

		repository.deleteAllLogDataForProject(projectId);

		LOG.info("Deleted all log data for project:{}",logSanitizer.sanitize(projectId, 30));
	}


}
