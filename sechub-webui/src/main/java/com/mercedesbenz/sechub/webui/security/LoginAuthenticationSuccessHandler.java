// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.mercedesbenz.sechub.webui.RequestConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code OAuth2SuccessHandler} implements {@link AuthenticationSuccessHandler}
 * to provide custom behavior upon successful authentication. This handler
 * redirects the user to the /home page specified in {@link RequestConstants}.
 *
 * @see SecurityConfiguration
 * @see RequestConstants
 *
 * @author hamidonos
 */
class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoginAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LOG.debug("Redirecting to %s".formatted(RequestConstants.HOME));
        response.sendRedirect(RequestConstants.HOME);
    }
}