// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecHubServerMDCAsyncHandlerInterceptor implements AsyncHandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubServerMDCAsyncHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOG.trace("Initial clearing MDC for asynchronous request");
        // we clear MDC for every call
        MDC.clear();
        try {
            handleRoutingInformation(request);
        } catch (Exception e) {
            LOG.error("Handle routing information failed - so continue without new MDC settings", e);
        }
        return true;
    }

    void handleRoutingInformation(HttpServletRequest request) {
        String reqURI = request.getRequestURI();
        if (reqURI == null) {
            return;
        }
        if (handledProjectAPICall(reqURI)) {
            return;
        }

    }

    private boolean handledProjectAPICall(String reqURI) {
        int indexOf = reqURI.indexOf(APIConstants.API_PROJECT);
        if (indexOf == -1) {
            return false;
        }
        String part = reqURI.substring(indexOf + APIConstants.API_PROJECT.length());
        String projectId = part;
        int index = projectId.indexOf('/');
        if (index != -1) {
            projectId = part.substring(0, index);
        }
        MDC.put(LogConstants.MDC_SECHUB_PROJECT_ID, projectId);
        if (index == -1) {
            return true;
        }
        handleJobParameter(part);
        return true;
    }

    private void handleJobParameter(String part) {
        String[] splitted = part.split("/");
        if (splitted == null || splitted.length == 0) {
            return;
        }
        String uuid = null;
        boolean useNext = false;
        for (String split : splitted) {
            if (useNext) {
                uuid = split;
                break;
            }
            if ("job".equals(split) || "false-positive".equals(split)) {
                // the new alternative way to handle false positives does not contain a job uuid
                if (!part.contains("false-positive/project-data")) {
                    useNext = true;
                }
            }
        }
        if (uuid == null) {
            return;
        }
        try {
            UUID jobUUID = UUID.fromString(uuid);
            MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, jobUUID.toString());

        } catch (IllegalArgumentException e) {
            LOG.warn("Expected a UUID inside url but was no sechub job UUID");
        }
        return;
    }

}
