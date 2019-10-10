// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static com.daimler.sechub.domain.scan.product.ProductIdentifier.*;
import static org.junit.Assert.*;

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

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes= {ProductResultRepository.class,ProductResultRepositoryDBTest.SimpleTestConfiguration.class})
public class ProductResultRepositoryDBTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ProductResultRepository jobRepository;

	@Before
	public void before() {
	}


	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{

	}
	@Test
	public void findProduct_results_is_executable_and_returns_an_empty_list_for_faraday_and_netsparker()
			throws Exception {

		/* prepare */

		/* execute */
		List<ProductResult> results = jobRepository.findProductResults(UUID.randomUUID(), NETSPARKER, FARRADAY);

		/* test */
		assertNotNull(results);
		assertTrue(results.isEmpty());

	}

	@Test
	public void findProduct_results_is_executable_and_returns_faraday_result_for_faraday_and_netsparker()
			throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, ProductIdentifier.FARRADAY, "result");
		entityManager.persistAndFlush(result1);

		/* execute */
		List<ProductResult> results = jobRepository.findProductResults(secHubJobUUID, NETSPARKER, FARRADAY);

		/* test */
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(result1, results.iterator().next());

	}

	@Test
	public void findProduct_results_is_executable_and_returns_faraday_result_for_faraday() throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, ProductIdentifier.FARRADAY, "result");
		entityManager.persistAndFlush(result1);

		/* execute */
		List<ProductResult> results = jobRepository.findProductResults(secHubJobUUID, FARRADAY);

		/* test */
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(result1, results.iterator().next());

	}

	@Test
	public void findProduct_results_is_executable_and_returns_empty_result_for_netsparker() throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, ProductIdentifier.FARRADAY, "result");
		entityManager.persistAndFlush(result1);

		/* execute */
		List<ProductResult> results = jobRepository.findProductResults(secHubJobUUID, NETSPARKER);

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
		ProductResult result1 = new ProductResult(secHubJobUUID, ProductIdentifier.NETSPARKER, netsparkerContent);

		/* execute */
		ProductResult result = jobRepository.save(result1);

		/* test */
		assertNotNull(result);
		assertNotNull(result.uUID);
		assertEquals(result, result1);
		assertEquals(netsparkerContent, result.getResult());

	}

}
