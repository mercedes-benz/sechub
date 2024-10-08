// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;

class WebLoginTOTPConfigurationTest {

    @Test
    void default_values_are_as_expected() {
        /* execute */
        WebLoginTOTPConfiguration defaultConfig = new WebLoginTOTPConfiguration();

        /* test */
        assertEquals(null, defaultConfig.getSeed());
        assertEquals(WebLoginTOTPConfiguration.DEFAULT_VALIDITY_IN_SECONDS, defaultConfig.getValidityInSeconds());
        assertEquals(WebLoginTOTPConfiguration.DEFAULT_TOKEN_LENGTH, defaultConfig.getTokenLength());
        assertEquals(WebLoginTOTPConfiguration.DEFAULT_HASH_ALGORITHM, defaultConfig.getHashAlgorithm());
    }

    @Test
    void default_values_are_used_correctly_during_json_serialization_and_deserialization() {
        /* prepare */
        WebLoginTOTPConfiguration expectedConfig = new WebLoginTOTPConfiguration();

        /* execute */
        String json = JSONConverter.get().toJSON(expectedConfig);
        WebLoginTOTPConfiguration config = JSONConverter.get().fromJSON(WebLoginTOTPConfiguration.class, json);

        /* test */
        assertEquals(config.getSeed(), expectedConfig.getSeed());
        assertEquals(config.getValidityInSeconds(), expectedConfig.getValidityInSeconds());
        assertEquals(config.getTokenLength(), expectedConfig.getTokenLength());
        assertEquals(config.getHashAlgorithm(), expectedConfig.getHashAlgorithm());
    }

    @Test
    void custom_values_are_used_correctly_during_json_serialization_and_deserialization() {
        /* prepare */
        WebLoginTOTPConfiguration expectedConfig = new WebLoginTOTPConfiguration();
        expectedConfig.setSeed("example");
        expectedConfig.setValidityInSeconds(45);
        expectedConfig.setTokenLength(9);
        expectedConfig.setHashAlgorithm(TOTPHashAlgorithm.HMAC_SHA512);

        /* execute */
        String json = JSONConverter.get().toJSON(expectedConfig);
        WebLoginTOTPConfiguration config = JSONConverter.get().fromJSON(WebLoginTOTPConfiguration.class, json);

        /* test */
        assertEquals(config.getSeed(), expectedConfig.getSeed());
        assertEquals(config.getValidityInSeconds(), expectedConfig.getValidityInSeconds());
        assertEquals(config.getTokenLength(), expectedConfig.getTokenLength());
        assertEquals(config.getHashAlgorithm(), expectedConfig.getHashAlgorithm());
    }

}
