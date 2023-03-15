// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class SecHubWebScanApiConfigurationTest {

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_by_from_json() {
        /* prepare */
        String json = "{ \"use\" : [ \"openapi-reference1\"] }";

        /* execute */
        SecHubWebScanApiConfiguration config = JSONConverter.get().fromJSON(SecHubWebScanApiConfiguration.class, json);

        /* test */
        Set<String> set = config.getNamesOfUsedDataConfigurationObjects();
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains("openapi-reference1"));
    }

    /**
     * We have defined the json attribute "use" for the interface
     * "SecHubDataConfigurationUsageByName" but not for the class - here we check
     * that the "use" attribute works as expected
     */
    @Test
    void json_attribute_use_is_handled_correctly_by_to_json() {
        SecHubWebScanApiConfiguration config = new SecHubWebScanApiConfiguration();
        config.getNamesOfUsedDataConfigurationObjects().add("ref1");
        config.getNamesOfUsedDataConfigurationObjects().add("ref2");

        /* execute */
        String json = JSONConverter.get().toJSON(config);

        /* test */
        String expected = "{\"use\":[\"ref1\",\"ref2\"]}";
        assertEquals(expected, json);
    }

}
