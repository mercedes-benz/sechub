// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.jayway.jsonpath.JsonPath;

class LoginOAuth2AccessTokenClientTest {

    /* @formatter:off */
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
    private static final LoginOAuth2AccessTokenClient client = new LoginOAuth2AccessTokenClient(restTemplate);
    private static final ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(Constants.REGISTRATION_ID)
            .clientId(Constants.CLIENT_ID)
            .clientSecret(Constants.CLIENT_SECRET)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(Constants.REDIRECT_URI)
            .tokenUri(Constants.TOKEN_URI)
            .authorizationUri(Constants.AUTHORIZATION_URI)
            .build();
    private static final OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(Constants.AUTHORIZATION_URI)
            .clientId(Constants.CLIENT_ID)
            .redirectUri(Constants.REDIRECT_URI)
            .scopes(Set.of(Constants.OPENID))
            .state(Constants.STATE)
            .build();
    private static final OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(Constants.CODE)
            .redirectUri(Constants.REDIRECT_URI)
            .state(Constants.STATE)
            .build();
    /* @formatter:on */
    private static final OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse);
    private static final OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest = new OAuth2AuthorizationCodeGrantRequest(clientRegistration,
            authorizationExchange);
    private static final String tokenResponseJson;

    static {
        try {
            tokenResponseJson = Files.readString(Paths.get("src/test/resources/oauth2-token-response.json"));
        } catch (IOException e) {
            throw new TestAbortedException("Failed to prepare test", e);
        }
    }

    @BeforeEach
    void beforeEach() {
        mockServer.reset();
    }

    @Test
    void get_token_response_executes_correctly_formatted_http_request() {
        /* prepare */
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
        String authorizationHeaderValue = getBasicAuthHeaderValue(clientRegistration.getClientId(), clientRegistration.getClientSecret());
        /* @formatter:off */
        mockServer.expect(MockRestRequestMatchers.requestTo(tokenUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, authorizationHeaderValue))
                .andExpect(MockRestRequestMatchers.content().formData(getMultiValueMap(authorizationCodeGrantRequest)))
                .andRespond(MockRestResponseCreators.withSuccess(tokenResponseJson, MediaType.APPLICATION_JSON));
        /* @formatter:on */

        /* execute */
        client.getTokenResponse(authorizationCodeGrantRequest);

        /* test */
        mockServer.verify();
    }

    @Test
    void get_token_response_returns_o_auth_access_token_as_expected() {
        /* prepare */
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
        /* @formatter:off */
        mockServer.expect(MockRestRequestMatchers.requestTo(tokenUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(tokenResponseJson, MediaType.APPLICATION_JSON));
        /* @formatter:on */

        /* execute */
        OAuth2AccessTokenResponse oAuth2AccessTokenResponse = client.getTokenResponse(authorizationCodeGrantRequest);

        /* test */
        mockServer.verify();
        assertThat(oAuth2AccessTokenResponse.getAccessToken().getTokenValue()).isEqualTo(JsonPath.read(tokenResponseJson, "$.access_token"));
        assertThat(oAuth2AccessTokenResponse.getAccessToken().getTokenType().getValue()).isEqualTo(JsonPath.read(tokenResponseJson, "$.token_type"));
        assertThat(oAuth2AccessTokenResponse.getAdditionalParameters().get(Constants.ID_TOKEN)).isEqualTo(JsonPath.read(tokenResponseJson, "$.id_token"));
        assertThat(oAuth2AccessTokenResponse.getRefreshToken()).isNotNull();
        assertThat(oAuth2AccessTokenResponse.getRefreshToken().getTokenValue()).isEqualTo(JsonPath.read(tokenResponseJson, "$.refresh_token"));
        int expiresIn = JsonPath.read(tokenResponseJson, "$.expires_in");
        long expiresInLong = Long.parseLong(String.valueOf(expiresIn));
        assertThat(oAuth2AccessTokenResponse.getAccessToken().getExpiresAt())
                .isAfterOrEqualTo(Instant.now().minus(expiresInLong, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void get_token_response_handles_rest_client_exception_well() {
        /* prepare */
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
        /* @formatter:off */
        mockServer.expect(MockRestRequestMatchers.requestTo(tokenUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.APPLICATION_JSON));

        /* execute & test */

        assertThatThrownBy(() -> client.getTokenResponse(authorizationCodeGrantRequest))
                .isExactlyInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Failed to get access token response");
        /* @formatter:on */
    }

    private static MultiValueMap<String, String> getMultiValueMap(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        OAuth2AuthorizationExchange authorizationExchange = authorizationGrantRequest.getAuthorizationExchange();
        String code = authorizationExchange.getAuthorizationResponse().getCode();
        String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();

        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, Constants.GRANT_TYPE_VALUE);
        formParameters.add(OAuth2ParameterNames.CODE, code);
        formParameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);

        return formParameters;
    }

    private static String getBasicAuthHeaderValue(String clientId, String clientSecret) {
        String clientIdClientSecret = Constants.CLIENT_ID_CLIENT_SECRET_FORMAT.formatted(clientId, clientSecret);
        String clientIdClientSecretB64Encoded = Base64.getEncoder().encodeToString(clientIdClientSecret.getBytes());
        return String.format(Constants.BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT, clientIdClientSecretB64Encoded);
    }

    private static final class Constants {
        private static final String REGISTRATION_ID = "registration-id";
        private static final String CLIENT_ID = "client-id";
        private static final String CLIENT_SECRET = "client-secret";
        private static final String GRANT_TYPE_VALUE = "authorization_code";
        private static final String BASIC_AUTHORIZATION_HEADER_VALUE_FORMAT = "Basic %s";
        private static final String CLIENT_ID_CLIENT_SECRET_FORMAT = "%s:%s";
        private static final String APPLICATION_FORM_URLENCODED_VALUE = "%s;charset=%s".formatted(MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                StandardCharsets.UTF_8);
        private static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/registration-id";
        private static final String TOKEN_URI = "https://localhost:8080/oauth2/token";
        private static final String AUTHORIZATION_URI = "https://localhost:8080/oauth2/authorize";
        private static final String STATE = "state";
        private static final String CODE = "code";
        private static final String OPENID = "openid";
        private static final String ID_TOKEN = "id_token";

    }
}