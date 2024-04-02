// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
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

    @Test
    void api_definition_url_is_handled_correctly() throws MalformedURLException {
        SecHubWebScanApiConfiguration config = new SecHubWebScanApiConfiguration();
        URL apiDefinitionUrl = new URL("https://example.com/api/v1/swagger/");
        config.setApiDefinitionUrl(apiDefinitionUrl);

        /* execute */
        String json = JSONConverter.get().toJSON(config);
        SecHubWebScanApiConfiguration apiConfig = JSONConverter.get().fromJSON(SecHubWebScanApiConfiguration.class, json);

        /* test */
        String expected = "{\"apiDefinitionUrl\":\"https://example.com/api/v1/swagger/\",\"use\":[]}";
        assertEquals(expected, json);
        assertEquals(config.getApiDefinitionUrl(), apiConfig.getApiDefinitionUrl());
    }

}
