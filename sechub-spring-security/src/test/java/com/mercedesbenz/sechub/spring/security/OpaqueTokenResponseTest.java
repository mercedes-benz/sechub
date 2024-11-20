// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opentest4j.TestAbortedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;

class OpaqueTokenResponseTest {

    private static final String opaqueTokenResponseJson;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            opaqueTokenResponseJson = Files.readString(Paths.get("src/test/resources/opaque-token-response.json"));
        } catch (IOException e) {
            throw new TestAbortedException("Failed to prepare test", e);
        }
    }

    @Test
    void construct_opaque_token_response_from_valid_json_is_successful() throws JsonProcessingException {
        /* prepare */
        Instant epoch = Instant.EPOCH;

        /* execute */
        OpaqueTokenResponse opaqueTokenResponse = objectMapper.readValue(opaqueTokenResponseJson, OpaqueTokenResponse.class);

        // assert
        assertThat(opaqueTokenResponse).isNotNull();
        assertThat(opaqueTokenResponse.isActive()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.active"));
        assertThat(opaqueTokenResponse.getScope()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.scope"));
        assertThat(opaqueTokenResponse.getClientId()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.client_id"));
        assertThat(opaqueTokenResponse.getClientType()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.client_type"));
        assertThat(opaqueTokenResponse.getUsername()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.username"));
        assertThat(opaqueTokenResponse.getTokenType()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.token_type"));
        assertThat(opaqueTokenResponse.getExpiresAt()).isAfter(epoch);
        assertThat(opaqueTokenResponse.getSubject()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.sub"));
        assertThat(opaqueTokenResponse.getAudience()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.aud"));
        assertThat(opaqueTokenResponse.getGroupType()).isEqualTo(JsonPath.read(opaqueTokenResponseJson, "$.group_type"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidOpaqueTokenResponseProvider.class)
    void construct_opaque_token_response_from_invalid_json_fails(String invalidOpaqueTokenResponseJson, String errMsg) throws JsonProcessingException {
        /* execute & test */

        /* @formatter:off */
        assertThatThrownBy(() -> objectMapper.readValue(invalidOpaqueTokenResponseJson, OpaqueTokenResponse.class))
                .isInstanceOf(ValueInstantiationException.class)
                .hasMessageContaining(errMsg);
        /* @formatter:on */
    }

    private static String removeJsonKeyAndValue(String key) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(opaqueTokenResponseJson);

        if (rootNode instanceof ObjectNode) {
            ((ObjectNode) rootNode).remove(key);
        } else {
            throw new IllegalArgumentException("Invalid JSON");
        }

        return objectMapper.writeValueAsString(rootNode);
    }

    /* @formatter:off */
    private static class InvalidOpaqueTokenResponseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(removeJsonKeyAndValue("active"), "Property 'active' must not be null"),
                    Arguments.of(removeJsonKeyAndValue("sub"), "Property 'sub' must not be null")
            );
        }
    }
    /* @formatter:on */
}
