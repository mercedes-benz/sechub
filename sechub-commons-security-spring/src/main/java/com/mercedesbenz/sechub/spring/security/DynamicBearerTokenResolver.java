// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Arrays;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This class implements the {@link BearerTokenResolver} interface to provide
 * custom Bearer Token resolution. The access token is read from the
 * <code>Authorization</code> header first. If the access token is not found in
 * the header, it is then read from the cookies and decrypted using
 * {@link AES256Encryption}. Note that the access token has to be encrypted and
 * encoded in {@link Base64} format when passed as a cookie.
 *
 * @see BearerTokenResolver
 * @see org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
 * @see AES256Encryption
 *
 * @author hamidonos
 */
class DynamicBearerTokenResolver implements BearerTokenResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicBearerTokenResolver.class);
    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final String BEARER_PREFIX = "Bearer ";

    private final AES256Encryption aes256Encryption;

    DynamicBearerTokenResolver(AES256Encryption aes256Encryption) {
        this.aes256Encryption = aes256Encryption;
    }

    @Override
    public String resolve(HttpServletRequest request) {

        /*
         * Try to resolve the token from the 'Authorization' header first
         */

        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        /*
         * Attempt to resolve the token from the 'access_token' cookie
         */

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            LOG.trace("No cookies found in the request");
            return null;
        }

        /* @formatter:off */
        String accessToken = Arrays
                .stream(cookies)
                .filter(cookie -> AbstractSecurityConfiguration.OAUTH2_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        /* @formatter:on */

        if (accessToken == null) {
            LOG.trace("Request is missing the 'access_token' cookie");
            return null;
        }

        try {
            byte[] tokenBytes = DECODER.decode(accessToken);
            return aes256Encryption.decrypt(tokenBytes);
        } catch (Exception e) {
            LOG.debug("Failed to decrypt the access token cookie", e);
            return null;
        }
    }
}
