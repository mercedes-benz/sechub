// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class AbstractProductExecutionServiceTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private static final ProductIdentifier USED_PRODUCT_IDENTIFIER = ProductIdentifier.NESSUS;
    private static final int USED_PRODUCT_EXECUTOR_VERSION = 1;

    private AbstractProductExecutionService serviceToTest;

    private UUIDTraceLogID traceLogID;
    private SecHubExecutionContext context;
    private ProductExecutor executor;
    private List<ProductExecutor> executors;
    private ProductResultRepository productResultRepository;
    private Logger logger;
    private UUID sechubJobUUID;
    private ProductExecutorContextFactory productExecutorContextFactory;
    private ProductExecutorContext productExecutorContext;
    private ProductExecutorConfig config1;

    @Before
    public void before() throws Exception {
        serviceToTest = new TestImplAbstractProductExecutionService();
        serviceToTest.allAvailableProductExecutors = new ArrayList<>();

        SecHubConfiguration configuration = new SecHubConfiguration();
        configuration.setProjectId("projectid1");

        sechubJobUUID = UUID.randomUUID();
        logger = mock(Logger.class);
        traceLogID = mock(UUIDTraceLogID.class);

        executors = new ArrayList<>();
        executor = mock(ProductExecutor.class);
        when(executor.getIdentifier()).thenReturn(USED_PRODUCT_IDENTIFIER);
        when(executor.getVersion()).thenReturn(USED_PRODUCT_EXECUTOR_VERSION);

        executors.add(executor);
        context = mock(SecHubExecutionContext.class);
        when(context.getSechubJobUUID()).thenReturn(sechubJobUUID);
        when(context.getConfiguration()).thenReturn(configuration);

        ProductExecutorConfigRepository productExecutorConfigRepository = mock(ProductExecutorConfigRepository.class);
        serviceToTest.productExecutorConfigRepository = productExecutorConfigRepository;

        config1 = new ProductExecutorConfig(USED_PRODUCT_IDENTIFIER, 0, new ProductExecutorConfigSetup());
        when(productExecutorConfigRepository.findExecutableConfigurationsForProject(any(), eq(USED_PRODUCT_IDENTIFIER), eq(USED_PRODUCT_EXECUTOR_VERSION)))
                .thenReturn(Arrays.asList(config1));

        productResultRepository = mock(ProductResultRepository.class);
        serviceToTest.productResultRepository = productResultRepository;

        productExecutorContextFactory = mock(ProductExecutorContextFactory.class);
        serviceToTest.productExecutorContextFactory = productExecutorContextFactory;

        productExecutorContext = mock(ProductExecutorContext.class);
        when(productExecutorContextFactory.create(any(), any(), any(), any())).thenReturn(productExecutorContext);
    }

    @Test
    public void executeAndPersistResults_a_null_result_throws_no_error_but_does_error_logging() throws Exception {
        /* prepare */
        when(executor.execute(eq(context), any())).thenReturn(null);

        /* execute */
        serviceToTest.runOnAllAvailableExecutors(executors, context, traceLogID);

        /* test */
        verify(productResultRepository, never()).save(any());
        verify(logger).error(any(), eq(USED_PRODUCT_IDENTIFIER), eq(traceLogID));
    }

    @Test
    public void executeAndPersistResults_a_non_null_result_saves_the_result_no_error_logging() throws Exception {
        /* prepare */
        ProductResult result = mock(ProductResult.class);

        ArgumentCaptor<ProductExecutorContext> executorContext = ArgumentCaptor.forClass(ProductExecutorContext.class);

        when(executor.execute(eq(context), executorContext.capture())).thenReturn(Collections.singletonList(result));

        /* execute */
        serviceToTest.runOnAllAvailableExecutors(executors, context, traceLogID);

        /* test */
        verify(productResultRepository).findProductResults(sechubJobUUID, config1);
        verify(productExecutorContext).getExecutorConfig();
        verify(productExecutorContext).persist(result);
        verify(logger, never()).error(any(), eq(USED_PRODUCT_IDENTIFIER), eq(traceLogID));

    }

    @Test
    public void service_uses_scan_type_filter_for_product_executor_registration_and_returns_filtered_result_for_executors() throws Exception {
        List<ProductExecutor> filteredExecutors = new ArrayList<>();
        serviceToTest.scanTypeFilter = mock(ScanTypeBasedProductExecutorFilter.class);
        when(serviceToTest.scanTypeFilter.filter(any())).thenReturn(filteredExecutors);

        ProductExecutor productExecutor1 = mock(ProductExecutor.class);
        ProductExecutor productExecutor2 = mock(ProductExecutor.class);

        serviceToTest.allAvailableProductExecutors.add(productExecutor1);
        serviceToTest.allAvailableProductExecutors.add(productExecutor2);

        filteredExecutors.add(productExecutor2);

        /* execute */
        serviceToTest.registerProductExecutorsForScanTypes();
        List<ProductExecutor> result = serviceToTest.getProductExecutors();

        /* test */
        assertEquals(1, result.size());
        assertTrue(result.contains(productExecutor2));
    }

    @Test
    public void sechub_execution_error_on_execution_shall_not_break_the_build_but_safe_fallbackresult() throws Exception {
        /* prepare */
        ArgumentCaptor<ProductResult> productResultCaptor = ArgumentCaptor.forClass(ProductResult.class);

        SecHubExecutionException exception = new SecHubExecutionException("an-error occurred on execution, but this should not break at all!");
        when(executor.execute(context, productExecutorContext)).thenThrow(exception);

        /* execute */
        serviceToTest.runOnAllAvailableExecutors(executors, context, traceLogID);

        /* test */
        verify(productResultRepository).findProductResults(sechubJobUUID, config1);
        verify(productExecutorContext).persist(productResultCaptor.capture());

        ProductResult captured = productResultCaptor.getValue();
        assertEquals(USED_PRODUCT_IDENTIFIER, captured.getProductIdentifier());
        assertEquals("", captured.getResult());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(stringCaptor.capture(), eq(USED_PRODUCT_IDENTIFIER), eq(traceLogID), eq(exception));
        assertTrue(stringCaptor.getValue().startsWith("Product executor failed"));

    }

    @Test
    public void runtime__error_on_execution_shall_not_break_the_build() throws Exception {
        /* prepare */
        ArgumentCaptor<ProductResult> productResultCaptor = ArgumentCaptor.forClass(ProductResult.class);

        RuntimeException exception = new RuntimeException("an-error occurred on execution, but this should not break at all!");
        when(executor.execute(context, productExecutorContext)).thenThrow(exception);

        /* execute */
        serviceToTest.runOnAllAvailableExecutors(executors, context, traceLogID);

        /* test */
        verify(productExecutorContext).persist(productResultCaptor.capture());

        ProductResult captured = productResultCaptor.getValue();
        assertEquals(USED_PRODUCT_IDENTIFIER, captured.getProductIdentifier());
        assertEquals("", captured.getResult());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(stringCaptor.capture(), eq(USED_PRODUCT_IDENTIFIER), eq(traceLogID), eq(exception));
        assertTrue(stringCaptor.getValue().startsWith("Product executor failed:"));

    }

    @Test
    public void runtime_errors_in_persistence_shall_break_the_build() throws Exception {
        /* test */
        expected.expect(RuntimeException.class);

        ProductResult result = mock(ProductResult.class);
        /* prepare */
        when(executor.execute(context, productExecutorContext)).thenReturn(Collections.singletonList(result));
        doThrow(new RuntimeException("save-failed")).when(productExecutorContext).persist(result);

        /* execute */
        serviceToTest.runOnAllAvailableExecutors(executors, context, traceLogID);

    }

    private class TestImplAbstractProductExecutionService extends AbstractProductExecutionService {

        private ScanType scanType;

        @Override
        protected boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID, SecHubConfiguration configuration) {
            return true;
        }

        @Override
        Logger getMockableLog() {
            return logger;
        }

        @Override
        public ScanType getScanType() {
            if (scanType == null) {
                scanType = ScanType.UNKNOWN;
            }
            return scanType;
        }

    }
}
