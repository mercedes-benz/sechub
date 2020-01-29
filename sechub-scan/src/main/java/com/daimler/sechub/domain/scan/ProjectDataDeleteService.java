// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.log.ProjectScanLogRepository;
import com.daimler.sechub.domain.scan.product.ProductResultRepository;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigRepository;
import com.daimler.sechub.domain.scan.report.ScanReportRepository;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDeleteProject;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

/**
 * This service will delete all project data from domain scan in ONE transaction.
 * @author Albert Tregnaghi
 *
 */
@Service
public class ProjectDataDeleteService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectDataDeleteService.class);


	@Autowired
	ProjectScanLogRepository scanLogRepository;

	@Autowired
	ProductResultRepository productResultRepository;

	@Autowired
	ScanReportRepository scanReportRepository;
	
	@Autowired
	ScanProjectConfigRepository scanProjectConfigRepository;

	@Autowired
	UserInputAssertion assertion;

	@Autowired
	LogSanitizer logSanitizer;

	@Transactional
	@UseCaseAdministratorDeleteProject(@Step(number=8,name="delete all project scan data"))
	public void deleteAllDataForProject(String projectId) {
		assertion.isValidProjectId(projectId);

		productResultRepository.deleteAllResultsForProject(projectId);
		scanReportRepository.deleteAllReportsForProject(projectId);
		scanLogRepository.deleteAllLogDataForProject(projectId);
		scanProjectConfigRepository.deleteAllConfigurationsForProject(projectId);
		
		LOG.info("Deleted all data (results,reports, scanlogs) for project:{}",logSanitizer.sanitize(projectId, 30));
	}


}
