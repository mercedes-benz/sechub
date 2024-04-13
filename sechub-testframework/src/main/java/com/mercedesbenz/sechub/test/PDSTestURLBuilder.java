// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.util.UUID;

public class PDSTestURLBuilder extends AbstractTestURLBuilder {

    private static final String API_PDS_JOB = "/api/job";
    private static final String API_PDS_ANONYMOUS = "/api/anonymous";
    private static final String API_PDS_INTEGRATIONTEST = "/api/anonymous/integrationtest";
    private static final String API_PDS_ADMIN = "/api/admin";

    private static final String API_ADMIN = "/api/admin";
    private static final String API_ANONYMOUS = "/api/anonymous";

    private static final String API_ADMIN_JOB = API_ADMIN + "/job";

    private static final String API_ADMIN_CONFIG = API_ADMIN + "/config";

    public static PDSTestURLBuilder https(int port) {
        return new PDSTestURLBuilder("https", port);
    }

    public static PDSTestURLBuilder http(int port) {
        return new PDSTestURLBuilder("http", port);
    }

    public PDSTestURLBuilder(String protocol, int port) {
        this(protocol, port, "localhost");
    }

    public PDSTestURLBuilder(String protocol, int port, String hostname) {
        super(protocol, port, hostname);
    }

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
        return buildUrl(API_PDS_JOB, jobUUID.toString(), "upload", fileName);
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

    public String buildGetJobResultOrErrorText(UUID jobUUID) {
        return buildUrl(API_PDS_ADMIN, "job", jobUUID, "result");
    }

    public String buildAdminGetServerConfiguration() {
        return buildUrl(API_PDS_ADMIN, "config/server");
    }

    public String buildBaseUrl() {
        return buildUrl("");
    }

    public String buildIntegrationTestFetchStoragePathHistoryEntryForSecHubJob(UUID jobUUID) {
        return buildUrl(API_PDS_INTEGRATIONTEST, "storage", jobUUID, "path");
    }

    public String buildIntegrationTestGetWorkspaceUploadFolder(UUID jobUUID) {
        return buildUrl(API_PDS_INTEGRATIONTEST, jobUUID, "uploadfolder");
    }

    public String buildFetchLastStartedJobUUIDUrl() {
        return buildUrl(API_PDS_INTEGRATIONTEST, "last/started/job/uuid");
    }

    public String buildAdminFetchesJobOutputStreamUrl(UUID jobUUID) {
        return buildUrl(API_ADMIN_JOB, jobUUID, "stream", "output");
    }

    public String buildAdminFetchesJobErrorStreamUrl(UUID jobUUID) {
        return buildUrl(API_ADMIN_JOB, jobUUID, "stream", "error");
    }

    public String buildAdminFetchesJobMetaData(UUID pdsJobUUID) {
        return buildUrl(API_ADMIN_JOB, pdsJobUUID.toString(), "metadata");
    }

    public String buildAdminUpdatesAutoCleanupConfigurationUrl() {
        return buildUrl(API_ADMIN_CONFIG, "autoclean");
    }

    public String buildIntegrationTestFetchAutoCleanupInspectionDeleteCountsUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/autocleanup/inspection/deleteCounts");
    }

    public String buildIntegrationTestResetAutoCleanupInspectionUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/autocleanup/inspection/reset");
    }

    public String buildCheckIsAliveUrl() {
        return buildUrl(API_ANONYMOUS, "check/alive");
    }

    public String buildIntegrationTestIsAliveUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/alive");
    }

    public String buildIntegrationTestLogInfoUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/log/info");
    }

    public String buildAdminFetchesAutoCleanupConfigurationUrl() {
        return buildUrl(API_ADMIN_CONFIG, "autoclean");
    }

}