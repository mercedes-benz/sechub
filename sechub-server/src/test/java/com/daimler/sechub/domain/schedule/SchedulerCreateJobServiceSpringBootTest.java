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
public class SchedulerCreateJobServiceSpringBootTest {

	private static final String PROJECT_ID = "project1";

	@Autowired
	private SchedulerCreateJobService serviceToTest;

	@MockBean
	private SecHubJobFactory jobFactory;

	@MockBean
	private SecHubJobRepository jobRepository;

	@MockBean
	private UserInputAssertion assertion;


	private SecHubConfiguration configuration;
	private ScheduleSecHubJob nextJob;

	private UUID jobUUID;

	private String project;

	private String projectUUID="projectId1";

	@Rule
	public ExpectedException expectedException = ExpectedExceptionFactory.none();

	@Before
	public void before() {
		jobUUID = UUID.randomUUID();
		nextJob = mock(ScheduleSecHubJob.class);
		configuration = mock(SecHubConfiguration.class);
		project = "projectId";

		when(nextJob.getProjectId()).thenReturn(project);

		when(nextJob.getUUID()).thenReturn(jobUUID);
		when(nextJob.getProjectId()).thenReturn(projectUUID);
		when(jobFactory.createJob(eq(configuration))).thenReturn(nextJob);

		/* prepare */
		when(jobRepository.save(nextJob)).thenReturn(nextJob);
		when(jobRepository.findNextJobToExecute()).thenReturn(Optional.of(nextJob));
	}

	@Test(expected = NotFoundException.class) // spring boot tests with Rule "ExpectedException" not working.
	public void scheduling_a_new_job_to_an_unexisting_project_throws_NOT_FOUND_exception() {
		/* execute */
		serviceToTest.createJob("a-project-not-existing", configuration);
	}

	@Test(expected = NotFoundException.class)
	public void no_access_entry__scheduling_a_configuration__will_throw_not_found_exception() {
		/* execute */
		serviceToTest.createJob(PROJECT_ID, configuration);
	}

	@Test
	public void configuration_having_no_project_gets_project_from_URL() {
		/* test (later)*/
		expectedException.expect(NotFoundException.class);

		/* prepare */
		when(jobRepository.save(nextJob)).thenReturn(nextJob);
		when(jobRepository.findNextJobToExecute()).thenReturn(Optional.of(nextJob));

		/* execute */
		serviceToTest.createJob(PROJECT_ID, configuration);
	}

}
