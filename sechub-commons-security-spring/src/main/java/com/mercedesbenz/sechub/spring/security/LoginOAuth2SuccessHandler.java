// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * {@code LoginOAuth2SuccessHandler} implements
 * {@link AuthenticationSuccessHandler} to provide custom behavior upon
 * successful authentication. This handler redirects the user to the specified
 * <code>redirectUri</code>.
 * </p>
 *
 * <p>
 * This handler will also populate a secure HTTP-only cookie containing the
 * access token which can be used in subsequent requests to authenticate the
 * user. Note that the access token is encrypted using {@link AES256Encryption}
 * and encoded using {@link Base64}.
 * </p>
 *
 * @see LoginSecurityConfiguration
 * @see LoginOAuth2Properties
 * @see OAuth2AuthorizedClientService
 *
 * @author hamidonos
 */
class LoginOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoginOAuth2SuccessHandler.class);
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final int DEFAULT_EXPIRY_SECONDS = 3600;
    private static final String BASE_PATH = "/";

    private final LoginOAuth2Properties loginOAuth2Properties;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final AES256Encryption aes256Encryption;
    private final String redirectUri;

    /* @formatter:off */
    public LoginOAuth2SuccessHandler(LoginOAuth2Properties loginOAuth2Properties,
                                     OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                     AES256Encryption aes256Encryption,
                                     String redirectUri) {
        this.loginOAuth2Properties = requireNonNull(loginOAuth2Properties, "Property loginOAuth2Properties must not be null");
        this.oAuth2AuthorizedClientService = requireNonNull(oAuth2AuthorizedClientService, "Property oAuth2AuthorizedClientService must not be null");
        this.aes256Encryption = requireNonNull(aes256Encryption, "Property aes256Encryption must not be null");
        this.redirectUri = requireNonNull(redirectUri, "Property redirectUri must not be null");
    }
    /* @formatter:on */

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AccessToken oAuth2AccessToken = getAccessTokenFromAuthentication(authentication);
        Instant issuedAt = requireNonNullElseGet(oAuth2AccessToken.getIssuedAt(), Instant::now);
        /* Assume a default expiry of 1 hour if the expiry time is not set */
        Instant expiresAt = requireNonNullElseGet(oAuth2AccessToken.getExpiresAt(), () -> Instant.now().plusSeconds(DEFAULT_EXPIRY_SECONDS));
        long expirySeconds = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        String accessToken = oAuth2AccessToken.getTokenValue();
        byte[] encryptedAccessTokenBytes = aes256Encryption.encrypt(accessToken);
        String encryptedAccessTokenB64Encoded = ENCODER.encodeToString(encryptedAccessTokenBytes);
        response.addCookie(createAccessTokenCookie(encryptedAccessTokenB64Encoded, expirySeconds));
        LOG.debug("Redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
    }

    private OAuth2AccessToken getAccessTokenFromAuthentication(Authentication authentication) {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(loginOAuth2Properties.getProvider(),
                authentication.getName());
        return oAuth2AuthorizedClient.getAccessToken();
    }

    private Cookie createAccessTokenCookie(String accessToken, long expirySeconds) {
        Cookie cookie = new Cookie(AbstractSecurityConfiguration.ACCESS_TOKEN, accessToken);
        cookie.setMaxAge((int) expirySeconds); /* Casting this should be safe in all cases */
        cookie.setHttpOnly(true); /* Prevents client-side code (JavaScript) from accessing the cookie */
        cookie.setSecure(true); /* Send the cookie only over HTTPS */
        cookie.setPath(BASE_PATH); /* Cookie is available throughout the application */
        return cookie;
    }
}