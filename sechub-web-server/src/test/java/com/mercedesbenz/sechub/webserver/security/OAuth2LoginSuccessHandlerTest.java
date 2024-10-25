// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class OAuth2LoginSuccessHandlerTest {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String ENCRYPTED_JWT = "this-is-an-encrypted-jwt";
    private static final byte[] ENCRYPTED_JWT_BYTES = ENCRYPTED_JWT.getBytes(StandardCharsets.UTF_8);
    private static final String ENCRYPTED_JWT_BASE64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_JWT_BYTES);
    private static final String PROVIDER = "example-provider";
    private static final String PRINCIPAL = "example-principal";
    private static final String JWT = "this-is-a-plain-jwt";
    private static final int DEFAULT_EXPIRY_SECONDS = 3600;
    private static final String BASE_PATH = "/";

    private static final OAuth2Properties oAuth2Properties = mock();
    private static final OAuth2AuthorizedClientService oAuth2AuthorizedClientService = mock();
    private static final AES256Encryption aes256Encryption = mock();
    private static final HttpServletRequest httpServletRequest = mock();
    private static final HttpServletResponse httpServletResponse = mock();
    private static final Authentication authentication = mock();
    private static final OAuth2AuthorizedClient oauth2AuthorizedClient = mock();
    private static final OAuth2AccessToken oAuth2AccessToken = mock();
    private static final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler = new OAuth2LoginSuccessHandler(oAuth2Properties, oAuth2AuthorizedClientService,
            aes256Encryption);

    @BeforeEach
    void beforeEach() {
        reset(aes256Encryption, httpServletResponse);
        when(oAuth2Properties.getProvider()).thenReturn(PROVIDER);
        when(aes256Encryption.encrypt(anyString())).thenReturn(ENCRYPTED_JWT_BYTES);
        when(oAuth2AuthorizedClientService.loadAuthorizedClient(PROVIDER, PRINCIPAL)).thenReturn(oauth2AuthorizedClient);
        when(authentication.getName()).thenReturn(PRINCIPAL);
        when(oauth2AuthorizedClient.getAccessToken()).thenReturn(oAuth2AccessToken);
        when(oAuth2AccessToken.getTokenValue()).thenReturn(JWT);
    }

    @Test
    void on_authentication_success_sends_a_valid_redirect_containing_the_encrypted_jwt_cookie() throws IOException {
        /* prepare */
        Instant now = Instant.now();
        when(oAuth2AccessToken.getIssuedAt()).thenReturn(now);
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(now.plusSeconds(60));
        int expirySeconds = 60;

        /* execute */
        oAuth2LoginSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

        /* test */
        verify(aes256Encryption).encrypt(JWT);
        verify(httpServletResponse).sendRedirect(RequestConstants.HOME);
        ArgumentMatcher<Cookie> jwt = cookie -> {
            /* @formatter:off */
            if (!ACCESS_TOKEN.equals(cookie.getName())) return false;
            if (!ENCRYPTED_JWT_BASE64_ENCODED.equals(cookie.getValue())) return false;
            if (cookie.getMaxAge() != expirySeconds) return false;
            if (!cookie.isHttpOnly()) return false;
            if (!cookie.getSecure()) return false;
            return BASE_PATH.equals(cookie.getPath());
            /* @formatter:on */
        };
        verify(httpServletResponse).addCookie(argThat(jwt));
    }

    @Test
    void on_authentication_success_assumes_default_expiry_when_expires_at_is_null() throws IOException {
        /* prepare */
        Instant now = Instant.now();
        when(oAuth2AccessToken.getIssuedAt()).thenReturn(now);
        when(oAuth2AccessToken.getExpiresAt()).thenReturn(null);

        /* execute */
        oAuth2LoginSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

        /* test */
        ArgumentMatcher<Cookie> jwt = cookie -> cookie.getMaxAge() == DEFAULT_EXPIRY_SECONDS;
        verify(httpServletResponse).addCookie(argThat(jwt));
    }
}