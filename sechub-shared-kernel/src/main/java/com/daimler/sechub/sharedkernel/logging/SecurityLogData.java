// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class SecurityLogData {
    
    private static final SecurityLogType FALLBACK_TYPE = SecurityLogType.UNKNOWN;
    
    SecurityLogType type = FALLBACK_TYPE;

    String clientIp;
    String userId;
    String message;
    Object[] messageParameters;
    List<String> traceInformation = new ArrayList<>();
    String requestURI;
    Map<String,String> httpHeaders = new TreeMap<>(); // we want it sorted, so a tree map

    String sessionId;

    public String getSessionId() {
        return sessionId;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public String getRequestURI() {
        return requestURI;
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

}