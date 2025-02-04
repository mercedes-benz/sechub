// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class LoginOAuth2SuccessHandlerTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "SECHUB_OAUTH2_ACCESS_TOKEN";
    private static final String ENCRYPTED_ACCESS_TOKEN = "this-is-an-encrypted-access-token";
    private static final byte[] ENCRYPTED_ACCESS_TOKEN_BYTES = ENCRYPTED_ACCESS_TOKEN.getBytes(StandardCharsets.UTF_8);
    private static final String ENCRYPTED_ACCESS_TOKEN_BASE64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_ACCESS_TOKEN_BYTES);
    private static final String PROVIDER = "example-provider";
    private static final String PRINCIPAL = "example-principal";
    private static final String ACCESS_TOKEN = "this-is-a-plain-access-token";
    private static final Duration DEFAULT_EXPIRY = Duration.ofHours(1);
    private static final String BASE_PATH = "/";
    private static final String REDIRECT_URI = "https://example.org/redirect-uri";

    private static final OAuth2AuthorizedClientService oAuth2AuthorizedClientService = mock();
    private static final AES256Encryption aes256Encryption = mock();
    private static final HttpServletRequest httpServletRequest = mock();
    private static final HttpServletResponse httpServletResponse = mock();
    private static final Authentication authentication = mock();
    private static final OAuth2AuthorizedClient oauth2AuthorizedClient = mock();
    private static final OAuth2AccessToken oAuth2AccessToken = mock();
    private static final LoginOAuth2SuccessHandler loginOAuth2SuccessHandler = new LoginOAuth2SuccessHandler(PROVIDER, oAuth2AuthorizedClientService,
            aes256Encryption, REDIRECT_URI);

    @BeforeEach
    void beforeEach() {
        reset(aes256Encryption, httpServletResponse);
        when(aes256Encryption.encrypt(ArgumentMatchers.anyString())).thenReturn(ENCRYPTED_ACCESS_TOKEN_BYTES);
        when(oAuth2AuthorizedClientService.loadAuthorizedClient(PROVIDER, PRINCIPAL)).thenReturn(oauth2AuthorizedClient);
        when(authentication.getName()).thenReturn(PRINCIPAL);
        when(oauth2AuthorizedClient.getAccessToken()).thenReturn(oAuth2AccessToken);
        when(oAuth2AccessToken.getTokenValue()).thenReturn(ACCESS_TOKEN);
    }

    @Test
    void on_authentication_success_sends_a_valid_redirect_containing_the_encrypted_access_token_cookie() throws IOException {
        /* prepare */
        Duration expiry = Duration.ofMinutes(1);
        Instant now = Instant.now();
        when(oAuth2AccessToken.getIssuedAt()).thenReturn(now);
        /* setting this should make sure that the default expiry (1 hour) is not used */
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(now.plusSeconds(expiry.toSeconds()));

        /* execute */
        loginOAuth2SuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

        /* test */
        verify(aes256Encryption).encrypt(ACCESS_TOKEN);
        verify(httpServletResponse).sendRedirect(REDIRECT_URI);
        ArgumentMatcher<Cookie> argumentMatcher = cookie -> {
            /* @formatter:off */
            if (!ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) return false;
            if (!ENCRYPTED_ACCESS_TOKEN_BASE64_ENCODED.equals(cookie.getValue())) return false;
            if (cookie.getMaxAge() != expiry.toSeconds()) return false;
            if (!cookie.isHttpOnly()) return false;
            if (!cookie.getSecure()) return false;
            return BASE_PATH.equals(cookie.getPath());
            /* @formatter:on */
        };
        verify(httpServletResponse).addCookie(ArgumentMatchers.argThat(argumentMatcher));
    }

    @Test
    void on_authentication_success_assumes_default_expiry_when_expires_at_is_null() throws IOException {
        /* prepare */
        Instant now = Instant.now();
        when(oAuth2AccessToken.getIssuedAt()).thenReturn(now);
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(null);

        /* execute */
        loginOAuth2SuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

        /* test */
        ArgumentMatcher<Cookie> argumentMatcher = cookie -> cookie.getMaxAge() == DEFAULT_EXPIRY.toSeconds();
        verify(httpServletResponse).addCookie(ArgumentMatchers.argThat(argumentMatcher));
    }
}