package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecHubMessagesListTest {

    @Test
    void json_only_brackets_can_be_converted() {
        /* prepare */
        String json = "{}";

        /* execute */
        SecHubMessagesList result = SecHubMessagesList.fromJSONString(json);

        /* test */
        assertNotNull(result);
        assertEquals(0, result.getSecHubMessages().size());
    }

    @Test
    void new_empty_message_list__to_json_has_empty_messages() {
        /* prepare */
        SecHubMessagesList list = new SecHubMessagesList();

        /* execute */
        String json = list.toJSON();

        /* test */
        assertNotNull(json);
        assertEquals("{\"secHubMessages\":[],\"type\":\"sechubMessagesList\"}", json);
    }

    @Test
    void simple_serialize_and_deserialize_back_has_equal_messages() {
        /* prepare */
        SecHubMessagesList list = new SecHubMessagesList();
        SecHubMessage message = new SecHubMessage();
        message.setText("this is a text");
        message.setType(SecHubMessageType.INFO);
        list.getSecHubMessages().add(message);

        String json = list.toJSON();

        /* execute */
        SecHubMessagesList list2 = SecHubMessagesList.fromJSONString(json);

        /* test */
        assertNotNull(list2);
        assertEquals(1, list2.getSecHubMessages().size());
        SecHubMessage list2Message = list2.getSecHubMessages().iterator().next();
        assertEquals(message, list2Message);
    }

}
