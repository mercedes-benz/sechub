// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter which checks if the request is targeting the allowed port. If not, it
 * will return a <code>403 Forbidden</code> response.
 *
 * @author hamidonos
 */
class PortAccessGuard extends OncePerRequestFilter {

    private final int allowedPort;

    public PortAccessGuard(int allowedPort) {
        this.allowedPort = allowedPort;
    }

    @Override
    /* @formatter:off */
    protected void doFilterInternal(HttpServletRequest request,
                                    @SuppressWarnings("NullableProblems") HttpServletResponse response,
                                    @SuppressWarnings("NullableProblems") FilterChain filterChain) throws ServletException, IOException {
        /* @formatter:on */
        int requestPort = request.getServerPort();
        if (allowedPort != requestPort) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
