// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.domain.schedule.ExecutionState.*;
import static com.mercedesbenz.sechub.domain.schedule.job.JobCreator.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.domain.schedule.ExecutionState;
import com.mercedesbenz.sechub.test.TestUtil;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { JobRepository.class, SecHubJobRepositoryDBTest.SimpleTestConfiguration.class })
public class SecHubJobRepositoryDBTest {

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
    public void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        jobRepository.deleteJobsOlderThan(testData.before_1_day);
        jobRepository.flush();

        /* test */
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(2, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        jobRepository.deleteJobsOlderThan(testData.before_1_day.plusSeconds(1));
        jobRepository.flush();

        /* test */
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        jobRepository.deleteJobsOlderThan(testData.before_90_days);
        jobRepository.flush();

        /* test */
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job1_90_days_before_created));
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(4, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_deletes_0() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(testData.before_90_days);
        jobRepository.flush();

        /* test */
        assertEquals(0, deleted);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        jobRepository.deleteJobsOlderThan(testData.before_89_days.minusSeconds(1));
        jobRepository.flush();

        /* test */
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_1_day() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        jobRepository.deleteJobsOlderThan(testData.before_89_days.minusSeconds(1));
        jobRepository.flush();

        /* test */
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_deleted_1() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(testData.before_89_days.minusSeconds(1));
        jobRepository.flush();

        /* test */
        assertEquals(1, deleted);
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
				   newJob().being(CANCELED).createAnd().
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

    private class DeleteJobTestData {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before_89_days = now.minusDays(89);
        LocalDateTime before_90_days = now.minusDays(90);
        LocalDateTime before_3_days = now.minusDays(3);
        LocalDateTime before_1_day = now.minusDays(1);

        ScheduleSecHubJob job1_90_days_before_created;
        ScheduleSecHubJob job2_2_days_before_created;
        ScheduleSecHubJob job3_1_day_before_created;
        ScheduleSecHubJob job4_now_created;

        private void createAndCheckAvailable() {
            job1_90_days_before_created = jobCreator.created(before_90_days).started(null).create();
            job2_2_days_before_created = jobCreator.created(before_3_days).started(before_1_day).being(ExecutionState.STARTED).create();
            job3_1_day_before_created = jobCreator.created(before_1_day).started(now).create();
            job4_now_created = jobCreator.created(now).create();

            // check preconditions
            jobRepository.flush();
            assertEquals(4, jobRepository.count());
            List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
            assertTrue(allJobsNow.contains(job1_90_days_before_created));
            assertTrue(allJobsNow.contains(job2_2_days_before_created));
            assertTrue(allJobsNow.contains(job3_1_day_before_created));
            assertTrue(allJobsNow.contains(job4_now_created));
        }
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
