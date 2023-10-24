// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import static com.mercedesbenz.sechub.test.FlakyOlderThanTestWorkaround.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { JobInformationRepository.class, JobInformationRepositoryDBTest.SimpleTestConfiguration.class })
public class JobInformationRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobInformationRepository jobRepository;

    @Before
    public void before() {
    }

    @Test
    public void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);

        /* execute */
        int deleted = jobRepository.deleteJobInformationOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
        List<JobInformation> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(2, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = jobRepository.deleteJobInformationOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
        List<JobInformation> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_counts_3_deleted_entries() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = jobRepository.deleteJobInformationOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);

    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        /* execute */
        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);
        int deleted = jobRepository.deleteJobInformationOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);

        List<JobInformation> allJobsNow = jobRepository.findAll();
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

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = jobRepository.deleteJobInformationOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        int deleted = jobRepository.deleteJobInformationOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
        List<JobInformation> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    private void assertDeleted(int expected, int deleted, DeleteJobTestData testData, LocalDateTime olderThan) {
        if (deleted == expected) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<JobInformation> all = jobRepository.findAll();
        sb.append("Delete call did return ").append(deleted).append(" uploadMaximumBytes was ").append(expected).append("\n");
        sb.append("The remaining entries are:\n");
        for (JobInformation info : all) {
            sb.append(resolveName(info.since, testData)).append("- since       : ").append(info.since).append("\n");
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

    private String describe(JobInformation info, DeleteJobTestData data) {
        return resolveName(info.since, data) + " - since: " + info.since + "\n";
    }

    private String resolveName(LocalDateTime time, DeleteJobTestData data) {
        if (data.job1_90_days_before_created.since.equals(time)) {
            return "job1_90_days_before_created";
        }
        if (data.job2_2_days_before_created.since.equals(time)) {
            return "job2_2_days_before_created";
        }
        if (data.job3_1_day_before_created.since.equals(time)) {
            return "job3_1_day_before_created";
        }
        if (data.job4_now_created.since.equals(time)) {
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

        JobInformation job1_90_days_before_created;
        JobInformation job2_2_days_before_created;
        JobInformation job3_1_day_before_created;
        JobInformation job4_now_created;

        private void createAndCheckAvailable() {
            job1_90_days_before_created = create(before_90_days, JobStatus.DONE);
            job2_2_days_before_created = create(before_3_days, JobStatus.RUNNING);
            job3_1_day_before_created = create(before_1_day, JobStatus.CREATED);
            job4_now_created = create(now, JobStatus.CREATED);

            // check preconditions
            jobRepository.flush();
            assertEquals(4, jobRepository.count());
            List<JobInformation> allJobsNow = jobRepository.findAll();
            assertTrue(allJobsNow.contains(job1_90_days_before_created));
            assertTrue(allJobsNow.contains(job2_2_days_before_created));
            assertTrue(allJobsNow.contains(job3_1_day_before_created));
            assertTrue(allJobsNow.contains(job4_now_created));
        }

        private JobInformation create(LocalDateTime since, JobStatus status) {
            JobInformation jobInformation = new JobInformation(UUID.randomUUID());
            jobInformation.since = since;
            jobInformation.owner = "owner1";
            jobInformation.projectId = "project1";
            jobInformation.status = status;
            JobInformation result = entityManager.persist(jobInformation);
            entityManager.flush();
            return result;
        }
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
