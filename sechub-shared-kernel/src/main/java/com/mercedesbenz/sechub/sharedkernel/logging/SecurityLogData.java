// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder(alphabetic = true)
public class SecurityLogData {

    private static final SecurityLogType FALLBACK_TYPE = SecurityLogType.UNKNOWN;

    SecurityLogType type = FALLBACK_TYPE;

    String clientIp;
    String userId;
    String message;
    Object[] messageParameters;
    List<String> traceInformation = new ArrayList<>();
    String method;
    String requestURI;
    Map<String, String> httpHeaders = new TreeMap<>(); // we want it sorted, so a tree map

    String sessionId;
    boolean afterSessionClosed;

    String basicAuthUser;

    public boolean isAfterSessionClosed() {
        return afterSessionClosed;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getMethod() {
        return method;
    }

    public String getUserId() {
        return userId;
    }

    public SecurityLogType getType() {
        if (type == null) {
            return FALLBACK_TYPE;
        }
        return type;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public String getMessage() {
        return message;
    }

    public List<Object> getMessageParameters() {
        if (messageParameters == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(messageParameters));
    }

    public List<String> getTraceInformation() {
        return traceInformation;
    }

    public String getBasicAuthUser() {
        return basicAuthUser;
    }

}