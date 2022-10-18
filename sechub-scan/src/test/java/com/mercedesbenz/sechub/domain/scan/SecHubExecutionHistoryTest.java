package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;

class SecHubExecutionHistoryTest {

    private SecHubExecutionHistory historyToTest;

    @BeforeEach
    void beforeEach() {
        historyToTest = new SecHubExecutionHistory();
    }

    @Test
    void adding_two_elements_and_removing_them_results_in_empty_history() {
        /* prepare */
        ProductExecutorData data1 = mock(ProductExecutorData.class);
        ProductExecutorData data2 = mock(ProductExecutorData.class);

        ProductExecutor productExecutor1 = mock(ProductExecutor.class);
        ProductExecutor productExecutor2 = mock(ProductExecutor.class);

        /* execute 1 */
        SecHubExecutionHistoryElement element1 = historyToTest.remember(productExecutor1, data1);
        SecHubExecutionHistoryElement element2 = historyToTest.remember(productExecutor2, data2);

        /* test 1 */
        List<SecHubExecutionHistoryElement> elementList = historyToTest.getUnmodifiableElements();
        assertEquals(2, elementList.size());
        assertTrue(elementList.contains(element1));
        assertTrue(elementList.contains(element2));

        /* execute 2 */
        historyToTest.forget(element1);
        historyToTest.forget(element2);

        /* test 2 */
        List<SecHubExecutionHistoryElement> elementList2 = historyToTest.getUnmodifiableElements();
        assertEquals(0, elementList2.size());

    }

    @Test
    void adding_two_elements_and_removing_one_results_in_history_without_this_element() {
        /* prepare */
        ProductExecutorData data1 = mock(ProductExecutorData.class);
        ProductExecutorData data2 = mock(ProductExecutorData.class);

        ProductExecutor productExecutor1 = mock(ProductExecutor.class);
        ProductExecutor productExecutor2 = mock(ProductExecutor.class);

        /* execute */
        SecHubExecutionHistoryElement element1 = historyToTest.remember(productExecutor1, data1);
        SecHubExecutionHistoryElement element2 = historyToTest.remember(productExecutor2, data2);
        historyToTest.forget(element1);

        /* test */
        List<SecHubExecutionHistoryElement> elementList = historyToTest.getUnmodifiableElements();
        assertEquals(1, elementList.size());
        assertTrue(elementList.contains(element2));

    }

}
