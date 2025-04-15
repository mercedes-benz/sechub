// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter which checks if the request path is targeting the allowed port. If
 * not, it will return a <code>403 Forbidden</code> response.
 *
 * <p>
 * For example, if the allowed port is <code>8080</code>, and the request is
 * targeting <code>8081</code>, the filter will return a
 * <code>403 Forbidden</code> response .
 * </p>
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
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        /* @formatter:on */
        int requestPort = request.getServerPort();
        if (allowedPort != requestPort) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
