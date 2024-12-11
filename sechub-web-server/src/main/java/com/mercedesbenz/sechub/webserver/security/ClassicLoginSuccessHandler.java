// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.mercedesbenz.sechub.webserver.RequestConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code ClassicLoginSuccessHandler} implements
 * {@link AuthenticationSuccessHandler} to provide custom behavior upon
 * successful authentication. This handler redirects the user to the /home page
 * specified in {@link RequestConstants}.
 *
 * @see WebServerSecurityConfiguration
 * @see RequestConstants
 *
 * @author hamidonos
 */
class ClassicLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClassicLoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LOG.debug("Redirecting to {}", RequestConstants.HOME);
        response.sendRedirect(RequestConstants.HOME);
    }
}