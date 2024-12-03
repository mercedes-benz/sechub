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
 * @see AES256Encryption
 *
 * @author hamidonos
 */
class DynamicBearerTokenResolver implements BearerTokenResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicBearerTokenResolver.class);
    private static final String MISSING_ACCESS_TOKEN_VALUE = "missing-access-token";
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
            LOG.debug("No cookies found in the request");

            /*
             * If the access token cookie is not found, we return a constant string to
             * indicate that the access token is missing. We do this because we want to pass
             * exception handling further down the chain. Spring does not provide a way to
             * wrap exceptions around custom BearerTokenResolver classes effectively. This
             * is a good practice because it allows us to handle the missing access token in
             * a more controlled manner.
             */
            return MISSING_ACCESS_TOKEN_VALUE;
        }

        /* @formatter:off */
        String accessToken = Arrays
                .stream(cookies)
                .filter(cookie -> AbstractSecurityConfiguration.ACCESS_TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        /* @formatter:on */

        if (accessToken == null) {
            LOG.debug("Request is missing the 'access_token' cookie");
            /* same here */
            return MISSING_ACCESS_TOKEN_VALUE;
        }

        try {
            byte[] jwtBytes = DECODER.decode(accessToken);
            return aes256Encryption.decrypt(jwtBytes);
        } catch (Exception e) {
            LOG.debug("Failed to decrypt the access token cookie", e);
            /* same here */
            return MISSING_ACCESS_TOKEN_VALUE;
        }
    }
}
