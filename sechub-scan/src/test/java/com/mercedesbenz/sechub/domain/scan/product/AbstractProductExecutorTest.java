// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

class AbstractProductExecutorTest {

    private ProductExecutorContext executorContext;
    private ProductResult productResult;
    private AdapterExecutionResult adapterResult;
    private List<SecHubMessage> messages;

    @BeforeEach
    void beforeEach() {
        productResult = mock(ProductResult.class);
        adapterResult = mock(AdapterExecutionResult.class);
        executorContext = mock(ProductExecutorContext.class);

        messages = new ArrayList<>();

        when(adapterResult.getProductMessages()).thenReturn(messages);
        when(executorContext.getCurrentProductResult()).thenReturn(productResult);
    }

    @Test
    void updateCurrentProductResult_when_no_messages_an_empty_sechub_messages_list_json_is_set() throws Exception {
        /* prepare */
        AbstractProductExecutor executor = new TestAbstractProductExecutor();

        /* execute */
        executor.updateCurrentProductResult(adapterResult, executorContext);

        /* test */
        verify(productResult).setMessages(new SecHubMessagesList().toJSON());
    }

    @Test
    void updateCurrentProductResult_when_2_messages_sechub_messages_list_json_containing_them_is_set() throws Exception {
        /* prepare */
        AbstractProductExecutor executor = new TestAbstractProductExecutor();
        SecHubMessage message1 = new SecHubMessage(SecHubMessageType.WARNING, "a warning");
        SecHubMessage message2 = new SecHubMessage(SecHubMessageType.ERROR, "a problem");

        messages.add(message1);
        messages.add(message2);

        /* execute */
        executor.updateCurrentProductResult(adapterResult, executorContext);

        /* test */
        verify(productResult).setMessages(new SecHubMessagesList(messages).toJSON());
    }

    private class TestAbstractProductExecutor extends AbstractProductExecutor {

        public TestAbstractProductExecutor() {
            super(ProductIdentifier.PDS_CODESCAN, 999);
        }

        @Override
        protected void customize(ProductExecutorData data) {

        }

        @Override
        protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
            return null;
        }

    }
}
