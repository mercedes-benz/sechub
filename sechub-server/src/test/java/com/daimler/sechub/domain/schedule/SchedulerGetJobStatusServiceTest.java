// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobFactory;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class SchedulerGetJobStatusServiceTest {

	private static final String PROJECT_ID = "project1";

	@Autowired
	private SchedulerGetJobStatusService serviceToTest;

	@MockBean
	private SecHubJobFactory jobFactory;

	@MockBean
	private SecHubJobRepository jobRepository;

	@MockBean
	private ScheduleAccessRepository projectUserAccessRepository;

	@MockBean
	private UserInputAssertion assertion;

	private SecHubConfiguration configuration;
	private ScheduleSecHubJob job;

	private UUID jobUUID;

	private String project;

	private String projectUUID="projectId1";

	@Rule
	public ExpectedException expectedException = ExpectedExceptionFactory.none();

	@Before
	public void before() {
		jobUUID = UUID.randomUUID();
		job = mock(ScheduleSecHubJob.class);
		configuration = mock(SecHubConfiguration.class);
		project = "projectId";

		when(job.getProjectId()).thenReturn(project);

		when(job.getUUID()).thenReturn(jobUUID);
		when(job.getProjectId()).thenReturn(projectUUID);
		when(jobFactory.createJob(eq(configuration))).thenReturn(job);
	}

	@Test(expected = NotFoundException.class) // spring boot tests with Rule "ExpectedException" not working.
	public void get_a_job_status_from_an_unexisting_project_throws_NOT_FOUND_exception() {
		/* execute */
		UUID jobUUID = UUID.randomUUID();
		when(jobRepository.findById(jobUUID)).thenReturn(Optional.of(mock(ScheduleSecHubJob.class)));// should not be necessary, but to
																				// prevent dependency to call
																				// hierachy... we simulate job can be
																				// found
		serviceToTest.getJobStatus("a-project-not-existing", jobUUID);
	}

	@Test(expected = NotFoundException.class) // spring boot tests with Rule "ExpectedException" not working.
	public void get_a_job_status_from_an_exsting_project_but_no_job_throws_NOT_FOUND_exception() {
		/* execute */
		UUID jobUUID = UUID.randomUUID();
		when(jobRepository.findById(jobUUID)).thenReturn(Optional.empty()); // not found...
		serviceToTest.getJobStatus(PROJECT_ID, jobUUID);
	}

}
