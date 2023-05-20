// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

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

import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.report.ReportProductResultTransformer;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class SecHubReportProductTransformerServiceTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private SecHubReportProductTransformerService serviceToTest;
    private ReportProductResultTransformer reportTransformer;
    private ProductResultRepository productResultRepository;
    private SecHubExecutionContext context;
    private UUID secHubJobUUID;
    private ReportTransformationResultMerger resultMerger;

    @Before
    public void before() throws Exception {
        secHubJobUUID = UUID.randomUUID();
        context = mock(SecHubExecutionContext.class);
        when(context.getSechubJobUUID()).thenReturn(secHubJobUUID);
        when(context.getConfiguration()).thenReturn(new SecHubConfiguration());

        reportTransformer = mock(ReportProductResultTransformer.class);
        productResultRepository = mock(ProductResultRepository.class);
        resultMerger = mock(ReportTransformationResultMerger.class);

        serviceToTest = new SecHubReportProductTransformerService();
        serviceToTest.transformers = Collections.singletonList(reportTransformer);
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.resultMerger = resultMerger;
    }

    @Test
    public void when_product_result_repository_returns_empty_list__sechub_execution_is_thrown_with_message() throws Exception {
        /* prepare */
        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(new ArrayList<>());

        /* test */
        expected.expect(SecHubExecutionException.class);
        expected.expectMessage("No report result found for:" + secHubJobUUID);

        /* execute */
        serviceToTest.createResult(context);
    }

    @Test
    public void when_product_result_repository_returns_only_netsparker_result__sechub_execution_is_thrown_with_message() throws Exception {
        /* prepare */
        ProductResult scanResult = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.NETSPARKER),
                "scan-result");

        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(Arrays.asList(scanResult));

        /* test */
        expected.expect(SecHubExecutionException.class);

        /* execute */
        serviceToTest.createResult(context);
    }

    @Test
    public void when_product_result_repository_returns_only_sereco_report_result__sechubResultFromTransformer_is_returned() throws Exception {
        /* prepare */
        ReportTransformationResult transformationResult = new ReportTransformationResult();
        ProductResult scanResult = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "scan-result");
        when(reportTransformer.canTransform(ProductIdentifier.SERECO)).thenReturn(true);
        when(reportTransformer.transform(scanResult)).thenReturn(transformationResult);

        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(Arrays.asList(scanResult));
        when(resultMerger.merge(null, transformationResult)).thenReturn(transformationResult);

        /* execute */
        ReportTransformationResult result = serviceToTest.createResult(context);

        /* test */
        assertEquals(transformationResult, result);

    }

}
