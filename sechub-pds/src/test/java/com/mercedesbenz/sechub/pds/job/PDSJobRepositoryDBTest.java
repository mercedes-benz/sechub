// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.pds.PDSProfiles;
import com.mercedesbenz.sechub.pds.PDSShutdownService;
import com.mercedesbenz.sechub.pds.config.PDSPathExecutableValidator;
import com.mercedesbenz.sechub.pds.config.PDSProductIdentifierValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerIdentifierValidator;

@ActiveProfiles(PDSProfiles.TEST)
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { PDSPathExecutableValidator.class, PDSServerIdentifierValidator.class, PDSServerConfigurationValidator.class,
        PDSProductIdentifierValidator.class, PDSShutdownService.class, PDSJobRepository.class, PDSServerConfigurationService.class,
        PDSJobRepositoryDBTest.SimpleTestConfiguration.class })
public class PDSJobRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PDSJobRepository repositoryToTest;

    @Autowired
    private PDSServerConfigurationService serverConfigService;

    @Before
    public void before() {
    }

    @Test
    public void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        repositoryToTest.deleteJobOlderThan(testData.before_1_day);
        repositoryToTest.flush();

        /* test */
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
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
        repositoryToTest.deleteJobOlderThan(testData.before_1_day.plusSeconds(1));
        repositoryToTest.flush();

        /* test */
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_counts_3_deleted_entries() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(testData.before_1_day.plusSeconds(1));
        repositoryToTest.flush();

        /* test */
        assertEquals(3, deleted);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        repositoryToTest.deleteJobOlderThan(testData.before_90_days);
        repositoryToTest.flush();

        /* test */
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job1_90_days_before_created));
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(4, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_counts_0_deleted_entries() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(testData.before_90_days);
        repositoryToTest.flush();

        /* test */
        assertEquals(0, deleted);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        repositoryToTest.deleteJobOlderThan(testData.before_89_days.minusSeconds(1));
        repositoryToTest.flush();

        /* test */
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
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
        repositoryToTest.deleteJobOlderThan(testData.before_89_days.minusSeconds(1));
        repositoryToTest.flush();

        /* test */
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    public void findByServerIdAndStatus_returns_0_for_RUNNING_and_SERVERDI1_when_nothing_created() {
        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.RUNNING);

        /* test */
        assertEquals(0, amount);

    }

    @Test
    public void findByServerIdAndStatus_returns_2_for_RUNNING_and_SERVERDI1_when_two_created_and_in_state_running() {
        /* prepare */
        createJob(PDSJobStatusState.RUNNING);
        createJob(PDSJobStatusState.RUNNING);

        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.RUNNING);

        /* test */
        assertEquals(2, amount);

    }

    @Test
    public void findByServerIdAndStatus_returns_1_for_RUNNING_and_SERVERDI1_when_two_created_but_only_one_for_this_server_and_in_state_running() {
        /* prepare */
        createJob(PDSJobStatusState.RUNNING);
        createJob(PDSJobStatusState.RUNNING, "OTHER_SERVER_ID_FOR_TEST");

        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.RUNNING);

        /* test */
        assertEquals(1, amount);

    }

    @Test
    public void findByServerIdAndStatus_returns_3_for_CREATED_for_this_server_when_mixed_up_with_other_parts_from_same_serve_and_other_servers() {
        /* prepare */
        createJob(PDSJobStatusState.CREATED);
        createJob(PDSJobStatusState.CREATED);
        createJob(PDSJobStatusState.RUNNING);
        createJob(PDSJobStatusState.CREATED);
        createJob(PDSJobStatusState.DONE);
        createJob(PDSJobStatusState.FAILED);
        createJob(PDSJobStatusState.CREATED, "OTHER_SERVER_ID_FOR_TEST");
        createJob(PDSJobStatusState.RUNNING, "OTHER_SERVER_ID_FOR_TEST");

        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.CREATED);

        /* test */
        assertEquals(3, amount);

    }

    @Test
    public void when_no_job_created_findNextJobToExecute_returns_optional_not_present() {
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    public void when_one_jobs_created_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.CREATED);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    public void when_one_jobs_marked_as_ready_to_start_findNextJobToExecute_returns_this_one() {
        /* prepare */
        PDSJob job1 = createJob(PDSJobStatusState.READY_TO_START, 0);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job1, nextJob.get());

    }

    @Test
    public void when_two_jobs_just_created_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.CREATED, 0);
        createJob(PDSJobStatusState.CREATED, 1);

        entityManager.flush();

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    public void when_two_jobs_ready_to_start_findNextJobToExecute_returns_older_one() {
        /* prepare */
        PDSJob job1 = createJob(PDSJobStatusState.READY_TO_START, 1);
        createJob(PDSJobStatusState.READY_TO_START, 0);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job1, nextJob.get());

    }

    @Test
    public void when_two_jobs_exist_but_older_is_already_running_findNextJobToExecute_returns_new_ready_to_start() {
        /* prepare */
        createJob(PDSJobStatusState.RUNNING, 2);
        PDSJob job2 = createJob(PDSJobStatusState.READY_TO_START, 1);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job2, nextJob.get());

    }

    @Test
    public void when_two_jobs_exist_but_older_is_done_and_newer_already_running_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.DONE, 1);
        createJob(PDSJobStatusState.RUNNING, 0);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    public void when_two_jobs_exist_but_older_is_canceled_and_newer_failed_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.CANCEL_REQUESTED, 1);
        createJob(PDSJobStatusState.FAILED, 0);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    /**
     * Creates a new job and stores inside db
     *
     * @param state
     * @return job
     */
    private PDSJob createJob(PDSJobStatusState state) {
        return createJob(state, 0, serverConfigService.getServerId());
    }

    /**
     * Creates a new job and stores inside db
     *
     * @param state
     * @return job
     */
    private PDSJob createJob(PDSJobStatusState state, String serverId) {
        return createJob(state, 0, serverId);
    }

    /**
     * Creates a new job and stores inside db
     *
     * @param state
     * @param minutes - created n minutes before
     * @return job
     */
    private PDSJob createJob(PDSJobStatusState state, int minutes) {
        return createJob(state, minutes, serverConfigService.getServerId());
    }

    /**
     * Creates a new job and stores inside db
     *
     * @param state
     * @param minutes  - created n minutes before
     * @param serverId - server id where this job belongs to
     * @return job
     */
    private PDSJob createJob(PDSJobStatusState state, int minutes, String serverId) {
        PDSJob job = new PDSJob();
        job.serverId = serverId;
        // necessary because must be not null
        job.created = LocalDateTime.of(2020, 06, 24, 13, 55, 01).minusMinutes(minutes);
        job.owner = "owner";
        job.jsonConfiguration = "{}";
        job.state = state;

        /* persist */
        job = entityManager.persistAndFlush(job);
        return job;
    }

    private class DeleteJobTestData {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before_89_days = now.minusDays(89);
        LocalDateTime before_90_days = now.minusDays(90);
        LocalDateTime before_3_days = now.minusDays(3);
        LocalDateTime before_1_day = now.minusDays(1);

        PDSJob job1_90_days_before_created;
        PDSJob job2_2_days_before_created;
        PDSJob job3_1_day_before_created;
        PDSJob job4_now_created;

        private void createAndCheckAvailable() {
            job1_90_days_before_created = create(before_90_days, PDSJobStatusState.DONE);
            job2_2_days_before_created = create(before_3_days, PDSJobStatusState.RUNNING);
            job3_1_day_before_created = create(before_1_day, PDSJobStatusState.CREATED);
            job4_now_created = create(now, PDSJobStatusState.CREATED);

            // check preconditions
            repositoryToTest.flush();
            assertEquals(4, repositoryToTest.count());
            List<PDSJob> allJobsNow = repositoryToTest.findAll();
            assertTrue(allJobsNow.contains(job1_90_days_before_created));
            assertTrue(allJobsNow.contains(job2_2_days_before_created));
            assertTrue(allJobsNow.contains(job3_1_day_before_created));
            assertTrue(allJobsNow.contains(job4_now_created));
        }

        private PDSJob create(LocalDateTime created, PDSJobStatusState state) {
            PDSJob pdsJob = new PDSJob();
            pdsJob.created = created;
            pdsJob.owner = "owner1";
            pdsJob.state = state;
            pdsJob.serverId = "serverId1";

            entityManager.persist(pdsJob);
            entityManager.flush();
            return pdsJob;
        }
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
