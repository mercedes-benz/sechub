// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code MissingAuthenticationEntryPointHandler} implements
 * {@link AuthenticationEntryPoint} to provide custom behavior upon missing or
 * invalid authentication. This class is used by Spring's
 * <i>oauth2ResourceServer</i> configuration to redirect the user to the login
 * page if the user is not authenticated.
 *
 * @author hamidonos
 */
class MissingAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    private final String location;

    MissingAuthenticationEntryPointHandler(String location) {
        this.location = location;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.sendRedirect(location);
    }
}
