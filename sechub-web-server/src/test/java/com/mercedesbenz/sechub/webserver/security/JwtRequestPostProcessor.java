// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;

/**
 * {@code JwtRequestPostProcessor} is a custom {@link RequestPostProcessor}
 * implementation that adds a JWT token as a cookie to the
 * {@link MockHttpServletRequest} . This is useful for testing endpoints that
 * require JWT authentication.
 *
 * @author hamidonos
 */
public class JwtRequestPostProcessor implements RequestPostProcessor {

    private static final String ACCESS_TOKEN = "access_token";
    private final String jwt;

    public JwtRequestPostProcessor(String jwt) {
        this.jwt = jwt;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        request.setCookies(new Cookie(ACCESS_TOKEN, jwt));
        return request;
    }

    public static RequestPostProcessor fromJwt(String jwtToken) {
        return new JwtRequestPostProcessor(jwtToken);
    }
}