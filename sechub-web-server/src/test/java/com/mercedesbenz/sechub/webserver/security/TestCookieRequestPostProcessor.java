// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;

/**
 * {@code TestCookieRequestPostProcessor} is a custom
 * {@link RequestPostProcessor} implementation that adds a custom cookie to the
 * request headers. {@link MockHttpServletRequest} . This is useful for testing
 * endpoints that require cookie authentication.
 *
 * @author hamidonos
 */
public class TestCookieRequestPostProcessor implements RequestPostProcessor {

    private final Cookie cookie;

    public TestCookieRequestPostProcessor(Cookie cookie) {
        this.cookie = cookie;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        request.setCookies(cookie);
        return request;
    }
}