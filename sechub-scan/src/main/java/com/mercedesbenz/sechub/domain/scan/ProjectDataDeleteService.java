// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfileRepository;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.transaction.Transactional;

/**
 * This service will delete all project data from domain scan in ONE
 * transaction.
 *
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
    ProductExecutionProfileRepository profileRepository;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    LogSanitizer logSanitizer;

    @Transactional
    @UseCaseAdminDeleteProject(@Step(number = 8, name = "delete all project scan data"))
    public void deleteAllDataForProject(String projectId) {
        assertion.assertIsValidProjectId(projectId);

        productResultRepository.deleteAllResultsForProject(projectId);
        scanReportRepository.deleteAllReportsForProject(projectId);
        scanLogRepository.deleteAllLogDataForProject(projectId);
        /*
         * next line deletes any project related configuration - this includes template
         * assignment
         */
        scanProjectConfigRepository.deleteAllConfigurationsForProject(projectId);
        profileRepository.deleteAllProfileRelationsToProject(projectId);

        LOG.info("Deleted all data (results,reports, scanlogs,profile-relations) for project:{}", logSanitizer.sanitize(projectId, 30));
    }

}
