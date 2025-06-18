// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class StatelessAuthorizationRequestRepositoryTest {

    private static final Duration COOKIE_AGE = Duration.ofMinutes(1);
    private final static String COOKIE_NAME = "SECHUB_OAUTH2_AUTHORIZATION_REQUEST";
    private final static String SERIALIZED_REQUEST = """
            {"authorizationUri":"https://auth.example.com","responseType":{"value":"code"},"clientId":"client-id","redirectUri":"https://redirect.example.com","scopes":["openid"],"state":"state123","additionalParameters":{"customParam":"customValue"},"authorizationRequestUri":"https://auth.example.com","attributes":{"key1":"value1"},"authorizationGrantType":{"value":"authorization_code"}}""";
    private final static String SERIALIZED_REQUEST_ENCRYPTED = "encryptedRequest";
    private final static String SERIALIZED_REQUEST_ENCODED = Base64.getEncoder().encodeToString(SERIALIZED_REQUEST_ENCRYPTED.getBytes(StandardCharsets.UTF_8));
    private AES256Encryption aes256Encryption;
    private StatelessAuthorizationRequestRepository repositoryToTest;
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private ArgumentCaptor<Cookie> cookieCaptor;

    @BeforeEach
    void beforeEach() {
        aes256Encryption = mock();
        httpRequest = mock();
        httpResponse = mock();
        cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        // Always return the same encrypted bytes for the specific serialized request
        // string
        when(aes256Encryption.encrypt(SERIALIZED_REQUEST)).thenReturn(SERIALIZED_REQUEST_ENCRYPTED.getBytes(StandardCharsets.UTF_8));
        when(aes256Encryption.decrypt(SERIALIZED_REQUEST_ENCRYPTED.getBytes(StandardCharsets.UTF_8))).thenReturn(SERIALIZED_REQUEST);

        repositoryToTest = new StatelessAuthorizationRequestRepository(aes256Encryption);
    }

    @Test
    void save_authorization_request_sets_expected_cookie() throws Exception {
        /* prepare */
        OAuth2AuthorizationRequest request = buildOAuth2AuthorizationRequest();

        /* execute */
        repositoryToTest.saveAuthorizationRequest(request, httpRequest, httpResponse);

        /* test */
        verify(httpResponse).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertThat(cookie.getMaxAge()).isEqualTo(COOKIE_AGE.getSeconds());
        assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(SERIALIZED_REQUEST_ENCODED);
        assertThat(cookie.getPath()).isEqualTo("/");

        verify(aes256Encryption).encrypt(anyString());
    }

    @Test
    void load_authorization_returns_expected_request_from_cookie() throws Exception {
        /* prepare */
        OAuth2AuthorizationRequest expected = buildOAuth2AuthorizationRequest();

        repositoryToTest.saveAuthorizationRequest(expected, httpRequest, httpResponse);
        verify(httpResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        when(httpRequest.getCookies()).thenReturn(new Cookie[] { cookie });

        /* execute */
        OAuth2AuthorizationRequest request = repositoryToTest.loadAuthorizationRequest(httpRequest);

        /* test */
        verify(aes256Encryption).decrypt(SERIALIZED_REQUEST_ENCRYPTED.getBytes());

        assertThat(request).isNotNull();
        assertThat(request.getAuthorizationUri()).isEqualTo(expected.getAuthorizationUri());
        assertThat(request.getClientId()).isEqualTo(expected.getClientId());
        assertThat(request.getRedirectUri()).isEqualTo(expected.getRedirectUri());
        assertThat(request.getScopes()).isEqualTo(expected.getScopes());
        assertThat(request.getState()).isEqualTo(expected.getState());
        assertThat(request.getAdditionalParameters()).isEqualTo(expected.getAdditionalParameters());
        assertThat(request.getAttributes()).isEqualTo(expected.getAttributes());
        assertThat(request.getAuthorizationRequestUri()).isEqualTo(expected.getAuthorizationRequestUri());
    }

    @Test
    void remove_authorization_request_removes_cookie() {
        /* prepare */
        OAuth2AuthorizationRequest request = buildOAuth2AuthorizationRequest();

        repositoryToTest.saveAuthorizationRequest(request, httpRequest, httpResponse);
        verify(httpResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        when(httpRequest.getCookies()).thenReturn(new Cookie[] { cookie });

        /* execute */
        OAuth2AuthorizationRequest removedRequest = repositoryToTest.removeAuthorizationRequest(httpRequest, httpResponse);

        /* test */
        assertThat(removedRequest).isNotNull();
        verify(httpResponse, times(2)).addCookie(cookieCaptor.capture());
        Cookie removedCookie = cookieCaptor.getValue();
        assertThat(removedCookie.getMaxAge()).isEqualTo(0);
        assertThat(removedCookie.getName()).isEqualTo(COOKIE_NAME);
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void remove_authorization_request_returns_null_when_no_cookie() {
        /* prepare */
        when(httpRequest.getCookies()).thenReturn(null);

        /* execute */
        OAuth2AuthorizationRequest request = repositoryToTest.removeAuthorizationRequest(httpRequest, httpResponse);

        /* test */
        assertThat(request).isNull();
    }

    @Test
    void serialize_authorization_request_handles_grant_type_field() throws Exception {
        /* prepare */
        OAuth2AuthorizationRequest request = buildOAuth2AuthorizationRequest();

        // get original json from OAuth2AuthorizationRequest
        ObjectMapper mapper = new ObjectMapper();
        String originalJson = mapper.writeValueAsString(request);

        /* execute */
        String json = repositoryToTest.serializeAuthorizationRequest(request);

        /* test */
        assertThat(json).isNotEqualTo(originalJson);
        assertThat(json).contains("\"authorizationGrantType\":{\"value\":\"authorization_code\"}");
        assertThat(json).doesNotContain("\"grantType\"");
        assertThat(originalJson).contains("\"grantType\":{\"value\":\"authorization_code\"}");
        assertThat(originalJson).doesNotContain("\"authorizationGrantType\"");
    }

    private static OAuth2AuthorizationRequest buildOAuth2AuthorizationRequest() {
        /* @formatter:off */
        return OAuth2AuthorizationRequest
                .authorizationCode()
                .authorizationUri("https://auth.example.com")
                .clientId("client-id")
                .redirectUri("https://redirect.example.com")
                .scopes(Set.of("openid"))
                .state("state123")
                .additionalParameters(Map.of("customParam", "customValue"))
                .authorizationRequestUri("https://auth.example.com")
                .attributes(Map.of("key1", "value1"))
                .build();
        /* @formatter:on */
    }
}
