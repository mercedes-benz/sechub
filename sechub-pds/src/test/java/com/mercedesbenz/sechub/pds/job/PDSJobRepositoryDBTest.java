// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.test.FlakyOlderThanTestWorkaround.*;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSShutdownService;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.pds.config.PDSConfigurationAutoFix;
import com.mercedesbenz.sechub.pds.config.PDSPathExecutableValidator;
import com.mercedesbenz.sechub.pds.config.PDSProductIdentifierValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerIdentifierValidator;

@ActiveProfiles(PDSProfiles.TEST)
@ExtendWith(MockitoExtension.class)
@DataJpaTest
/* @formatter:off */
@ContextConfiguration(classes = {
		PDSPathExecutableValidator.class,
		PDSServerIdentifierValidator.class,
		PDSServerConfigurationValidator.class,
        PDSProductIdentifierValidator.class,
        PDSJobRepository.class,
        PDSShutdownService.class,
        PDSConfigurationAutoFix.class,
        PDSServerConfigurationService.class,
        PDSJobRepositoryDBTest.SimpleTestConfiguration.class,
        SecHubDataConfigurationTypeListParser.class })
/* @formatter:on */
public class PDSJobRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PDSJobRepository repositoryToTest;

    @Autowired
    private PDSServerConfigurationService serverConfigService;

    @ParameterizedTest
    @EnumSource(value = PDSJobStatusState.class)
    void forceStateForJobs(PDSJobStatusState stateBefore) throws Exception {
        /* prepare */
        PDSJob pdsJob1 = createJob(stateBefore);
        PDSJob pdsJob2 = createJob(stateBefore);
        PDSJob pdsJob3 = createJob(stateBefore);

        entityManager.persist(pdsJob1);
        entityManager.flush();

        Set<UUID> jobUUIDs = new HashSet<>();
        jobUUIDs.add(pdsJob1.getUUID());
        jobUUIDs.add(pdsJob3.getUUID());

        PDSJobStatusState newState = PDSJobStatusState.READY_TO_START;
        if (stateBefore == PDSJobStatusState.READY_TO_START) {
            /* in this case we try another target state */
            newState = PDSJobStatusState.CANCELED;
        }

        /* execute */
        repositoryToTest.forceStateForJobs(newState, jobUUIDs);

        /* test */
        entityManager.flush();
        entityManager.clear(); // we must clear to avoid caching of old entities in test

        PDSJob pdsJob1b = repositoryToTest.findById(pdsJob1.getUUID()).get();
        PDSJob pdsJob2b = repositoryToTest.findById(pdsJob2.getUUID()).get();
        PDSJob pdsJob3b = repositoryToTest.findById(pdsJob3.getUUID()).get();

        assertEquals(newState, pdsJob1b.getState());
        assertEquals(stateBefore, pdsJob2b.getState()); // was not inside set - means not changed
        assertEquals(newState, pdsJob3b.getState());

    }

    @ParameterizedTest
    @EnumSource(value = PDSJobStatusState.class, mode = Mode.EXCLUDE, names = "CANCEL_REQUESTED")
    void findAllJobsInState_returns_empty_list_forjobs_not_in_state_CANCEL_REQUESTED(PDSJobStatusState state) throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        PDSJob pdsJob = testData.job3_1_day_before_created;
        pdsJob.setState(state);

        /* execute */
        List<PDSJob> result = repositoryToTest.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED);

        /* test */
        assertEquals(0, result.size());
    }

    @Test
    void findAllJobsInState_returns_one_entry_when_one_job_is_in_state_CANCEL_REQUESTED() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();
        PDSJob pdsJob = testData.job3_1_day_before_created;
        pdsJob.setState(PDSJobStatusState.CANCEL_REQUESTED);

        entityManager.persist(pdsJob);
        entityManager.flush();

        /* execute */
        List<PDSJob> result = repositoryToTest.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED);

        /* test */
        assertEquals(1, result.size());
        assertTrue(result.contains(pdsJob));
    }

    @Test
    void findAllJobsInState_returns_three_entries_when_3_jobs_are_in_state_CANCEL_REQUESTED() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();
        PDSJob pdsJob1 = testData.job1_90_days_before_created;
        pdsJob1.setState(PDSJobStatusState.CANCEL_REQUESTED);

        PDSJob pdsJob2 = testData.job2_2_days_before_created;
        pdsJob2.setState(PDSJobStatusState.CANCEL_REQUESTED);

        PDSJob pdsJob3 = testData.job3_1_day_before_created;
        pdsJob3.setState(PDSJobStatusState.CANCEL_REQUESTED);

        entityManager.persist(pdsJob1);
        entityManager.persist(pdsJob2);
        entityManager.persist(pdsJob3);
        entityManager.flush();

        /* execute */
        List<PDSJob> result = repositoryToTest.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED);

        /* test */
        assertEquals(3, result.size());
        assertTrue(result.contains(pdsJob1));
        assertTrue(result.contains(pdsJob2));
        assertTrue(result.contains(pdsJob3));
    }

    @Test
    void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(2, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_delete_1_day_before_plus1_second_counts_3_deleted_entries() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
    }

    @Test
    void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job1_90_days_before_created));
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(4, allJobsNow.size());
    }

    @Test
    void test_data_4_jobs_oldest_90_days_delete_90_days_counts_0_deleted_entries() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
    }

    @Test
    void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        int deleted = repositoryToTest.deleteJobOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
        List<PDSJob> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    void findByServerIdAndStatus_returns_0_for_RUNNING_and_SERVERDI1_when_nothing_created() {
        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.RUNNING);

        /* test */
        assertEquals(0, amount);

    }

    @Test
    void findByServerIdAndStatus_returns_2_for_RUNNING_and_SERVERDI1_when_two_created_and_in_state_running() {
        /* prepare */
        createJob(PDSJobStatusState.RUNNING);
        createJob(PDSJobStatusState.RUNNING);

        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.RUNNING);

        /* test */
        assertEquals(2, amount);

    }

    @Test
    void findByServerIdAndStatus_returns_1_for_RUNNING_and_SERVERDI1_when_two_created_but_only_one_for_this_server_and_in_state_running() {
        /* prepare */
        createJob(PDSJobStatusState.RUNNING);
        createJob(PDSJobStatusState.RUNNING, "OTHER_SERVER_ID_FOR_TEST");

        /* execute */
        long amount = repositoryToTest.countJobsOfServerInState("SERVERID1", PDSJobStatusState.RUNNING);

        /* test */
        assertEquals(1, amount);

    }

    @Test
    void findByServerIdAndStatus_returns_3_for_CREATED_for_this_server_when_mixed_up_with_other_parts_from_same_serve_and_other_servers() {
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
    void when_no_job_created_findNextJobToExecute_returns_optional_not_present() {
        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    void when_one_jobs_created_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.CREATED);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    void when_one_jobs_marked_as_ready_to_start_findNextJobToExecute_returns_this_one() {
        /* prepare */
        PDSJob job1 = createJob(PDSJobStatusState.READY_TO_START, 0);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertTrue(nextJob.isPresent());
        assertEquals(job1, nextJob.get());

    }

    @Test
    void when_two_jobs_just_created_findNextJobToExecute_returns_none() {
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
    void when_two_jobs_ready_to_start_findNextJobToExecute_returns_older_one() {
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
    void when_two_jobs_exist_but_older_is_already_running_findNextJobToExecute_returns_new_ready_to_start() {
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
    void when_two_jobs_exist_but_older_is_done_and_newer_already_running_findNextJobToExecute_returns_none() {
        /* prepare */
        createJob(PDSJobStatusState.DONE, 1);
        createJob(PDSJobStatusState.RUNNING, 0);

        /* execute */
        Optional<PDSJob> nextJob = repositoryToTest.findNextJobToExecute();

        /* test */
        assertFalse(nextJob.isPresent());

    }

    @Test
    void when_two_jobs_exist_but_older_is_canceled_and_newer_failed_findNextJobToExecute_returns_none() {
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
        job.encryptedConfiguration = "{}".getBytes(); // simulate encryption
        job.encryptionInitialVectorData = "initial-vector".getBytes(); // simulate initial vector
        job.state = state;

        /* persist */
        job = entityManager.persistAndFlush(job);
        return job;
    }

    private void assertDeleted(int expected, int deleted, DeleteJobTestData testData, LocalDateTime olderThan) {
        if (deleted == expected) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<PDSJob> all = repositoryToTest.findAll();
        sb.append("Delete call did return ").append(deleted).append(" uploadMaximumBytes was ").append(expected).append("\n");
        sb.append("The remaining entries are:\n");
        for (PDSJob info : all) {
            sb.append(resolveName(info.created, testData)).append("- since       : ").append(info.created).append("\n");
        }
        sb.append("\n-----------------------------------------------------");
        sb.append("\nolderThan was: ").append(olderThan).append(" - means :").append((resolveName(olderThan, testData)));
        sb.append("\n-----------------------------------------------------\n");
        sb.append(describe(testData.job1_90_days_before_created, testData));
        sb.append(describe(testData.job2_2_days_before_created, testData));
        sb.append(describe(testData.job3_1_day_before_created, testData));
        sb.append(describe(testData.job4_now_created, testData));

        fail(sb.toString());
    }

    private String describe(PDSJob info, DeleteJobTestData data) {
        return resolveName(info.created, data) + " - created: " + info.created + "\n";
    }

    private String resolveName(LocalDateTime time, DeleteJobTestData data) {
        if (data.job1_90_days_before_created.created.equals(time)) {
            return "job1_90_days_before_created";
        }
        if (data.job2_2_days_before_created.created.equals(time)) {
            return "job2_2_days_before_created";
        }
        if (data.job3_1_day_before_created.created.equals(time)) {
            return "job3_1_day_before_created";
        }
        if (data.job4_now_created.created.equals(time)) {
            return "job4_now_created";
        }
        return null;
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
