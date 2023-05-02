// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.domain.schedule.ExecutionState.*;
import static com.mercedesbenz.sechub.domain.schedule.job.JobCreator.*;
import static com.mercedesbenz.sechub.test.FlakyOlderThanTestWorkaround.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import com.mercedesbenz.sechub.commons.model.ModuleGroup;
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

        jobCreator = jobCreator("p0", entityManager);
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectNotYetExecuted_one_available() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectNotYetExecuted_2_projects_1_project_running() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob3.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_2_projects_1_project_running_all_same_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob3.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_2_projects_1_project_running_different_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.DYNAMIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob2.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_2_project_running_different_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.DYNAMIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob2.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_2_project_running_same_groups() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.STARTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob3.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_4_projects_no_project_started_all_ready() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p4").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob1.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted_3_projects_different_states() {
        /* prepare */
        ScheduleSecHubJob newJob1 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob2 = jobCreator.project("p1").module(ModuleGroup.STATIC).being(ExecutionState.INITIALIZING).create();
        ScheduleSecHubJob newJob3 = jobCreator.project("p2").module(ModuleGroup.STATIC).being(ExecutionState.CANCELED).create();
        ScheduleSecHubJob newJob4 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.ENDED).create();
        ScheduleSecHubJob newJob5 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.CANCEL_REQUESTED).create();
        ScheduleSecHubJob newJob6 = jobCreator.project("p3").module(ModuleGroup.STATIC).being(ExecutionState.READY_TO_START).create();

        /* check preconditions */
        assertTrue(newJob2.created.isAfter(newJob1.created));
        assertTrue(newJob3.created.isAfter(newJob2.created));
        assertTrue(newJob4.created.isAfter(newJob3.created));
        assertTrue(newJob5.created.isAfter(newJob4.created));
        assertTrue(newJob6.created.isAfter(newJob5.created));

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob6.getUUID(), uuid.get());
    }

    @Test
    public void custom_query_nextJobIdToExecuteFirstInFirstOut() {
        /* prepare */
        ScheduleSecHubJob newJob = jobCreator.being(ExecutionState.READY_TO_START).create();

        /* execute */
        Optional<UUID> uuid = jobRepository.nextJobIdToExecuteFirstInFirstOut();

        /* test */
        assertTrue(uuid.isPresent());
        assertEquals(newJob.getUUID(), uuid.get());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();
        
        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);
        
        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
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

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);
        
        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
        List<ScheduleSecHubJob> allJobsNow = jobRepository.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteJobTestData testData = new DeleteJobTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);
        
        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
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

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);
        
        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
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
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
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
        
        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        jobRepository.deleteJobsOlderThan(olderThan);
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
        
        LocalDateTime olderThan = testData.before_89_days;
        
        /* execute */
        int deleted = jobRepository.deleteJobsOlderThan(olderThan);
        jobRepository.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
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

        assertFalse(jobRepository.nextJobIdToExecuteFirstInFirstOut().isPresent());
    }

    @Test
    public void findNextJobToExecute__and_no_executable_job_available_at_all_null_is_returned_when_existing() {
        /* prepare */
        jobCreator.newJob().being(STARTED).create();

        /* execute + test */
        assertFalse(jobRepository.nextJobIdToExecuteFirstInFirstOut().isPresent());

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
		Optional<UUID> optional = jobRepository.nextJobIdToExecuteFirstInFirstOut();
		assertTrue(optional.isPresent());

		UUID jobUUID = optional.get();

		/* test @formatter:on*/
        assertNotNull(jobUUID);
        assertEquals(expectedNextJob.getUUID(), jobUUID);
    }

    private void assertDeleted(int expected, int deleted, DeleteJobTestData testData, LocalDateTime olderThan) {
        if (deleted == expected) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ScheduleSecHubJob> all = jobRepository.findAll();
        sb.append("Delete call did return ").append(deleted).append(" uploadMaximumBytes was ").append(expected).append("\n");
        sb.append("The remaining entries are:\n");
        for (ScheduleSecHubJob info : all) {
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

    private String describe(ScheduleSecHubJob info, DeleteJobTestData data) {
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
