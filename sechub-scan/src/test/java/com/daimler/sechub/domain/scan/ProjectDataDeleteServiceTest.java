package com.daimler.sechub.domain.scan;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.log.ProjectScanLogRepository;
import com.daimler.sechub.domain.scan.product.ProductResultRepository;
import com.daimler.sechub.domain.scan.report.ScanReportRepository;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectDataDeleteServiceTest {

	private ProjectDataDeleteService serviceToTest;
	private ProjectScanLogRepository projectScanLogRepository;
	private ProductResultRepository productResultRepository;
	private ScanReportRepository scanReportRepository;

	@Before
	public void before() {
		projectScanLogRepository= mock(ProjectScanLogRepository.class);
		productResultRepository = mock(ProductResultRepository.class);
		scanReportRepository = mock(ScanReportRepository.class);

		serviceToTest = new ProjectDataDeleteService();
		serviceToTest.logSanitizer=mock(LogSanitizer.class);
		serviceToTest.assertion=mock(UserInputAssertion.class);

		serviceToTest.scanLogRepository=projectScanLogRepository;
		serviceToTest.productResultRepository=productResultRepository;
		serviceToTest.scanReportRepository=scanReportRepository;
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

}
