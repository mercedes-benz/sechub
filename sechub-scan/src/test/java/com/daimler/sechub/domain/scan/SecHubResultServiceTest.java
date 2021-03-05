// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.ProductResultRepository;
import com.daimler.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.report.ScanReportToSecHubResultTransformer;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class SecHubResultServiceTest {

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();

	private SecHubResultService serviceToTest;
	private ScanReportToSecHubResultTransformer reportTransformer;
	private ProductResultRepository productResultRepository;
	private SecHubExecutionContext context;
	private UUID secHubJobUUID;
    private SecHubResultMerger resultMerger;

	@Before
	public void before() throws Exception {
		secHubJobUUID = UUID.randomUUID();
		context = mock(SecHubExecutionContext.class);
		when(context.getSechubJobUUID()).thenReturn(secHubJobUUID);

		reportTransformer = mock(ScanReportToSecHubResultTransformer.class);
		productResultRepository = mock(ProductResultRepository.class);
		resultMerger = mock(SecHubResultMerger.class);

		serviceToTest = new SecHubResultService();
		serviceToTest.transformers = Collections.singletonList(reportTransformer);
		serviceToTest.productResultRepository = productResultRepository;
		serviceToTest.resultMerger=resultMerger;
	}

	@Test
	public void when_product_result_repository_returns_empty_list__sechub_execution_is_thrown_with_message()
			throws Exception {
		/* prepare */
		when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(new ArrayList<>());

		/* test */
		expected.expect(SecHubExecutionException.class);
		expected.expectMessage("No report result found for:" + secHubJobUUID);

		/* execute */
		serviceToTest.createResult(context);
	}

	@Test
	public void when_product_result_repository_returns_only_netsparker_result__sechub_execution_is_thrown_with_message()
			throws Exception {
		/* prepare */
		ProductResult scanResult = new ProductResult(secHubJobUUID, "project1",  new WithoutProductExecutorConfigInfo(ProductIdentifier.NETSPARKER), "scan-result");

		when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any()))
				.thenReturn(Arrays.asList(scanResult));

		/* test */
		expected.expect(SecHubExecutionException.class);

		/* execute */
		serviceToTest.createResult(context);
	}

	@Test
	public void when_product_result_repository_returns_only_sereco_report_result__sechubResultFromTransformer_is_returned()
			throws Exception {
		/* prepare */
		SecHubResult secHubResult = new SecHubResult();
		ProductResult scanResult = new ProductResult(secHubJobUUID, "project1",  new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "scan-result");
		when(reportTransformer.canTransform(ProductIdentifier.SERECO)).thenReturn(true);
		when(reportTransformer.transform(scanResult)).thenReturn(secHubResult);

		when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any()))
				.thenReturn(Arrays.asList(scanResult));
		when(resultMerger.merge(null,secHubResult)).thenReturn(secHubResult);

		/* execute */
		SecHubResult result = serviceToTest.createResult(context);

		/* test */
		assertEquals(secHubResult, result);

	}

}
