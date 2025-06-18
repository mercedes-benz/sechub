// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private AES256Encryption aes256Encryption;
    private StatelessAuthorizationRequestRepository repositoryToTest;
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;

    @BeforeEach
    void setUp() {
        aes256Encryption = mock(AES256Encryption.class);
        repositoryToTest = new StatelessAuthorizationRequestRepository(aes256Encryption);
        httpRequest = mock(HttpServletRequest.class);
        httpResponse = mock(HttpServletResponse.class);

        // Mock the AES256Encryption methods by turning strings into byte arrays and
        // byte arrays back into strings
        when(aes256Encryption.encrypt(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().getBytes());
        when(aes256Encryption.decrypt(any(byte[].class))).thenAnswer(inv -> new String((byte[]) inv.getArgument(0)));
    }

    @Test
    void save_and_load_authorization_request_returns_expected_values() throws Exception {
        /* prepare */
        OAuth2AuthorizationRequest expected = OAuth2AuthorizationRequest.authorizationCode().authorizationUri("https://auth.example.com").clientId("client-id")
                .redirectUri("https://redirect.example.com").scopes(Set.of("openid", "profile")).state("state123")
                .additionalParameters(Map.of("customParam", "customValue")).authorizationRequestUri("https://auth.example.com")
                .attributes(Map.of("key1", "value1")).build();

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        /* execute 1 */
        repositoryToTest.saveAuthorizationRequest(expected, httpRequest, httpResponse);

        // Capture the cookie that was added to the response and add it to the request
        verify(httpResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        when(httpRequest.getCookies()).thenReturn(new Cookie[] { cookie });

        /* execute 2 */
        OAuth2AuthorizationRequest request = repositoryToTest.loadAuthorizationRequest(httpRequest);

        /* test */
        assertThat(cookie.getName()).isEqualTo("SECHUB_OAUTH2_AUTHORIZATION_REQUEST");

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
        OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest.authorizationCode().authorizationUri("https://auth.example.com").clientId("client-id")
                .redirectUri("https://redirect.example.com").scopes(Set.of("openid", "profile")).state("state123")
                .additionalParameters(Map.of("customParam", "customValue")).authorizationRequestUri("https://auth.example.com")
                .attributes(Map.of("key1", "value1")).build();

        // get original json from OAuth2AuthorizationRequest
        ObjectMapper mapper = new ObjectMapper();
        String originalJson = mapper.writeValueAsString(request);

        /* execute */
        String json = repositoryToTest.serializeAuthorizationRequest(request);

        /* test */
        assertThat(json).contains("\"authorizationGrantType\":{\"value\":\"authorization_code\"}");
        assertThat(json).doesNotContain("\"grantType\"");
        assertThat(json).isNotEqualTo(originalJson);
        assertThat(originalJson).contains("\"grantType\":{\"value\":\"authorization_code\"}");
        assertThat(originalJson).doesNotContain("\"authorizationGrantType\"");
    }
}
