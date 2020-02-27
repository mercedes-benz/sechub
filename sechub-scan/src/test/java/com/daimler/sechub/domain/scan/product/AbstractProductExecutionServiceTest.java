// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public class AbstractProductExecutionServiceTest {

	private static final ProductIdentifier USED_PRODUCT_IDENTIFIER = ProductIdentifier.FARRADAY;
	private AbstractProductExecutionService serviceToTest;
	private UUIDTraceLogID traceLogID;
	private SecHubExecutionContext context;
	private ProductExecutor executor;
	private List<ProductExecutor> executors;
	private ProductResultRepository productResultRepository;
	private Logger logger;
	private UUID sechubJobUUID;

	@Rule
	public ExpectedException expected = ExpectedException.none();
    private ProductExecutorContext executorContext;
    private ProductResultTransactionService transactionService;
    private ProductResult result1;

	@Before
	public void before() throws Exception {
		SecHubConfiguration configuration = new SecHubConfiguration();
		configuration.setProjectId("projectid1");

		sechubJobUUID = UUID.randomUUID();
		logger=mock(Logger.class);
		executorContext = mock(ProductExecutorContext.class);
		traceLogID=mock(UUIDTraceLogID.class);

		serviceToTest = new TestImplAbstractProductExecutionService();
		executors = new ArrayList<>();
		executor = mock(ProductExecutor.class);
		when(executor.getIdentifier()).thenReturn(USED_PRODUCT_IDENTIFIER);

		executors.add(executor);
		context = mock(SecHubExecutionContext.class);
		when(context.getSechubJobUUID()).thenReturn(sechubJobUUID);
		when(context.getConfiguration()).thenReturn(configuration);

		productResultRepository=mock(ProductResultRepository.class);
		serviceToTest.productResultRepository=productResultRepository;
		
		transactionService=mock(ProductResultTransactionService.class);
		serviceToTest.transactionService=transactionService;

	}

	@Test
	public void executeAndPersistResults_a_null_result_throws_no_error_but_does_error_logging() throws Exception{
		/* prepare */
		when(executor.execute(context,executorContext)).thenReturn(null);

		/* execute */
		serviceToTest.executeAndPersistResults(executors, context, traceLogID);

		/* test */
		verify(productResultRepository, never()).save(any());
		verify(logger).error(any(), eq(USED_PRODUCT_IDENTIFIER), eq(traceLogID));
	}

	@Test
	public void executeAndPersistResults_a_non_null_result_saves_the_result_no_error_logging() throws Exception{
		ProductResult result = mock(ProductResult.class);
		/* prepare */
		when(executor.execute(context,executorContext)).thenReturn(Collections.singletonList(result));

		/* execute */
		serviceToTest.executeAndPersistResults(executors, context, traceLogID);

		/* test */
		verify(productResultRepository).findProductResults(sechubJobUUID,USED_PRODUCT_IDENTIFIER);
		verify(executorContext).persist(result);
		verify(logger,never()).error(any(), eq(USED_PRODUCT_IDENTIFIER), eq(traceLogID));

	}

	@Test
	public void sechub_execution_error_on_execution_shall_not_break_the_build_but_safe_fallbackresult() throws Exception{
		ArgumentCaptor<ProductResult> productResultCaptor = ArgumentCaptor.forClass(ProductResult.class);
		/* prepare */
		SecHubExecutionException exception = new SecHubExecutionException("an-error occurred on execution, but this should not break at all!");
		when(executor.execute(context,executorContext)).thenThrow(exception);

		/* execute */
		serviceToTest.executeAndPersistResults(executors, context, traceLogID);

		/* test */
		verify(productResultRepository).findProductResults(sechubJobUUID,USED_PRODUCT_IDENTIFIER);
		verify(executorContext).persist(productResultCaptor.capture());

		ProductResult captured = productResultCaptor.getValue();
		assertEquals(USED_PRODUCT_IDENTIFIER, captured.getProductIdentifier());
		assertEquals("", captured.getResult());

		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(logger).error(stringCaptor.capture(), eq(exception));
		assertTrue(stringCaptor.getValue().startsWith("Product executor failed:"+USED_PRODUCT_IDENTIFIER));

	}

	@Test
	public void runtime__error_on_execution_shall_not_break_the_build() throws Exception{
		ArgumentCaptor<ProductResult> productResultCaptor = ArgumentCaptor.forClass(ProductResult.class);
		/* prepare */
		RuntimeException exception = new RuntimeException("an-error occurred on execution, but this should not break at all!");
		when(executor.execute(context,executorContext)).thenThrow(exception);

		/* execute */
		serviceToTest.executeAndPersistResults(executors, context, traceLogID);

		/* test */
		verify(executorContext).persist(productResultCaptor.capture());

		ProductResult captured = productResultCaptor.getValue();
		assertEquals(USED_PRODUCT_IDENTIFIER, captured.getProductIdentifier());
		assertEquals("", captured.getResult());

		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(logger).error(stringCaptor.capture(), eq(exception));
		assertTrue(stringCaptor.getValue().startsWith("Product executor failed:"+USED_PRODUCT_IDENTIFIER));

	}

	@Test
	public void runtime_errors_in_persistence_shall_break_the_build() throws Exception{
		/* test */
		expected.expect(RuntimeException.class);

		ProductResult result = mock(ProductResult.class);
		/* prepare */
		when(executor.execute(context,executorContext)).thenReturn(Collections.singletonList(result));
		doThrow(new RuntimeException("save-failed")).when(executorContext).persist(result);

		/* execute */
		serviceToTest.executeAndPersistResults(executors, context, traceLogID);

	}

	private class TestImplAbstractProductExecutionService extends AbstractProductExecutionService{

		@Override
		protected boolean isExecutionNecessary(SecHubExecutionContext context, UUIDTraceLogID traceLogID,
				SecHubConfiguration configuration) {
			return true;
		}

		@Override
		Logger getMockableLog() {
			return logger;
		}

	}
}
