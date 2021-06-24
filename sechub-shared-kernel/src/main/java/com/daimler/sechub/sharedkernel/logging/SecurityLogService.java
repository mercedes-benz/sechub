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

@Service
@Profile("!" + Profiles.INTEGRATIONTEST)
public class SecurityLogService {

    @Autowired
    UserContextService userContextService;

    @Autowired
    LogSanitizer logSanititzer;

    private int MAXIMUM_HEADER_AMOUNT_TO_SHOW = 300;

    private ObjectMapper objectMapper;

    private static final Logger LOG = LoggerFactory.getLogger(SecurityLogService.class);

    private static String SECURITY = "[SECURITY] [{}]:";

    public SecurityLogService() {
        objectMapper = SpringUtilFactory.createDefaultObjectMapper();
    }

    /**
     * Log a security event
     * 
     * @param type
     * @param message
     * @param objects
     */
    public final void log(SecurityLogType type, String message, Object... objects) {

        SecurityLogData securityLogData = buildLogData(type, message, objects);

        doLogging(securityLogData);
    }

    void doLogging(SecurityLogData logData) {
        StringBuilder sb = new StringBuilder();
        sb.append(SECURITY);
        sb.append("\ndata=\n{}");
        // we add this to the end, so a wrong defined custom log message could never overwrite security log data
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
        // we add this to the end, so a wrong defined custom log message could never overwrite security log data
        paramList.addAll(logData.getMessageParameters());

        LOG.warn(sb.toString(), paramList.toArray());

    }

    private SecurityLogData buildLogData(SecurityLogType type, String message, Object... objects) {
        SecurityLogData logData = new SecurityLogData();
        if (type == null) {
            LOG.warn("Security log service was called with no log type, so call was wrong implemented! Use fallback:{}", logData.type);
        } else {
            logData.type = type;
        }
        logData.message = message;
        logData.messageParameters = objects;

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            collectRequestInfo(httpServletRequest, logData);
        }

        logData.userId = userContextService.getUserId();
        
        return logData;
    }

    private void collectRequestInfo(HttpServletRequest request, SecurityLogData logContext) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession();
        if (session!=null) {
            logContext.sessionId= session.getId();
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
            String headerValue = request.getHeader(headerName);
            if (headerValue == null) {
                continue;
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
