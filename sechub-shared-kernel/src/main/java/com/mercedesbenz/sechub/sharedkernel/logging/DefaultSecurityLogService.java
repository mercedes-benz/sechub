// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.adapter.SpringUtilFactory;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;

/**
 * Default security log service.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
@Profile("!" + Profiles.INTEGRATIONTEST) // For integration testing we extend this service! See hierarchy
public class DefaultSecurityLogService implements SecurityLogService {

    private static final int MINIMUM_LENGTH_TO_SHOW_PWD_INT = 52;

    @Autowired
    UserContextService userContextService;

    @Autowired
    LogSanitizer logSanititzer;

    @Autowired
    RequestAttributesProvider requestAttributesProvider;

    @Autowired
    AuthorizeValueObfuscator authorizedValueObfuscator;

    @Autowired
    BasicAuthUserExtraction basicAuthUserExtraction;

    private int MAXIMUM_HEADER_AMOUNT_TO_SHOW = 300;

    private ObjectMapper objectMapper;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSecurityLogService.class);

    private static String SECURITY = "[SECURITY] [{}]:";

    public DefaultSecurityLogService() {
        objectMapper = SpringUtilFactory.createDefaultObjectMapper();
    }

    @Override
    public final void log(SecurityLogType type, String message, Object... objects) {
        HttpServletRequest httpServletRequest = null;
        String sessionId = null;

        RequestAttributes requestAttributes = requestAttributesProvider.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            HttpSession session = httpServletRequest.getSession();
            if (session != null) {
                sessionId = session.getId();
            }
        }

        doLogging(buildLogData(httpServletRequest, sessionId, false, type, message, objects));
    }

    /**
     * Log security event - this method can be used inside filters where session is
     * already closed (response committed). The normal
     * {@link #log(SecurityLogType, String, Object...)} method would not work in
     * this case, because we can only log in filters after filter chain has applied
     * - but when a failure happens, the sesson will be closed.<br>
     * <br>
     * Because this a very special use case (only for filtering) it is not inside
     * the {@link SecurityLogService} interface.
     *
     * @param request
     * @param httpSessionId
     * @param type
     * @param message
     * @param objects
     */
    public final void logAfterSessionClosed(HttpServletRequest request, String httpSessionId, SecurityLogType type, String message, Object... objects) {
        doLogging(buildLogData(request, httpSessionId, true, type, message, objects));
    }

    void doLogging(SecurityLogData logData) {
        StringBuilder sb = new StringBuilder();
        sb.append(SECURITY);
        sb.append("\ndata=\n{}");
        // we add this to the end, so a wrong defined custom log message could never
        // overwrite security log data
        sb.append("\n, message=");
        sb.append(logData.message);

        /*
         * Convert this to a new list - otherwise slf4j has problems with identifying
         * this as list and produces wrong output
         */
        List<Object> paramList = new ArrayList<>();
        paramList.add(logData.getType().getTypeId());

        try {
            String logDataAsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logData);
            paramList.add(logDataAsJson);
        } catch (JsonProcessingException e) {
            getLogger().error("Was not able to write security log data as json - will fallback to empty JSON", e);

            paramList.add("{}");
        }
        // we add this to the end, so a wrong defined custom log message could never
        // overwrite security log data
        paramList.addAll(logData.getMessageParameters());

        getLogger().warn(sb.toString(), paramList.toArray());

    }

    // package private so mockable in unit tests
    Logger getLogger() {
        return LOG;
    }

    private SecurityLogData buildLogData(HttpServletRequest request, String sessionId, boolean afterSessionClosed, SecurityLogType type, String message,
            Object... objects) {
        SecurityLogData logData = new SecurityLogData();
        if (type == null) {
            getLogger().warn("Security log service was called with no log type! Using fallback:{}", logData.type);
        } else {
            logData.type = type;
        }
        logData.message = message;
        logData.messageParameters = objects;
        logData.sessionId = sessionId;
        logData.afterSessionClosed = afterSessionClosed;

        collectRequestInfo(request, logData);

        logData.userId = userContextService.getUserId();

        String userFromAuthHeader = basicAuthUserExtraction.extractUserFromAuthHeader(request.getHeader("authorization"));
        logData.basicAuthUser = logSanititzer.sanitize(userFromAuthHeader, 100, false); // without log forgery handling, we want the origin output

        return logData;
    }

    private void collectRequestInfo(HttpServletRequest request, SecurityLogData logContext) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession();
        if (session != null) {
            logContext.sessionId = logSanititzer.sanitize(session.getId(), 1024);
        }
        logContext.clientIp = logSanititzer.sanitize(request.getRemoteAddr(), 1024);
        logContext.requestURI = logSanititzer.sanitize(request.getRequestURI(), 1024);
        logContext.method = logSanititzer.sanitize(request.getMethod(), 10);

        appendSanitizedHttpHeaders(request, logContext);

    }

    private void appendSanitizedHttpHeaders(HttpServletRequest request, SecurityLogData logContext) {
        int amountOfHeadersFound = 0;
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName == null) {
                continue;
            }
            String headerValue = request.getHeader(headerName);
            if (headerValue == null) {
                continue;
            }
            amountOfHeadersFound++;
            if (amountOfHeadersFound > MAXIMUM_HEADER_AMOUNT_TO_SHOW) {
                continue;
            }
            if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                headerValue = authorizedValueObfuscator.obfuscate(headerValue, MINIMUM_LENGTH_TO_SHOW_PWD_INT);
            }
            Map<String, String> headers = logContext.getHttpHeaders();
            String sanitzedHeaderName = logSanititzer.sanitize(headerName, 40);
            String sanitizedHeaderValue = logSanititzer.sanitize(headerValue, 1024, false); // headerValue without log forgery handling, we want the origin
                                                                                            // output
            // inside JSON
            headers.put(sanitzedHeaderName, sanitizedHeaderValue);

        }
        if (amountOfHeadersFound > MAXIMUM_HEADER_AMOUNT_TO_SHOW) {
            getLogger().warn(
                    "Maximum header values ({}) reached in request from ip={} - header amount was {} at all! So truncate header values inside next log entry!",
                    MAXIMUM_HEADER_AMOUNT_TO_SHOW, logContext.getClientIp(), amountOfHeadersFound);
        }
    }
}
