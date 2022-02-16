// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfileRepository;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectDataDeleteServiceTest {

    private ProjectDataDeleteService serviceToTest;
    private ProjectScanLogRepository projectScanLogRepository;
    private ProductResultRepository productResultRepository;
    private ScanReportRepository scanReportRepository;
    private ScanProjectConfigRepository scanProjectConfigRepository;
    private ProductExecutionProfileRepository profileRepository;

    @Before
    public void before() {
        projectScanLogRepository = mock(ProjectScanLogRepository.class);
        productResultRepository = mock(ProductResultRepository.class);
        scanReportRepository = mock(ScanReportRepository.class);
        scanProjectConfigRepository = mock(ScanProjectConfigRepository.class);
        profileRepository = mock(ProductExecutionProfileRepository.class);

        serviceToTest = new ProjectDataDeleteService();
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);

        serviceToTest.scanLogRepository = projectScanLogRepository;
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.scanReportRepository = scanReportRepository;
        serviceToTest.scanProjectConfigRepository = scanProjectConfigRepository;
        serviceToTest.profileRepository = profileRepository;
    }

    @Test
    public void deleteAllDataForProject_triggers_deleteAllResultsForProject() {
        /* execute */
        serviceToTest.deleteAllDataForProject("project-1");

        /* test */
        verify(productResultRepository).deleteAllResultsForProject("project-1");

    }

    @Test
    public void deleteAllDataForProject_triggers_deleteAllReportsForProject() {
        /* execute */
        serviceToTest.deleteAllDataForProject("project-1");

        /* test */
        verify(scanReportRepository).deleteAllReportsForProject("project-1");

    }

    @Test
    public void deleteAllDataForProject_triggers_deleteAllLogDataForProject() {
        /* execute */
        serviceToTest.deleteAllDataForProject("project-1");

        /* test */
        verify(projectScanLogRepository).deleteAllLogDataForProject("project-1");

    }

    @Test
    public void deleteAllDataForProject_triggers_deleteAllConfigurationsForProject() {
        /* execute */
        serviceToTest.deleteAllDataForProject("project-1");

        /* test */
        verify(scanProjectConfigRepository).deleteAllConfigurationsForProject("project-1");

    }

}
