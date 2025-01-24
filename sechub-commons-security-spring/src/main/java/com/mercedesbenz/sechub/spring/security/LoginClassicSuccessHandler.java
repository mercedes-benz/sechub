// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code LoginClassicSuccessHandler} implements
 * {@link AuthenticationSuccessHandler} to provide custom behavior upon
 * successful authentication. This handler redirects the user to the specified
 * <code>redirectUri</code>.
 *
 * @see AbstractSecurityConfiguration
 *
 * @author hamidonos
 */
class LoginClassicSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoginClassicSuccessHandler.class);

    private final String redirectUri;

    LoginClassicSuccessHandler(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LOG.debug("Redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
    }
}