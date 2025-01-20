// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static com.mercedesbenz.sechub.sharedkernel.ProductIdentifier.*;
import static com.mercedesbenz.sechub.test.FlakyOlderThanTestWorkaround.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

import com.mercedesbenz.sechub.domain.scan.TestScanDomainFileSupport;
import com.mercedesbenz.sechub.domain.scan.product.config.DefaultProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ProductResultRepository.class, ProductResultRepositoryDBTest.SimpleTestConfiguration.class })
public class ProductResultRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductResultRepository repositoryToTest;

    @Before
    public void before() {
    }

    @Test
    public void test_data_4_jobs_delete_1_day_still_has_deleted_2() throws Exception {
        /* prepare */
        DeleteProductResultTestData testData = new DeleteProductResultTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);

        /* execute */
        int deleted = repositoryToTest.deleteResultsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
    }

    @Test
    public void test_data_4_jobs_delete_1_day_still_has_2() throws Exception {
        /* prepare */
        DeleteProductResultTestData testData = new DeleteProductResultTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_1_day);
        /* execute */
        int deleted = repositoryToTest.deleteResultsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(2, deleted, testData, olderThan);
        List<ProductResult> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(2, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_still_has_1() throws Exception {
        /* prepare */
        DeleteProductResultTestData testData = new DeleteProductResultTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = repositoryToTest.deleteResultsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
        List<ProductResult> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(1, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_delete_1_day_before_plus1_second_has_3_deleted() throws Exception {
        /* prepare */
        DeleteProductResultTestData testData = new DeleteProductResultTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_1_day.plusSeconds(1);

        /* execute */
        int deleted = repositoryToTest.deleteResultsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(3, deleted, testData, olderThan);
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_90_days_still_has_4() throws Exception {
        /* prepare */
        DeleteProductResultTestData testData = new DeleteProductResultTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = olderThanForDelete(testData.before_90_days);

        /* execute */
        int deleted = repositoryToTest.deleteResultsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(0, deleted, testData, olderThan);
        List<ProductResult> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job1_90_days_before_created));
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(4, allJobsNow.size());
    }

    @Test
    public void test_data_4_jobs_oldest_90_days_delete_89_days() throws Exception {
        /* prepare */
        DeleteProductResultTestData testData = new DeleteProductResultTestData();
        testData.createAndCheckAvailable();

        LocalDateTime olderThan = testData.before_89_days;

        /* execute */
        int deleted = repositoryToTest.deleteResultsOlderThan(olderThan);
        repositoryToTest.flush();

        /* test */
        assertDeleted(1, deleted, testData, olderThan);
        List<ProductResult> allJobsNow = repositoryToTest.findAll();
        assertTrue(allJobsNow.contains(testData.job2_2_days_before_created));
        assertTrue(allJobsNow.contains(testData.job3_1_day_before_created));
        assertTrue(allJobsNow.contains(testData.job4_now_created));
        assertEquals(3, allJobsNow.size());
    }

    @Test
    public void given_3_stored_product_results_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        UUID job1_project1 = UUID.randomUUID();
        UUID job2_project2 = UUID.randomUUID();
        UUID job3_project1 = UUID.randomUUID();

        ProductResult result1 = new ProductResult(job1_project1, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "result1");
        ProductResult result2 = new ProductResult(job2_project2, "project2", new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "result2");
        ProductResult result3 = new ProductResult(job3_project1, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "result3");

        repositoryToTest.save(result1);
        repositoryToTest.save(result2);
        repositoryToTest.save(result3);

        /* check preconditions */
        assertEquals(3, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(job2_project2));

        /* execute */
        repositoryToTest.deleteAllResultsForProject("project1");

        /* test */
        assertEquals(1, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(job2_project2));
    }

    @Test
    public void given_2_stored_results_find_for_given_executor_confing_with_uuid_returns_both_results() throws Exception {
        /* prepare */
        UUID job1_project1 = UUID.randomUUID();

        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        ProductExecutorConfigInfo configUUIDProvider1 = mock(ProductExecutorConfigInfo.class);
        when(configUUIDProvider1.getUUID()).thenReturn(uuid1);

        ProductExecutorConfigInfo configUUIDProvider2 = mock(ProductExecutorConfigInfo.class);
        when(configUUIDProvider2.getUUID()).thenReturn(uuid2);

        ProductResult result1 = new ProductResult(job1_project1, "project1", new DefaultProductExecutorConfigInfo(ProductIdentifier.PDS_CODESCAN, uuid1),
                "result1");
        ProductResult result2 = new ProductResult(job1_project1, "project2", new DefaultProductExecutorConfigInfo(ProductIdentifier.PDS_CODESCAN, uuid2),
                "result2");

        repositoryToTest.save(result1);
        repositoryToTest.save(result2);

        /* check preconditions */
        assertEquals(2, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(job1_project1)); // just check its written...

        /* execute */
        List<ProductResult> results1 = repositoryToTest.findProductResults(job1_project1, configUUIDProvider1);
        List<ProductResult> results2 = repositoryToTest.findProductResults(job1_project1, configUUIDProvider2);

        /* test */
        assertEquals(1, results1.size());
        assertTrue(results1.contains(result1));

        assertEquals(1, results2.size());
        assertTrue(results2.contains(result2));
    }

    @Test
    public void findAllProductResults_results_is_executable_and_returns_an_empty_list_for_pds_webscan_and_netsparker() throws Exception {

        /* prepare */

        /* execute */
        List<ProductResult> results = repositoryToTest.findAllProductResults(UUID.randomUUID(), NETSPARKER, PDS_WEBSCAN);

        /* test */
        assertNotNull(results);
        assertTrue(results.isEmpty());

    }

    @Test
    public void findAllProductResults_results_is_executable_and_returns_pds_webscan_result_for_pds_webscan_and_netsparker() throws Exception {

        /* prepare */
        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
        entityManager.persistAndFlush(result1);

        /* execute */
        List<ProductResult> results = repositoryToTest.findAllProductResults(secHubJobUUID, NETSPARKER, PDS_WEBSCAN);

        /* test */
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(result1, results.iterator().next());

    }

    @Test
    public void findAllProductResults_is_executable_and_returns_pds_webscan_result_for_pds_webscan_and_netsparker() throws Exception {

        /* prepare */
        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
        entityManager.persistAndFlush(result1);

        /* execute */
        List<ProductResult> results = repositoryToTest.findAllProductResults(secHubJobUUID, NETSPARKER, PDS_WEBSCAN);

        /* test */
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(result1, results.iterator().next());

    }

    @Test
    public void findProduct_results_is_executable_and_returns_pds_webscan_result_for_pds_webscan() throws Exception {

        /* prepare */
        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
        entityManager.persistAndFlush(result1);

        /* execute */
        List<ProductResult> results = repositoryToTest.findAllProductResults(secHubJobUUID, PDS_WEBSCAN);

        /* test */
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(result1, results.iterator().next());

    }

    @Test
    public void findProduct_results_is_executable_and_returns_empty_result_for_netsparker() throws Exception {

        /* prepare */
        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
        entityManager.persistAndFlush(result1);

        /* execute */
        List<ProductResult> results = repositoryToTest.findAllProductResults(secHubJobUUID, NETSPARKER);

        /* test */
        assertNotNull(results);
        assertTrue(results.isEmpty());

    }

    @Test
    public void a_standard_netsparker_output_can_be_persisted() {
        /* prepare */
        String path = "netsparker/netsparker_v1.0.40.109_testresult1.xml";
        String netsparkerContent = TestScanDomainFileSupport.getTestfileSupport().loadTestFile(path);

        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.NETSPARKER),
                netsparkerContent);

        /* execute */
        ProductResult result = repositoryToTest.save(result1);

        /* test */
        assertNotNull(result);
        assertNotNull(result.uUID);
        assertEquals(result, result1);
        assertEquals(netsparkerContent, result.getResult());

    }

    private void assertDeleted(int expected, int deleted, DeleteProductResultTestData testData, LocalDateTime olderThan) {
        if (deleted == expected) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ProductResult> all = repositoryToTest.findAll();
        sb.append("Delete call did return ").append(deleted).append(" uploadMaximumBytes was ").append(expected).append("\n");
        sb.append("The remaining entries are:\n");
        for (ProductResult info : all) {
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

    private String describe(ProductResult info, DeleteProductResultTestData data) {
        return resolveName(info.started, data) + " - created: " + info.started + "\n";
    }

    private String resolveName(LocalDateTime time, DeleteProductResultTestData data) {
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

    private class DeleteProductResultTestData {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before_89_days = now.minusDays(89);
        LocalDateTime before_90_days = now.minusDays(90);
        LocalDateTime before_3_days = now.minusDays(3);
        LocalDateTime before_1_day = now.minusDays(1);

        ProductResult job1_90_days_before_created;
        ProductResult job2_2_days_before_created;
        ProductResult job3_1_day_before_created;
        ProductResult job4_now_created;

        private void createAndCheckAvailable() {
            job1_90_days_before_created = create(before_90_days, ProductIdentifier.CHECKMARX);
            job2_2_days_before_created = create(before_3_days, ProductIdentifier.PDS_CODESCAN);
            job3_1_day_before_created = create(before_1_day, ProductIdentifier.PDS_WEBSCAN);
            job4_now_created = create(now, ProductIdentifier.PDS_INFRASCAN);

            // check preconditions
            repositoryToTest.flush();
            assertEquals(4, repositoryToTest.count());
            List<ProductResult> allJobsNow = repositoryToTest.findAll();
            assertTrue(allJobsNow.contains(job1_90_days_before_created));
            assertTrue(allJobsNow.contains(job2_2_days_before_created));
            assertTrue(allJobsNow.contains(job3_1_day_before_created));
            assertTrue(allJobsNow.contains(job4_now_created));
        }

        private ProductResult create(LocalDateTime since, ProductIdentifier identifier) {
            ProductResult jobInformation = new ProductResult();
            jobInformation.projectId = "project1";
            jobInformation.productIdentifier = identifier;
            jobInformation.secHubJobUUID = UUID.randomUUID();
            jobInformation.started = since;
            entityManager.persist(jobInformation);
            entityManager.flush();
            return jobInformation;
        }
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
