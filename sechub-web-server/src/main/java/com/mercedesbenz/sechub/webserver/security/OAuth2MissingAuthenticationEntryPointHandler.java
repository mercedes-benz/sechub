// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.mercedesbenz.sechub.webserver.RequestConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code OAuth2MissingAuthenticationEntryPointHandler} implements
 * {@link AuthenticationEntryPoint} to provide custom behavior upon missing or
 * invalid authentication. This class is used by Spring's
 * <i>oauth2ResourceServer</i> configuration to redirect the user to the OAuth2
 * login page if the user is not authenticated.
 *
 * @author hamidonos
 */
class OAuth2MissingAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.sendRedirect(RequestConstants.LOGIN_OAUTH2);
    }
}
