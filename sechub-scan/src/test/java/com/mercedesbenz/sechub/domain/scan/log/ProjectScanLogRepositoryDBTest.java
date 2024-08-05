// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.log;

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

import com.mercedesbenz.sechub.domain.scan.access.ScanAccessRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ScanAccessRepository.class, ProjectScanLogRepositoryDBTest.SimpleTestConfiguration.class })
public class ProjectScanLogRepositoryDBTest {

    @Autowired
    private ProjectScanLogRepository repositoryToTest;

    @Autowired
    private TestEntityManager entityManager;

    @Before
    public void before() {

    }

    @Test
    public void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteProjectScanLogTestData testData = new DeleteProjectScanLogTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);

        /* execute */
        int deleted = repositoryToTest.deleteLogsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
        List<ProjectScanLog> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(2, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1() throws Exception {
        /* prepare */
        DeleteProjectScanLogTestData testData = new DeleteProjectScanLogTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = repositoryToTest.deleteLogsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
        List<ProjectScanLog> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_has_deleted_3() throws Exception {
        /* prepare */
        DeleteProjectScanLogTestData testData = new DeleteProjectScanLogTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = repositoryToTest.deleteLogsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteProjectScanLogTestData testData = new DeleteProjectScanLogTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = repositoryToTest.deleteLogsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
        List<ProjectScanLog> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job1_90_days_before_created));
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(4, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_has_deleted_0() throws Exception {
        /* prepare */
        DeleteProjectScanLogTestData testData = new DeleteProjectScanLogTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = repositoryToTest.deleteLogsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteProjectScanLogTestData testData = new DeleteProjectScanLogTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        int deleted = repositoryToTest.deleteLogsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
        List<ProjectScanLog> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    public void given_3_stored_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        ProjectScanLog access1 = createNewProjectScanLog("project1");
        ProjectScanLog access2 = createNewProjectScanLog("project1");
        ProjectScanLog access3 = createNewProjectScanLog("project2");

        repositoryToTest.save(access1);
        repositoryToTest.save(access2);
        repositoryToTest.save(access3);

        /* check preconditions */
        assertEquals(3, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(access3.getSechubJobUUID()));

        /* execute */
        repositoryToTest.deleteAllLogDataForProject("project1");

        /* test */
        assertEquals(1, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(access3.getSechubJobUUID()));
    }

    private void assertDeleted(int expected, int deleted, DeleteProjectScanLogTestData testData, LocalDateTime olderThan) {
        if (deleted == expected) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ProjectScanLog> all = repositoryToTest.findAll();
        sb.append("Delete call did return ").append(deleted).append(" uploadMaximumBytes was ").append(expected).append("\n");
        sb.append("The remaining entries are:\n");
        for (ProjectScanLog info : all) {
            sb.append(resolveName(info.started, testData)).append("- since       : ").append(info.started).append("\n");
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

    private String describe(ProjectScanLog info, DeleteProjectScanLogTestData data) {
        return resolveName(info.started, data) + " - created: " + info.started + "\n";
    }

    private String resolveName(LocalDateTime time, DeleteProjectScanLogTestData data) {
        if (data.job1_90_days_before_created.started.equals(time)) {
            return "job1_90_days_before_created";
        }
        if (data.job2_2_days_before_created.started.equals(time)) {
            return "job2_2_days_before_created";
        }
        if (data.job3_1_day_before_created.started.equals(time)) {
            return "job3_1_day_before_created";
        }
        if (data.job4_now_created.started.equals(time)) {
            return "job4_now_created";
        }
        return null;
    }

    private class DeleteProjectScanLogTestData {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before_89_days = now.minusDays(89);
        LocalDateTime before_90_days = now.minusDays(90);
        LocalDateTime before_3_days = now.minusDays(3);
        LocalDateTime before_1_day = now.minusDays(1);

        ProjectScanLog job1_90_days_before_created;
        ProjectScanLog job2_2_days_before_created;
        ProjectScanLog job3_1_day_before_created;
        ProjectScanLog job4_now_created;

        private void createAndCheckAvailable() {
            job1_90_days_before_created = create(before_90_days);
            job2_2_days_before_created = create(before_3_days);
            job3_1_day_before_created = create(before_1_day);
            job4_now_created = create(now);

            // check preconditions
            repositoryToTest.flush();
            assertEquals(4, repositoryToTest.count());
            List<ProjectScanLog> allJobsNow = repositoryToTest.findAll();
            assertTrue(allJobsNow.contains(job1_90_days_before_created));
            assertTrue(allJobsNow.contains(job2_2_days_before_created));
            assertTrue(allJobsNow.contains(job3_1_day_before_created));
            assertTrue(allJobsNow.contains(job4_now_created));
        }

        private ProjectScanLog create(LocalDateTime since) {
            ProjectScanLog scanReport = new ProjectScanLog();
            scanReport.started = since;
            scanReport.projectId = "project1";
            scanReport.sechubJobUUID = UUID.randomUUID();
            entityManager.persist(scanReport);
            entityManager.flush();
            return scanReport;
        }
    }

    private ProjectScanLog createNewProjectScanLog(String projectId) {
        return new ProjectScanLog(projectId, UUID.randomUUID(), "testuser");
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
