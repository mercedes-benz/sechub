package com.daimler.sechub.domain.scan.log;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectScanLogDeleteServiceTest {

	private ProjectScanLogDeleteService serviceToTest;
	private ProjectScanLogRepository 		repo;

	@Before
	public void before() {
		repo= mock(ProjectScanLogRepository.class);

		serviceToTest = new ProjectScanLogDeleteService();
		serviceToTest.logSanitizer=mock(LogSanitizer.class);
		serviceToTest.repository=repo;
		serviceToTest.assertion=mock(UserInputAssertion.class);
	}

	@Test
	public void deleteAllForProjectTriggersRepositoryDelteAllForProject() {
		/* execute */
		serviceToTest.deleteAllLogDataForProject("project-1");

		/* test */
		verify(repo).deleteAllLogDataForProject("project-1");

	}

}
