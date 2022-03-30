// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecHubSourceDataConfigurationTest {

    @Test
    void when_unique_name_not_set_it_is_null() {
        /* execute */
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();

        /* test */
        assertNull(config1.getUniqueName());
    }

    @Test
    void when_unique_name_not_set_in_JSON_it_is_null() {
        String json = "{ }";

        /* execute */
        SecHubSourceDataConfiguration config1 = JSONConverter.get().fromJSON(SecHubSourceDataConfiguration.class, json);

        /* test */
        assertNull(config1.getUniqueName());
    }

    @Test
    void when_unique_name_is_set_it_is_used() {
        /* prepare */
        SecHubSourceDataConfiguration config1 = new SecHubSourceDataConfiguration();

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
        SecHubSourceDataConfiguration config = JSONConverter.get().fromJSON(SecHubSourceDataConfiguration.class, json);

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
        SecHubSourceDataConfiguration config = new SecHubSourceDataConfiguration();
        config.setUniqueName("name1");

        /* execute */
        String json = JSONConverter.get().toJSON(config);

        /* test */
        String expected = "{\"name\":\"name1\"}";
        assertEquals(expected, json);
    }

}
