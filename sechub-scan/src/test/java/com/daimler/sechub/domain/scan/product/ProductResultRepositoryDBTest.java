// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static com.daimler.sechub.domain.scan.product.ProductIdentifier.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

import com.daimler.sechub.domain.scan.ScanDomainTestFileSupport;
import com.daimler.sechub.domain.scan.product.config.DefaultProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes= {ProductResultRepository.class, ProductResultRepositoryDBTest.SimpleTestConfiguration.class})
public class ProductResultRepositoryDBTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ProductResultRepository repositoryToTest;

	@Before
	public void before() {
	}


	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{

	}

	@Test
	public void given_3_stored_product_results_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
		/* prepare */
		UUID job1_project1 = UUID.randomUUID();
		UUID job2_project2 = UUID.randomUUID();
		UUID job3_project1 = UUID.randomUUID();


		ProductResult result1 = new ProductResult(job1_project1,"project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "result1");
		ProductResult result2 = new ProductResult(job2_project2,"project2",  new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "result2");
		ProductResult result3 =new ProductResult(job3_project1,"project1",  new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "result3");

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


        ProductResult result1 = new ProductResult(job1_project1,"project1", new DefaultProductExecutorConfigInfo(ProductIdentifier.PDS_CODESCAN, uuid1), "result1");
        ProductResult result2 = new ProductResult(job1_project1,"project2",  new DefaultProductExecutorConfigInfo(ProductIdentifier.PDS_CODESCAN,uuid2), "result2");

        repositoryToTest.save(result1);
        repositoryToTest.save(result2);

        /* check preconditions */
        assertEquals(2, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(job1_project1)); // just check its written...

        /* execute */
        List<ProductResult> results1 = repositoryToTest.findProductResults(job1_project1, configUUIDProvider1);
        List<ProductResult> results2 = repositoryToTest.findProductResults(job1_project1, configUUIDProvider2);

        /* test */
        assertEquals(1,results1.size());
        assertTrue(results1.contains(result1));

        assertEquals(1,results2.size());
        assertTrue(results2.contains(result2));
    }

	@Test
	public void findAllProductResults_results_is_executable_and_returns_an_empty_list_for_pds_webscan_and_netsparker()
			throws Exception {

		/* prepare */

		/* execute */
		List<ProductResult> results = repositoryToTest.findAllProductResults(UUID.randomUUID(), NETSPARKER, PDS_WEBSCAN);

		/* test */
		assertNotNull(results);
		assertTrue(results.isEmpty());

	}

	@Test
	public void findAllProductResults_results_is_executable_and_returns_pds_webscan_result_for_pds_webscan_and_netsparker()
			throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID,"project1",   new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
		entityManager.persistAndFlush(result1);

		/* execute */
		List<ProductResult> results = repositoryToTest.findAllProductResults(secHubJobUUID, NETSPARKER, PDS_WEBSCAN);

		/* test */
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(result1, results.iterator().next());

	}
	@Test
	public void findAllProductResults_is_executable_and_returns_pds_webscan_result_for_pds_webscan_and_netsparker()
	        throws Exception {
	    
	    /* prepare */
	    UUID secHubJobUUID = UUID.randomUUID();
	    ProductResult result1 = new ProductResult(secHubJobUUID,"project1",   new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
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
		ProductResult result1 = new ProductResult(secHubJobUUID, "project1",  new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
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
		ProductResult result1 = new ProductResult(secHubJobUUID, "project1",  new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), "result");
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
		String netsparkerContent = ScanDomainTestFileSupport.getTestfileSupport().loadTestFile(path);

		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, "project1",  new WithoutProductExecutorConfigInfo(ProductIdentifier.NETSPARKER), netsparkerContent);

		/* execute */
		ProductResult result = repositoryToTest.save(result1);

		/* test */
		assertNotNull(result);
		assertNotNull(result.uUID);
		assertEquals(result, result1);
		assertEquals(netsparkerContent, result.getResult());

	}

}
