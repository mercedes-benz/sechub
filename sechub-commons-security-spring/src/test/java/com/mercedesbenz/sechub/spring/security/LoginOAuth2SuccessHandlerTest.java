// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class LoginOAuth2SuccessHandlerTest {

    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String ENCRYPTED_ACCESS_TOKEN = "this-is-an-encrypted-access-token";
    private static final byte[] ENCRYPTED_ACCESS_TOKEN_BYTES = ENCRYPTED_ACCESS_TOKEN.getBytes(StandardCharsets.UTF_8);
    private static final String ENCRYPTED_ACCESS_TOKEN_BASE64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_ACCESS_TOKEN_BYTES);
    private static final String PROVIDER = "example-provider";
    private static final String PRINCIPAL = "example-principal";
    private static final String ACCESS_TOKEN = "this-is-a-plain-access-token";
    private static final int DEFAULT_EXPIRY_SECONDS = 3600;
    private static final String BASE_PATH = "/";
    private static final String REDIRECT_URI = "https://example.org/redirect-uri";

    private static final LoginOAuth2Properties loginOAuth2Properties = Mockito.mock();
    private static final OAuth2AuthorizedClientService oAuth2AuthorizedClientService = Mockito.mock();
    private static final AES256Encryption aes256Encryption = Mockito.mock();
    private static final HttpServletRequest httpServletRequest = Mockito.mock();
    private static final HttpServletResponse httpServletResponse = Mockito.mock();
    private static final Authentication authentication = Mockito.mock();
    private static final OAuth2AuthorizedClient oauth2AuthorizedClient = Mockito.mock();
    private static final OAuth2AccessToken oAuth2AccessToken = Mockito.mock();
    private static final LoginOAuth2SuccessHandler loginOAuth2SuccessHandler = new LoginOAuth2SuccessHandler(loginOAuth2Properties,
            oAuth2AuthorizedClientService, aes256Encryption, REDIRECT_URI);

    @BeforeEach
    void beforeEach() {
        Mockito.reset(aes256Encryption, httpServletResponse);
        Mockito.when(loginOAuth2Properties.getProvider()).thenReturn(PROVIDER);
        Mockito.when(aes256Encryption.encrypt(ArgumentMatchers.anyString())).thenReturn(ENCRYPTED_ACCESS_TOKEN_BYTES);
        Mockito.when(oAuth2AuthorizedClientService.loadAuthorizedClient(PROVIDER, PRINCIPAL)).thenReturn(oauth2AuthorizedClient);
        Mockito.when(authentication.getName()).thenReturn(PRINCIPAL);
        Mockito.when(oauth2AuthorizedClient.getAccessToken()).thenReturn(oAuth2AccessToken);
        Mockito.when(oAuth2AccessToken.getTokenValue()).thenReturn(ACCESS_TOKEN);
    }

    @Test
    void on_authentication_success_sends_a_valid_redirect_containing_the_encrypted_access_token_cookie() throws IOException {
        /* prepare */
        Instant now = Instant.now();
        Mockito.when(oAuth2AccessToken.getIssuedAt()).thenReturn(now);
        Mockito.when(oAuth2AccessToken.getExpiresAt()).thenReturn(now.plusSeconds(60));
        int expirySeconds = 60;

        /* execute */
        loginOAuth2SuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

        /* test */
        Mockito.verify(aes256Encryption).encrypt(ACCESS_TOKEN);
        Mockito.verify(httpServletResponse).sendRedirect(REDIRECT_URI);
        ArgumentMatcher<Cookie> argumentMatcher = cookie -> {
            /* @formatter:off */
            if (!ACCESS_TOKEN_KEY.equals(cookie.getName())) return false;
            if (!ENCRYPTED_ACCESS_TOKEN_BASE64_ENCODED.equals(cookie.getValue())) return false;
            if (cookie.getMaxAge() != expirySeconds) return false;
            if (!cookie.isHttpOnly()) return false;
            if (!cookie.getSecure()) return false;
            return BASE_PATH.equals(cookie.getPath());
            /* @formatter:on */
        };
        Mockito.verify(httpServletResponse).addCookie(ArgumentMatchers.argThat(argumentMatcher));
    }

    @Test
    void on_authentication_success_assumes_default_expiry_when_expires_at_is_null() throws IOException {
        /* prepare */
        Instant now = Instant.now();
        Mockito.when(oAuth2AccessToken.getIssuedAt()).thenReturn(now);
        Mockito.when(oAuth2AccessToken.getExpiresAt()).thenReturn(null);

        /* execute */
        loginOAuth2SuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

        /* test */
        ArgumentMatcher<Cookie> argumentMatcher = cookie -> cookie.getMaxAge() == DEFAULT_EXPIRY_SECONDS;
        Mockito.verify(httpServletResponse).addCookie(ArgumentMatchers.argThat(argumentMatcher));
    }
}