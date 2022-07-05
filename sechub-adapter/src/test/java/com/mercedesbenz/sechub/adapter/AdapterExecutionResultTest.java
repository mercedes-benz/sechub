package com.mercedesbenz.sechub.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

class AdapterExecutionResultTest {

    @Test
    void product_result_set_no_messages() {
        /* execute */
        AdapterExecutionResult result = new AdapterExecutionResult("productResult1");

        /* test */
        assertEquals("productResult1", result.getProductResult());

        List<SecHubMessage> productMessages = result.getProductMessages();
        assertNotNull(productMessages);
        assertTrue(productMessages.isEmpty());
    }

    @Test
    void product_result_null_no_messages() {
        /* execute */
        AdapterExecutionResult result = new AdapterExecutionResult(null);

        /* test */
        assertEquals(null, result.getProductResult());

        List<SecHubMessage> productMessages = result.getProductMessages();
        assertNotNull(productMessages);
        assertTrue(productMessages.isEmpty());
    }

    @Test
    void product_result_set_two_messages() {

        /* prepare */
        SecHubMessage message1 = new SecHubMessage(SecHubMessageType.INFO, "test-info");
        SecHubMessage message2 = new SecHubMessage(SecHubMessageType.WARNING, "test-warning");

        List<SecHubMessage> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        /* execute */
        AdapterExecutionResult result = new AdapterExecutionResult("productResult1", messages);

        /* test */
        assertEquals("productResult1", result.getProductResult());

        List<SecHubMessage> productMessages = result.getProductMessages();
        assertNotNull(productMessages);
        assertEquals(2, productMessages.size());

        assertTrue(productMessages.contains(message1));
        assertTrue(productMessages.contains(message2));
    }

}
