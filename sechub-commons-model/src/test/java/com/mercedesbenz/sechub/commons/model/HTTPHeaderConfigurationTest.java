// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class HTTPHeaderConfigurationTest {

    @Test
    void new_http_header_configuration_instance_is_sensitive_on_default() {
        /* execute */
        HTTPHeaderConfiguration emptyHeaderConfig = new HTTPHeaderConfiguration();

        /* test */
        assertEquals(true, emptyHeaderConfig.isSensitive());
        assertNull(emptyHeaderConfig.getName());
        assertNull(emptyHeaderConfig.getValue());

        assertTrue(emptyHeaderConfig.isSensitive());
        assertTrue(emptyHeaderConfig.getOnlyForUrls().isEmpty());
        assertTrue(emptyHeaderConfig.getNamesOfUsedDataConfigurationObjects().isEmpty());
    }

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_without_password_set_by_from_json() {
        /* prepare */
        String json = "{ \"use\" : [ \"header-file-ref\"] }";

        /* execute */
        HTTPHeaderConfiguration config = JSONConverter.get().fromJSON(HTTPHeaderConfiguration.class, json);

        /* test */
        Set<String> set = config.getNamesOfUsedDataConfigurationObjects();
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains("header-file-ref"));
        assertNull(config.getName());
        assertNull(config.getValue());
        assertTrue(config.isSensitive());
        assertTrue(config.getOnlyForUrls().isEmpty());
    }

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_without_password_set_by_to_json() {
        HTTPHeaderConfiguration config = new HTTPHeaderConfiguration();
        config.getNamesOfUsedDataConfigurationObjects().add("header-file-ref");

        /* execute */
        String json = JSONConverter.get().toJSON(config);

        /* test */
        String expected = "{\"sensitive\":true,\"use\":[\"header-file-ref\"]}";
        assertEquals(expected, json);
        assertNull(config.getName());
        assertNull(config.getValue());
        assertTrue(config.isSensitive());
        assertTrue(config.getOnlyForUrls().isEmpty());
    }

}
