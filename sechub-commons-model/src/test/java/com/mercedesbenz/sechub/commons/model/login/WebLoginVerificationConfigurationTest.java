// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;

class WebLoginVerificationConfigurationTest {

    @Test
    void default_values_are_as_expected() {
        /* execute */
        WebLoginVerificationConfiguration defaultConfig = new WebLoginVerificationConfiguration();

        /* test */
        assertNull(defaultConfig.getUrl());
        assertEquals(WebLoginVerificationConfiguration.DEFAULT_VALUE_RESPONSE_CODE, defaultConfig.getResponseCode());
    }

    @Test
    void default_values_are_used_correctly_during_json_serialization_and_deserialization() {
        /* prepare */
        WebLoginVerificationConfiguration expectedConfig = new WebLoginVerificationConfiguration();

        /* execute */
        String json = JSONConverter.get().toJSON(expectedConfig);
        WebLoginVerificationConfiguration config = JSONConverter.get().fromJSON(WebLoginVerificationConfiguration.class, json);

        /* test */
        assertNull(config.getUrl());
        assertEquals(200, config.getResponseCode());
        assertEquals(config.getUrl(), expectedConfig.getUrl());
        assertEquals(config.getResponseCode(), expectedConfig.getResponseCode());
    }

    @Test
    void custom_values_are_used_correctly_during_json_serialization_and_deserialization() throws MalformedURLException {
        /* prepare */
        WebLoginVerificationConfiguration expectedConfig = new WebLoginVerificationConfiguration();
        URL url = new URL("http://example.com");
        expectedConfig.setUrl(url);
        expectedConfig.setResponseCode(204);

        /* execute */
        String json = JSONConverter.get().toJSON(expectedConfig);
        WebLoginVerificationConfiguration config = JSONConverter.get().fromJSON(WebLoginVerificationConfiguration.class, json);

        /* test */
        assertEquals(url, config.getUrl());
        assertEquals(204, config.getResponseCode());
        assertEquals(config.getUrl(), expectedConfig.getUrl());
        assertEquals(config.getResponseCode(), expectedConfig.getResponseCode());
    }

}