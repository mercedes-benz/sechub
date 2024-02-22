package com.mercedesbenz.sechub.domain.scan.product;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;

class PrepareProductExecutionServiceImplTest {

    private PrepareProductExecutionServiceImpl serviceToTest;
    private SecHubExecutionContext context;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PrepareProductExecutionServiceImpl();
        context = mock(SecHubExecutionContext.class);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "unknown", "SECHUB_PREPARE_RESULT;status=FAILED" })
    void markPrepareFailed_called_on_context_by_not_valid_or_prepare_failed(String productResultAsString) {
        /* prepare */
        ProductResult result = new ProductResult();
        result.setResult(productResultAsString);

        serviceToTest.afterProductResultsStored(List.of(result), context);

        /* test */
        verify(context).markPrepareFailed();
    }

    @Test
    void markPrepareFailed_NOT_called_on_context_when_prepare_done() {
        /* prepare */
        ProductResult result = new ProductResult();
        result.setResult(new PrepareResult(PrepareStatus.OK).toString());

        serviceToTest.afterProductResultsStored(List.of(result), context);

        /* test */
        verify(context, never()).markPrepareFailed();
    }

}
