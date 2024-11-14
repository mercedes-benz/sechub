// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class PortAccessGuardTest {

    private static final HttpServletResponse httpServletResponse = mock();
    private static final FilterChain filterChain = mock();

    @BeforeEach
    void beforeEach() {
        reset(httpServletResponse, filterChain);
    }

    @Test
    void do_filter_internal_does_not_send_error_when_requested_port_is_allowed() throws ServletException, IOException {
        /* prepare */
        int port = 8080;
        PortAccessGuard guard = new PortAccessGuard(port);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest() {
            @Override
            public int getServerPort() {
                return port;
            }
        };

        /* execute */
        guard.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        /* test */
        verify(httpServletResponse, never()).sendError(anyInt());
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    @Test
    void do_filter_internal_does_send_error_403_forbidden_when_requested_port_is_not_allowed() throws ServletException, IOException {
        /* prepare */
        int allowedPort = 4443;
        PortAccessGuard guard = new PortAccessGuard(allowedPort);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest() {
            @Override
            public int getServerPort() {
                return allowedPort + 1;
            }
        };

        /* execute */
        guard.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        /* test */
        verify(httpServletResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(httpServletRequest, httpServletResponse);
    }
}