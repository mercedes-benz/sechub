// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.UUID;

public class PDSUrlBuilder {

    private String baseURL;

    public PDSUrlBuilder(String baseURL) {
        this.baseURL = baseURL;
    }

    private static final String API_PDS_JOB = "/api/job";
    private static final String API_PDS_ANONYMOUS = "/api/anonymous";
    private static final String API_PDS_ADMIN = "/api/admin";

    public String buildCreateJob() {
        return buildUrl(API_PDS_JOB, "create");
    }

    public String buildGetJobStatus(UUID jobUUID) {
        return buildUrl(API_PDS_JOB, jobUUID.toString(), "status");
    }

    public String buildGetJobResult(UUID jobUUID) {
        return buildUrl(API_PDS_JOB, jobUUID.toString(), "result");
    }

    public String buildGetJobMessages(UUID jobUUID) {
        return buildUrl(API_PDS_JOB, jobUUID.toString(), "messages");
    }

    public String buildUpload(UUID jobUUID, String fileName) {
        return buildUpload(jobUUID.toString(), fileName);
    }

    public String buildUpload(String jobUUID, String fileName) {
        return buildUrl(API_PDS_JOB, jobUUID, "upload", fileName);
    }

    public String buildMarkJobReadyToStart(UUID jobUUID) {
        return buildUrl(API_PDS_JOB, jobUUID.toString(), "mark-ready-to-start");
    }

    public String buildCancelJob(UUID jobUUID) {
        return buildUrl(API_PDS_JOB, jobUUID.toString(), "cancel");
    }

    public String buildAdminGetMonitoringStatus() {
        return buildUrl(API_PDS_ADMIN, "monitoring/status");
    }

    public String buildAnonymousCheckAlive() {
        return buildUrl(API_PDS_ANONYMOUS, "check/alive");
    }

    public String buildGetJobResultAsAdmin(UUID jobUUID) {
        return buildUrl(API_PDS_ADMIN, "job", jobUUID, "result");
    }

    public String buildAdminGetServerConfiguration() {
        return buildUrl(API_PDS_ADMIN, "config/server");
    }

    public String buildBaseUrl() {
        return baseURL;
    }

    private String buildUrl(String custom, Object... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildBaseUrl());
        sb.append(custom);
        for (Object pathVariable : parts) {
            sb.append("/");
            sb.append(pathVariable);
        }
        return sb.toString();
    }

}