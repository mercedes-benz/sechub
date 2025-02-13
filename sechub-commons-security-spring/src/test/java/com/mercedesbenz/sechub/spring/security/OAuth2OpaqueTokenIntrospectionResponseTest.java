// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
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

class OAuth2OpaqueTokenIntrospectionResponseTest {

    private static final Duration DEFAULT_EXPIRES_IN = Duration.ofDays(1);

    private static final String jsonResponse;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            jsonResponse = Files.readString(Paths.get("src/test/resources/opaque-token-response.json"));
        } catch (IOException e) {
            throw new TestAbortedException("Failed to prepare test", e);
        }
    }

    @Test
    void construct_opaque_token_response_from_valid_json_is_successful() throws JsonProcessingException {
        /* prepare */
        Instant epoch = Instant.EPOCH;

        /* execute */
        OAuth2OpaqueTokenIntrospectionResponse response = objectMapper.readValue(jsonResponse, OAuth2OpaqueTokenIntrospectionResponse.class);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.isActive()).isEqualTo(JsonPath.read(jsonResponse, "$.active"));
        assertThat(response.getScope()).isEqualTo(JsonPath.read(jsonResponse, "$.scope"));
        assertThat(response.getClientId()).isEqualTo(JsonPath.read(jsonResponse, "$.client_id"));
        assertThat(response.getClientType()).isEqualTo(JsonPath.read(jsonResponse, "$.client_type"));
        assertThat(response.getUsername()).isEqualTo(JsonPath.read(jsonResponse, "$.username"));
        assertThat(response.getTokenType()).isEqualTo(JsonPath.read(jsonResponse, "$.token_type"));
        assertThat(response.getIssuedAt()).isAfter(epoch);
        assertThat(response.getExpiresAt()).isAfter(epoch);
        assertThat(response.getSubject()).isEqualTo(JsonPath.read(jsonResponse, "$.sub"));
        assertThat(response.getAudience()).isEqualTo(JsonPath.read(jsonResponse, "$.aud"));
        assertThat(response.getGroupType()).isEqualTo(JsonPath.read(jsonResponse, "$.group_type"));
    }

    @Test
    void construct_opaque_token_response_with_null_expires_at_uses_default_expires_in() throws JsonProcessingException {
        /* execute */
        OAuth2OpaqueTokenIntrospectionResponse response = objectMapper.readValue(removeJsonKeyAndValue("exp"), OAuth2OpaqueTokenIntrospectionResponse.class);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getExpiresAt()).isEqualTo(response.getIssuedAt().plus(DEFAULT_EXPIRES_IN));
    }

    @ParameterizedTest
    @ArgumentsSource(ValidOpaqueTokenResponseProvider.class)
    void construct_opaque_token_response_from_valid_json_with_nullable_properties_is_successful(String validOpaqueTokenResponseJson) {
        /* execute & test */
        assertDoesNotThrow(() -> objectMapper.readValue(validOpaqueTokenResponseJson, OAuth2OpaqueTokenIntrospectionResponse.class));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidOpaqueTokenResponseProvider.class)
    void construct_opaque_token_response_from_invalid_json_fails(String invalidOpaqueTokenResponseJson, String errMsg) throws JsonProcessingException {
        /* execute & test */

        /* @formatter:off */
        assertThatThrownBy(() -> objectMapper.readValue(invalidOpaqueTokenResponseJson, OAuth2OpaqueTokenIntrospectionResponse.class))
                .isInstanceOf(ValueInstantiationException.class)
                .hasMessageContaining(errMsg);
        /* @formatter:on */
    }

    private static String removeJsonKeyAndValue(String key) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        if (rootNode instanceof ObjectNode) {
            ((ObjectNode) rootNode).remove(key);
        } else {
            throw new IllegalArgumentException("Invalid JSON");
        }

        return objectMapper.writeValueAsString(rootNode);
    }

    /* @formatter:off */
    private static class ValidOpaqueTokenResponseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(removeJsonKeyAndValue("scope")),
                    Arguments.of(removeJsonKeyAndValue("client_id")),
                    Arguments.of(removeJsonKeyAndValue("client_type")),
                    Arguments.of(removeJsonKeyAndValue("username")),
                    Arguments.of(removeJsonKeyAndValue("token_type")),
                    Arguments.of(removeJsonKeyAndValue("exp")),
                    Arguments.of(removeJsonKeyAndValue("aud")),
                    Arguments.of(removeJsonKeyAndValue("group_type"))
            );
        }
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
