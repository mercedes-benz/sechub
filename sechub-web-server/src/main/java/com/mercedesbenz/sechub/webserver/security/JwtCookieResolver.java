// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.util.Arrays;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * {@code JwtCookieResolver} implements {@link BearerTokenResolver} to provide
 * custom Bearer Token resolution. The encrypted JWT is read from the cookies
 * and decrypted using {@link AES256Encryption}. Note that the JWT is expected
 * in {@link Base64} encoded format.
 *
 * @see BearerTokenResolver
 * @see AES256Encryption
 *
 * @author hamidonos
 */
class JwtCookieResolver implements BearerTokenResolver {

    private static final Logger LOG = LoggerFactory.getLogger(JwtCookieResolver.class);
    private static final String MISSING_JWT_VALUE = "missing-jwt";
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private final AES256Encryption aes256Encryption;

    JwtCookieResolver(AES256Encryption aes256Encryption) {
        this.aes256Encryption = aes256Encryption;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            LOG.debug("No cookies found in the request");

            /*
             * If the JWT cookie is not found, we return a constant string to indicate that
             * the JWT is missing. We do this because we want to pass exception handling
             * further down the chain. Spring does not provide a way to wrap exceptions
             * around custom BearerTokenResolver classes effectively. This is a good
             * practice because it allows us to handle the missing JWT in a more controlled
             * manner.
             */
            return MISSING_JWT_VALUE;
        }

        /* @formatter:off */
        String jwt = Arrays
                .stream(cookies)
                .filter(cookie -> WebServerSecurityConfiguration.ACCESS_TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        /* @formatter:on */

        if (jwt == null) {
            LOG.debug("Request is missing the 'access_token' cookie");
            /* same here */
            return MISSING_JWT_VALUE;
        }

        try {
            byte[] jwtBytes = DECODER.decode(jwt);
            return aes256Encryption.decrypt(jwtBytes);
        } catch (Exception e) {
            LOG.debug("Failed to decrypt JWT cookie", e);
            /* same here */
            return MISSING_JWT_VALUE;
        }
    }
}
