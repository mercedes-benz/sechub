// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Conditional;

import java.io.IOException;

/**
 * The {@code LoginRedirectHandler} handles the redirection of the user the desired redirect uri after successful
 * authentication. The redirect uri is specified as a query parameter in the request. If the redirect uri is not
 * specified, the default redirect uri from the configuration is used.
 *
 * @author hamidonos
 */
@Conditional(LoginEnabledCondition.class)
class LoginRedirectHandler {

    private static final String THEME_PARAMETER = "theme";
    private static final String REDIRECT_URI_PARAMETER = "redirectUri";

    private final String defaultRedirectUri;

    LoginRedirectHandler(String defaultRedirectUri) {
        this.defaultRedirectUri = defaultRedirectUri;
    }

    void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String theme = request.getParameter(THEME_PARAMETER);
        String redirectUri = request.getParameter(REDIRECT_URI_PARAMETER);
        if (redirectUri == null) {
            redirectUri = defaultRedirectUri;
        }
        response.sendRedirect("%s?%s=%s".formatted(redirectUri, THEME_PARAMETER, theme));
    }

}
