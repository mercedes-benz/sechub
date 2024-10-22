// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * {@code OAuth2LoginSuccessHandler} implements
 * {@link AuthenticationSuccessHandler} to provide custom behavior upon
 * successful authentication. This handler redirects the user to the /home page
 * specified in {@link RequestConstants}.
 * </p>
 *
 * <p>
 * This handler will also populate a secure HTTP-only cookie containing the JWT
 * token which can be used in subsequent requests to authenticate the user.
 * </p>
 *
 * @see SecurityConfiguration
 * @see RequestConstants
 * @see OAuth2Properties
 * @see OAuth2AuthorizedClientService
 *
 * @author hamidonos
 */
class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    private static final int DEFAULT_EXPIRY_SECONDS = 3600;
    private static final String BASE_PATH = "/";

    private final OAuth2Properties oAuth2Properties;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final AES256Encryption aes256Encryption;

    public OAuth2LoginSuccessHandler(OAuth2Properties oAuth2Properties, OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            AES256Encryption aes256Encryption) {
        this.oAuth2Properties = requireNonNull(oAuth2Properties, "Property oAuthProperties must not be null");
        this.oAuth2AuthorizedClientService = requireNonNull(oAuth2AuthorizedClientService, "Property oAuth2AuthorizedClientService must not be null");
        this.aes256Encryption = requireNonNull(aes256Encryption, "Property aes256Encryption must not be null");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AccessToken oAuth2AccessToken = getJwtFromAuthentication(authentication);
        Instant issuedAt = requireNonNullElseGet(oAuth2AccessToken.getIssuedAt(), Instant::now);
        /* Assume a default expiry of 1 hour if the expiry time is not set */
        Instant expiresAt = requireNonNullElseGet(oAuth2AccessToken.getExpiresAt(), () -> Instant.now().plusSeconds(DEFAULT_EXPIRY_SECONDS));
        long expirySeconds = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        String jwt = oAuth2AccessToken.getTokenValue();
        String encryptedJwt = aes256Encryption.encrypt(jwt);
        response.addCookie(createJwtCookie(encryptedJwt, expirySeconds));
        LOG.debug("Redirecting to {}", RequestConstants.HOME);
        response.sendRedirect(RequestConstants.HOME);
    }

    private OAuth2AccessToken getJwtFromAuthentication(Authentication authentication) {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(oAuth2Properties.getProvider(),
                authentication.getName());
        return oAuth2AuthorizedClient.getAccessToken();
    }

    private Cookie createJwtCookie(String jwt, long expirySeconds) {
        Cookie cookie = new Cookie(SecurityConfiguration.ACCESS_TOKEN, jwt);
        cookie.setMaxAge((int) expirySeconds); /* Casting this should be safe in all cases */
        cookie.setHttpOnly(true); /* Prevents client-side code (JavaScript) from accessing the cookie */
        cookie.setSecure(true); /* Send the cookie only over HTTPS */
        cookie.setPath(BASE_PATH); /* Cookie is available throughout the application */
        return cookie;
    }
}