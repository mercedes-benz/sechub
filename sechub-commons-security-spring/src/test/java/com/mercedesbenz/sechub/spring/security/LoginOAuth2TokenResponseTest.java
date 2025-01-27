// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

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

class LoginOAuth2TokenResponseTest {

    private static final String responseJson;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            responseJson = Files.readString(Paths.get("src/test/resources/oauth2-token-response.json"));
        } catch (IOException e) {
            throw new TestAbortedException("Failed to prepare test", e);
        }
    }

    @Test
    void construct_login_oauth2_token_response_from_valid_json_is_successful() throws JsonProcessingException {
        /* execute */
        LoginOAuth2TokenResponse loginOAuth2TokenResponse = objectMapper.readValue(responseJson, LoginOAuth2TokenResponse.class);

        // assert
        assertThat(loginOAuth2TokenResponse).isNotNull();
        assertThat(loginOAuth2TokenResponse.getAccessToken()).isEqualTo(JsonPath.read(responseJson, "$.access_token"));
        assertThat(loginOAuth2TokenResponse.getRefreshToken()).isEqualTo(JsonPath.read(responseJson, "$.refresh_token"));
        assertThat(loginOAuth2TokenResponse.getIdToken()).isEqualTo(JsonPath.read(responseJson, "$.id_token"));
        int expiresIn = JsonPath.read(responseJson, "$.expires_in");
        long expiresInLong = Long.parseLong(String.valueOf(expiresIn));
        assertThat(loginOAuth2TokenResponse.getExpiresIn()).isEqualTo(expiresInLong);
        assertThat(loginOAuth2TokenResponse.getTokenType()).isEqualTo(JsonPath.read(responseJson, "$.token_type"));
    }

    @Test
    void construct_login_oauth2_token_response_from_valid_json_with_no_refresh_token_is_successful() throws JsonProcessingException {
        /* prepare */
        String jsonResponseJsonWithoutRefreshToken = removeJsonKeyAndValue("refresh_token");

        /* execute & test */

        /* @formatter:off */
        assertDoesNotThrow(() -> {
            LoginOAuth2TokenResponse tokenResponse = objectMapper.readValue(jsonResponseJsonWithoutRefreshToken, LoginOAuth2TokenResponse.class);
            assertThat(tokenResponse.getRefreshToken()).isNull();
        });
        /* @formatter:on */
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidResponseJsonProvider.class)
    void construct_login_oauth2_token_response_from_invalid_json_fails(String invalidResponseJson, String expectedErrMsg) {
        /* execute & test */

        /* @formatter:off */
        assertThatThrownBy(() -> objectMapper.readValue(invalidResponseJson, LoginOAuth2TokenResponse.class))
                .isInstanceOf(ValueInstantiationException.class)
                .hasMessageContaining(expectedErrMsg);
        /* @formatter:on */
    }

    private static String removeJsonKeyAndValue(String key) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseJson);

        if (rootNode instanceof ObjectNode) {
            ((ObjectNode) rootNode).remove(key);
        } else {
            throw new IllegalArgumentException("Invalid JSON");
        }

        return objectMapper.writeValueAsString(rootNode);
    }

    /* @formatter:off */
    private static class InvalidResponseJsonProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(removeJsonKeyAndValue("access_token"), "access_token must not be null"),
                    Arguments.of(removeJsonKeyAndValue("token_type"), "token_type must not be null"),
                    Arguments.of(removeJsonKeyAndValue("id_token"), "id_token must not be null"),
                    Arguments.of(removeJsonKeyAndValue("expires_in"), "expires_in must not be null"));
        }
    }
    /* @formatter:on */
}
