// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.logging.DefaultSecurityLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogType;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Special HTTP filter component - will log client errors to security log
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityLogFilter implements Filter {

    @Autowired
    private DefaultSecurityLogService securityLogService;

    private Map<Integer, SecurityLogInfo> logInfoMap;

    public SecurityLogFilter() {
        logInfoMap = new HashMap<>();

        // we do not add BAD_REQUEST here, because this is already done in
        // SecHubHttpStatusRequestRejectedHandler with detailed message
        addInfo(HttpStatus.UNAUTHORIZED);
        addInfo(HttpStatus.FORBIDDEN, SecurityLogType.POTENTIAL_INTRUSION);
        addInfo(HttpStatus.METHOD_NOT_ALLOWED, SecurityLogType.POTENTIAL_INTRUSION);
        addInfo(HttpStatus.NOT_ACCEPTABLE);
        addInfo(HttpStatus.TOO_MANY_REQUESTS, SecurityLogType.POTENTIAL_INTRUSION);
        addInfo(HttpStatus.TOO_EARLY);
        addInfo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, SecurityLogType.POTENTIAL_INTRUSION);
        addInfo(HttpStatus.PAYLOAD_TOO_LARGE);
        addInfo(HttpStatus.URI_TOO_LONG);
        addInfo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        addInfo(HttpStatus.EXPECTATION_FAILED);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String sessionId = null;
        HttpServletRequest httpServletRequest = null;
        HttpServletResponse httpServletResponse = null;

        if (request instanceof HttpServletRequest) {
            httpServletRequest = (HttpServletRequest) request;
        }
        if (response instanceof HttpServletResponse) {
            httpServletResponse = (HttpServletResponse) response;
        }
        boolean isHTTPInformationAvailable = httpServletResponse != null && httpServletRequest != null;

        if (isHTTPInformationAvailable) {
            /* before chain handled (session is available, not closed) */
            sessionId = httpServletRequest.getSession().getId();
        }

        chain.doFilter(request, response);

        /* after chain handled */
        if (isHTTPInformationAvailable) {
            handleSecurityLogging(sessionId, httpServletRequest, httpServletResponse);
        }
    }

    private void handleSecurityLogging(String sessionId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        int status = httpServletResponse.getStatus();

        SecurityLogInfo logInfo = logInfoMap.get(Integer.valueOf(status));
        if (logInfo != null) {
            securityLogService.logAfterSessionClosed(httpServletRequest, sessionId, logInfo.type, logInfo.status.toString());
        }
    }

    private class SecurityLogInfo {
        HttpStatus status;

        SecurityLogType type;
    }

    private void addInfo(HttpStatus status) {
        addInfo(status, SecurityLogType.WRONG_USAGE);
    }

    private void addInfo(HttpStatus status, SecurityLogType type) {
        SecurityLogInfo info = new SecurityLogInfo();
        info.status = status;
        info.type = type;

        logInfoMap.put(status.value(), info);
    }

}
