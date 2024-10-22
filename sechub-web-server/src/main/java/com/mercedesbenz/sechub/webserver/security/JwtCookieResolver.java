// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.util.Arrays;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * {@code JwtCookieResolver} implements {@link BearerTokenResolver} to provide
 * custom Bearer Token resolution. The Bearer Token (JWT) is read from the
 * cookies.
 *
 * @author hamidonos
 */
class JwtCookieResolver implements BearerTokenResolver {

    private static final String MISSING_JWT_VALUE = "missing-jwt";

    private final AES256Encryption aes256Encryption;

    JwtCookieResolver(AES256Encryption aes256Encryption) {
        this.aes256Encryption = aes256Encryption;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
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
                .filter(cookie -> SecurityConfiguration.ACCESS_TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        /* @formatter:on */

        if (jwt == null) {
            /* same here */
            return MISSING_JWT_VALUE;
        }

        return aes256Encryption.decrypt(jwt);
    }
}
