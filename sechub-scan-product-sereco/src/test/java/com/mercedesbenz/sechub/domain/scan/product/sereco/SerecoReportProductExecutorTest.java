// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.sereco.Sereco;
import com.mercedesbenz.sechub.sereco.Workspace;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

class SerecoReportProductExecutorTest {

    private static final String TEST_SERECO_RESULT = "test-sereco-result";
    private SerecoReportProductExecutor executorToTest;
    private ProductResultRepository productResultRepository;
    private Sereco sechubReportCollector;
    private ProductExecutorContext executorContext;
    private SecHubExecutionContext context;
    private UUID jobUUID;

    @BeforeEach
    void beforeEach() {

        productResultRepository = mock(ProductResultRepository.class);
        sechubReportCollector = mock(Sereco.class);
        Workspace workspace = mock(Workspace.class);
        when(sechubReportCollector.createWorkspace(any())).thenReturn(workspace);
        when(workspace.createReport()).thenReturn(TEST_SERECO_RESULT);

        executorToTest = new SerecoReportProductExecutor();
        executorToTest.productResultRepository = productResultRepository;
        executorToTest.sechubReportCollector = sechubReportCollector;

        jobUUID = UUID.randomUUID();

        executorContext = mock(ProductExecutorContext.class);
        context = mock(SecHubExecutionContext.class);
        SecHubConfiguration configuration = new SecHubConfiguration();
        ProductExecutorConfig executorConfig = mock(ProductExecutorConfig.class);

        configuration.setProjectId("the-project-id");

        when(context.getConfiguration()).thenReturn(configuration);
        when(context.getSechubJobUUID()).thenReturn(jobUUID);
        when(executorContext.getExecutorConfig()).thenReturn(executorConfig);
        when(executorConfig.getProductIdentifier()).thenReturn(ProductIdentifier.SERECO);
    }

    @Test
    void no_db_results_even_dummy_fallback_result_has_started_and_ended() throws Exception {
        /* prepare */
        List<ProductResult> repositoryResults = new ArrayList<>();
        when(productResultRepository.findAllProductResults(jobUUID, executorToTest.getSupportedProducts())).thenReturn(repositoryResults);

        /* execute */
        List<ProductResult> productResults = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(1, productResults.size());
        ProductResult result = productResults.iterator().next();
        assertEquals("{}", result.getResult());
        assertNotNull(result.getStarted());
        assertNotNull(result.getEnded());

    }

    @Test
    void db_result_has_started_and_ended() throws Exception {
        /* prepare */
        /* prepare */
        List<ProductResult> repositoryResults = new ArrayList<>();
        ProductResult result1 = new ProductResult(jobUUID, "project1", executorContext.getExecutorConfig(), "result1");
        repositoryResults.add(result1);

        when(productResultRepository.findAllProductResults(jobUUID, executorToTest.getSupportedProducts())).thenReturn(repositoryResults);

        /* execute */
        List<ProductResult> productResults = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(1, productResults.size());
        ProductResult result = productResults.iterator().next();
        assertEquals(TEST_SERECO_RESULT, result.getResult());
        assertNotNull(result.getStarted());
        assertNotNull(result.getEnded());
    }

}
