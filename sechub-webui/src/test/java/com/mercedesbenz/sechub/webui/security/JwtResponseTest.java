// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

class JwtResponseTest {

    private static final String jwtResponseJson;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            jwtResponseJson = Files.readString(Paths.get("src/test/resources/jwt-response.json"));
        } catch (IOException e) {
            throw new TestAbortedException("Failed to prepare test", e);
        }
    }

    @Test
    void construct_jwt_response_from_valid_json_is_successful() throws JsonProcessingException {
        // execute
        JwtResponse jwtResponse = objectMapper.readValue(jwtResponseJson, JwtResponse.class);

        // assert
        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.getAccessToken()).isEqualTo(JsonPath.read(jwtResponseJson, "$.access_token"));
        assertThat(jwtResponse.getRefreshToken()).isEqualTo(JsonPath.read(jwtResponseJson, "$.refresh_token"));
        assertThat(jwtResponse.getIdToken()).isEqualTo(JsonPath.read(jwtResponseJson, "$.id_token"));
        int expiresIn = JsonPath.read(jwtResponseJson, "$.expires_in");
        long expiresInLong = Long.parseLong(String.valueOf(expiresIn));
        assertThat(jwtResponse.getExpiresIn()).isEqualTo(expiresInLong);
        assertThat(jwtResponse.getTokenType()).isEqualTo(JsonPath.read(jwtResponseJson, "$.token_type"));
    }

    @Test
    void construct_jwt_response_from_valid_json_with_no_refresh_token_is_successful() throws JsonProcessingException {
        // prepare
        String jwtResponseJsonWithoutRefreshToken = removeJsonKeyAndValue("refresh_token");

        // execute & assert

        // @formatter:off
        assertDoesNotThrow(() -> {
            JwtResponse jwtResponse = objectMapper.readValue(jwtResponseJsonWithoutRefreshToken, JwtResponse.class);
            assertThat(jwtResponse.getRefreshToken()).isNull();
        });
        // @formatter:on
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidJwtResponseJsonProvider.class)
    void construct_jwt_response_from_invalid_json_fails(String invalidJwtResponseJson, String expectedErrMsg) {
        // execute & assert

        // @formatter:off
        assertThatThrownBy(() -> objectMapper.readValue(invalidJwtResponseJson, JwtResponse.class))
                .isInstanceOf(ValueInstantiationException.class)
                .hasMessageContaining(expectedErrMsg);
        // @formatter:on
    }

    private static String removeJsonKeyAndValue(String key) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jwtResponseJson);

        if (rootNode instanceof ObjectNode) {
            ((ObjectNode) rootNode).remove(key);
        } else {
            throw new IllegalArgumentException("Invalid JSON");
        }

        return objectMapper.writeValueAsString(rootNode);
    }

    private static class InvalidJwtResponseJsonProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(Arguments.of(removeJsonKeyAndValue("access_token"), "access_token must not be null"),
                    Arguments.of(removeJsonKeyAndValue("token_type"), "token_type must not be null"),
                    Arguments.of(removeJsonKeyAndValue("id_token"), "id_token must not be null"),
                    Arguments.of(removeJsonKeyAndValue("expires_in"), "expires_in must not be null"));
        }
    }
}
