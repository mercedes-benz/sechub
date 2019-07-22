// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import static com.daimler.sechub.domain.schedule.ExecutionState.*;
import static com.daimler.sechub.domain.schedule.job.JobCreator.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.test.TestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
@ContextConfiguration(classes= {JobRepository.class, JobRepositoryDBTest.SimpleTestConfiguration.class})
public class JobRepositoryDBTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private SecHubJobRepository jobRepository;
	
	private JobCreator jobCreator;

	@Before
	public void before() {

		jobCreator = jobCreator("project-db-test", entityManager);
	}

	@Test
	public void findByUUID__the_job_is_returned_when_existing() {
		/* prepare */
		ScheduleSecHubJob newJob = jobCreator.create();

		/* execute */
		Optional<ScheduleSecHubJob> job = jobRepository.findById(newJob.getUUID());

		/* test */
		assertTrue(job.isPresent());
		assertEquals(newJob.getUUID(), job.get().getUUID());
	}

	@Test
	public void findNextJobToExecute__and_no_jobs_available_at_all_null_is_returned_when_existing() {

		assertFalse(jobRepository.findNextJobToExecute().isPresent());
	}

	@Test
	public void findNextJobToExecute__and_no_executable_job_available_at_all_null_is_returned_when_existing() {
		/* prepare */
		jobCreator.newJob().being(STARTED).create();

		/* execute + test */
		assertFalse(jobRepository.findNextJobToExecute().isPresent());

	}

	@Test
	public void findNextJobToExecute__the_first_job_in_state_READY_TO_START_is_returned_when_existing() {
		/* prepare @formatter:off*/
		
		jobCreator.newJob().being(STARTED).createAnd().
				   newJob().being(CANCEL_REQUESTED).createAnd().
				   newJob().being(ENDED).create();
		
		ScheduleSecHubJob expectedNextJob =  
		jobCreator.newJob().being(READY_TO_START).create();
		
		TestUtil.waitMilliseconds(1); // just enough time to make the next job "older" than former one, so we got no flaky tests when checking jobUUID later
		
		
		jobCreator.newJob().being(STARTED).createAnd().
				   newJob().being(READY_TO_START).create();
		
		/* execute */
		Optional<ScheduleSecHubJob> optional = jobRepository.findNextJobToExecute();
		assertTrue(optional.isPresent());
		
		ScheduleSecHubJob job = optional.get();
		
		/* test @formatter:on*/
		assertNotNull(job.getUUID());
		assertEquals(expectedNextJob.getUUID(), job.getUUID());
	}

	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{
		
	}
}
