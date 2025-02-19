// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class LoginRedirectHandlerTest {

    private static final String DEFAULT_REDIRECT_URI = "http://default-redirect.org";
    private static final HttpServletRequest httpServletRequest = mock();
    private static final HttpServletResponse httpServletResponse = mock();
    private static final LoginRedirectHandler handlerToTest = new LoginRedirectHandler(DEFAULT_REDIRECT_URI);
    private static final String THEME_PARAMETER = "theme";
    private static final String REDIRECT_URI_PARAMETER = "redirectUri";
    private static final String JETBRAINS_THEME = "jetbrains";

    @Test
    void redirect_with_no_redirect_parameter_should_redirect_to_default_uri() throws IOException {
        /* prepare */
        when(httpServletRequest.getParameter(THEME_PARAMETER)).thenReturn(JETBRAINS_THEME);
        when(httpServletRequest.getParameter(REDIRECT_URI_PARAMETER)).thenReturn(null);

        /* execute */
        handlerToTest.redirect(httpServletRequest, httpServletResponse);

        /* test */
        verify(httpServletResponse).sendRedirect("%s?%s=%s".formatted(DEFAULT_REDIRECT_URI, THEME_PARAMETER, JETBRAINS_THEME));
    }

    @Test
    void redirect_with_redirect_parameter_should_redirect_to_specified_uri() throws IOException {
        /* prepare */
        when(httpServletRequest.getParameter(THEME_PARAMETER)).thenReturn(JETBRAINS_THEME);
        String redirectUri = "http://desired-redirect.com";
        when(httpServletRequest.getParameter(REDIRECT_URI_PARAMETER)).thenReturn(redirectUri);

        /* execute */
        handlerToTest.redirect(httpServletRequest, httpServletResponse);

        /* test */
        verify(httpServletResponse).sendRedirect("%s?%s=%s".formatted(redirectUri, THEME_PARAMETER, JETBRAINS_THEME));
    }

}