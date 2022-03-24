package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecHubBinaryDataConfigurationTest {

    @Test
    void when_unique_name_not_set_it_is_null() {
        /* execute */
        SecHubBinaryDataConfiguration config1 = new SecHubBinaryDataConfiguration();

        /* test */
        assertNull(config1.getUniqueName());
    }
    
    @Test
    void when_unique_name_not_set_in_JSON_it_is_a_uuid() {
        String json = "{ }";

        /* execute */
        SecHubBinaryDataConfiguration config1 = JSONConverter.get().fromJSON(SecHubBinaryDataConfiguration.class, json);

        /* test */
        assertNull(config1.getUniqueName());
    }

    @Test
    void when_unique_name_is_set_it_is_used() {
        /* prepare */
        SecHubBinaryDataConfiguration config1 = new SecHubBinaryDataConfiguration();

        /* execute */
        config1.setUniqueName("name1");

        /* test */
        assertEquals("name1", config1.getUniqueName());
    }
    
    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_by_from_json() {
        /* prepare */
        String json = "{ \"name\" : \"my-unique-name1\" }";

        /* execute */
        SecHubBinaryDataConfiguration config = JSONConverter.get().fromJSON(SecHubBinaryDataConfiguration.class, json);

        /* test */
        assertEquals("my-unique-name1", config.getUniqueName());
    }

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_by_to_json() {
        SecHubBinaryDataConfiguration config = new SecHubBinaryDataConfiguration();
        config.setUniqueName("name1");
        
        /* execute */
        String json = JSONConverter.get().toJSON(config);

        /* test */
        String expected = "{\"name\":\"name1\"}";
        assertEquals(expected, json);
    }

}
