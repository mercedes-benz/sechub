// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.daimler.sechub.adapter.SpringUtilFactory;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default security log service. 
 * @author Albert Tregnaghi
 *
 */
@Service
@Profile("!" + Profiles.INTEGRATIONTEST)
public class DefaultSecurityLogService implements SecurityLogService {

    @Autowired
    UserContextService userContextService;

    @Autowired
    LogSanitizer logSanititzer;

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
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String sessionId = null;
        if (requestAttributes instanceof ServletRequestAttributes) {
            httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            HttpSession session = httpServletRequest.getSession();
            if (session != null) {
                sessionId = session.getId();
            }
        }
        log(httpServletRequest, sessionId, type, message, objects);
    }

    /**
     * Log security event - this method can be also used inside filters where session is already closed (response committed).
     * @param request
     * @param httpSessionId
     * @param type
     * @param message
     * @param objects
     */
    public final void log(HttpServletRequest request, String httpSessionId, SecurityLogType type, String message, Object... objects) {

        SecurityLogData securityLogData = buildLogData(request, httpSessionId, type, message, objects);

        doLogging(securityLogData);
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
         * convert this to a new list, otherwise slf4j becomes problems with identifying
         * this as list and having wrong output
         */
        List<Object> paramList = new ArrayList<>();
        paramList.add(logData.getType().getTypeId());
        try {
            paramList.add(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logData));
        } catch (JsonProcessingException e) {
            LOG.error("Was not able to write security log data as json - will fallback to empty JSON", e);

            paramList.add("{}");
        }
        // we add this to the end, so a wrong defined custom log message could never
        // overwrite security log data
        paramList.addAll(logData.getMessageParameters());

        LOG.warn(sb.toString(), paramList.toArray());

    }

    private SecurityLogData buildLogData(HttpServletRequest request, String sessionId, SecurityLogType type, String message, Object... objects) {
        SecurityLogData logData = new SecurityLogData();
        if (type == null) {
            LOG.warn("Security log service was called with no log type, so call was wrong implemented! Use fallback:{}", logData.type);
        } else {
            logData.type = type;
        }
        logData.message = message;
        logData.messageParameters = objects;
        logData.sessionId = sessionId;

        collectRequestInfo(request, logData);

        logData.userId = userContextService.getUserId();

        return logData;
    }

    private void collectRequestInfo(HttpServletRequest request, SecurityLogData logContext) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession();
        if (session != null) {
            logContext.sessionId = session.getId();
        }
        logContext.clientIp = request.getRemoteAddr();
        logContext.requestURI = request.getRequestURI();

        appendHttpHeaders(request, logContext);

    }

    private void appendHttpHeaders(HttpServletRequest request, SecurityLogData logContext) {
        int amountOfHeadersFound = 0;
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName == null) {
                continue;
            }
            String headerValue = null;
            if (headerName.equalsIgnoreCase("authorization")) {
                headerValue="***REPLACED***";
            }else {
                headerValue = request.getHeader(headerName);
                if (headerValue == null) {
                    continue;
                }
            }
            amountOfHeadersFound++;
            if (amountOfHeadersFound > MAXIMUM_HEADER_AMOUNT_TO_SHOW) {
                continue;
            }
            logContext.getHttpHeaders().put(logSanititzer.sanitize(headerName, 40), logSanititzer.sanitize(headerValue, 1024, false)); // headerValue without
                                                                                                                                       // log forgery handling,
                                                                                                                                       // we want the origin
                                                                                                                                       // output inside JSON
        }
        if (amountOfHeadersFound > MAXIMUM_HEADER_AMOUNT_TO_SHOW) {
            LOG.warn("Maximum header values ({}) reached in request from ip={} - header amoutn was {} at all! So truncate header values inside next log entry!",
                    MAXIMUM_HEADER_AMOUNT_TO_SHOW, logContext.getClientIp(), amountOfHeadersFound);
        }
    }
}
