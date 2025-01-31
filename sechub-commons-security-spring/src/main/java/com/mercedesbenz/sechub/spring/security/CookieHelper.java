// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Optional;

/**
 * Class {@code CookieHelper} provides utility methods for creating, retrieving, and removing cookies from the
 * {@link HttpServletRequest} and {@link HttpServletResponse} objects.
 *
 * @author hamidonos
 */
final class CookieHelper {

    static Cookie createCookie(String cookieName, String cookieValue, Duration duration, String basePath) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge((int) duration.toSeconds()); /* Casting this should be safe in all cases */
        cookie.setHttpOnly(true); /* Prevents client-side code (JavaScript) from accessing the cookie */
        cookie.setSecure(true); /* Send the cookie only over HTTPS */
        cookie.setPath(basePath); /* Cookie is available throughout the application */
        return cookie;
    }

    static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return Optional.of(cookie);
            }
        }

        return Optional.empty();
    }

    /**
     * Removes a cookie from the {@link HttpServletResponse}. Removing cookies is done by creating a new cookie
     * with the same name and setting the max age to <code>0</code>.
     *
     * @param httpServletResponse The http response to remove the cookie from
     * @param cookieName The name of the cookie to remove
     */
    static void removeCookie(HttpServletResponse httpServletResponse, String cookieName) {
        Cookie cookieToBeRemoved = createCookie(cookieName, "", Duration.ZERO, AbstractSecurityConfiguration.BASE_PATH);
        httpServletResponse.addCookie(cookieToBeRemoved);
    }
}
