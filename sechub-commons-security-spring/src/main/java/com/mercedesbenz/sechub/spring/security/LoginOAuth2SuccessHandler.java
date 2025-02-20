// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration.BASE_PATH;
import static com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration.OAUTH2_COOKIE_NAME;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

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
 * @see OAuth2AuthorizedClientService
 *
 * @author hamidonos
 */
class LoginOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Duration DEFAULT_EXPIRY_ONE_HOUR = Duration.ofHours(1);

    private final String provider;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final AES256Encryption aes256Encryption;
    private final LoginRedirectHandler loginRedirectHandler;

    /* @formatter:off */
    public LoginOAuth2SuccessHandler(String provider,
                                     OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                     AES256Encryption aes256Encryption,
                                     LoginRedirectHandler loginRedirectHandler) {
        this.provider = requireNonNull(provider, "Property provider must not be null");
        this.oAuth2AuthorizedClientService = requireNonNull(oAuth2AuthorizedClientService, "Property oAuth2AuthorizedClientService must not be null");
        this.aes256Encryption = requireNonNull(aes256Encryption, "Property aes256Encryption must not be null");
        this.loginRedirectHandler = requireNonNull(loginRedirectHandler, "Property loginRedirectHandler must not be null");
    }
    /* @formatter:on */

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AccessToken oAuth2AccessToken = getAccessTokenFromAuthentication(authentication);
        Instant issuedAt = requireNonNullElseGet(oAuth2AccessToken.getIssuedAt(), Instant::now);
        /* Assume a default expiry of 1 hour if the expiry time is not set */
        Instant expiresAt = requireNonNullElseGet(oAuth2AccessToken.getExpiresAt(), () -> Instant.now().plusSeconds(DEFAULT_EXPIRY_ONE_HOUR.toSeconds()));
        long expirySeconds = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        Duration expiryDuration = Duration.ofSeconds(expirySeconds);
        String accessToken = oAuth2AccessToken.getTokenValue();
        byte[] encryptedAccessTokenBytes = aes256Encryption.encrypt(accessToken);
        String encryptedAccessTokenB64Encoded = ENCODER.encodeToString(encryptedAccessTokenBytes);
        Cookie cookie = CookieHelper.createCookie(OAUTH2_COOKIE_NAME, encryptedAccessTokenB64Encoded, expiryDuration, BASE_PATH);
        response.addCookie(cookie);
        loginRedirectHandler.redirect(request, response);
    }

    private OAuth2AccessToken getAccessTokenFromAuthentication(Authentication authentication) {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(provider, authentication.getName());
        return oAuth2AuthorizedClient.getAccessToken();
    }
}