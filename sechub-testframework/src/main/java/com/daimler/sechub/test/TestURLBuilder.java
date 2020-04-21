// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.util.UUID;

public class TestURLBuilder {

    /**
     * This enum represents common used identifiers inside rest documentation. Use
     * this enumeration for building urls inside your RestDoc tests and use
     * parameters for data, so documentation is using these parameters - see
     * references for examples.
     * 
     * @author Albert Tregnaghi
     *
     */
    public enum RestDocPathParameter {
        JOB_UUID("jobUUID"),

        PROJECT_ID("projectId"),

        USER_ID("userId"),

        ONE_TIME_TOKEN("oneTimeToken"),

        EMAIL_ADDRESS("emailAddress"),

        MAPPING_ID("mappingId"),

        ;

        private String restDocName;
        private String urlPart;

        private RestDocPathParameter(String id) {
            this.restDocName = id;
            this.urlPart = "{" + id + "}";
        }

        /**
         *
         * We do NOT use name() because its an enum...
         *
         * @return The name of the parameter - e.g. when path element is "{userId}" then
         *         this method returns "userId".
         */
        public String paramName() {
            return restDocName;
        }

        /**
         * @return path element in url. For example: when pathName is "userId" this
         *         method returns "{userId}"
         */
        public String pathElement() {
            return urlPart;
        }
    }

    private static final String API_ADMIN = "/api/admin";
    private static final String API_USER = "/api/user";
    private static final String API_OWNER = "/api/owner";
    private static final String API_ANONYMOUS = "/api/anonymous";

    private static final String API_ADMIN_USER = API_ADMIN + "/user";
    private static final String API_ADMIN_PROJECT = API_ADMIN + "/project";
    private static final String API_ADMIN_JOB = API_ADMIN + "/job";
    private static final String API_ADMIN_JOBS = API_ADMIN_JOB + "s";
    private static final String API_ADMIN_SCHEDULER = API_ADMIN + "/scheduler";

    private static final String API_ADMIN_SCAN = API_ADMIN + "/scan";
    private static final String API_ADMIN_CONFIG = API_ADMIN + "/config";
    private static final String API_ADMIN_CONFIG_MAPPING = API_ADMIN_CONFIG + "/mapping";
    private static final String API_PROJECT = "/api/project";
    private String protocol;
    private String hostname;
    private int port;

    public static TestURLBuilder https(int port) {
        return new TestURLBuilder("https", port);
    }

    public static TestURLBuilder http(int port) {
        return new TestURLBuilder("http", port);
    }

    public TestURLBuilder(String protocol, int port) {
        this(protocol, port, "localhost");
    }

    public TestURLBuilder(String protocol, int port, String hostname) {
        this.protocol = protocol;
        this.port = port;
        this.hostname = hostname;
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ common ...................................+ */
    /* +-----------------------------------------------------------------------+ */

    private String buildUrl(String custom, Object... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append(createRootPath());
        sb.append(custom);
        for (Object pathVariable : parts) {
            sb.append("/");
            sb.append(pathVariable);
        }
        return sb.toString();
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ CLI /Execute .............................+ */
    /* +-----------------------------------------------------------------------+ */
    public String buildAddJobUrl(String projectId) {
        return buildUrl(API_PROJECT, projectId, "job");
    }

    public String buildApproveJobUrl(String projectId, String jobUUID) {
        return buildUrl(API_PROJECT, projectId, "job", jobUUID, "approve");
    }

    public String buildGetJobStatusUrl(String projectId, String jobUUID) {
        return buildUrl(API_PROJECT, projectId, "job", jobUUID);
    }

    public String buildGetJobReportUrl(String projectId, UUID jobUUID) {
        return buildUrl(API_PROJECT, projectId, "report", jobUUID);
    }

    public String buildUploadSourceCodeUrl(String projectId, UUID jobUUID) {
        return buildUploadSourceCodeUrl(projectId, jobUUID.toString());
    }

    public String buildUploadSourceCodeUrl(String projectId, String jobUUID) {
        return buildUrl(API_PROJECT, projectId, "job", jobUUID, "sourcecode");
    }
    
    /* +-----------------------------------------------------------------------+ */
    /* +............................ anonymous ................................+ */
    /* +-----------------------------------------------------------------------+ */

    public String buildUserSignUpUrl() {
        return buildUrl(API_ANONYMOUS, "signup");
    }

    public String buildAnonymousGetNewApiTokenByLinkWithOneTimeTokenUrl(String oneTimeToken) {
        return buildUrl(API_ANONYMOUS, "apitoken", oneTimeToken);
    }

    public String buildAnonymousRequestNewApiToken(String emailAddress) {
        return buildUrl(API_ANONYMOUS, "refresh/apitoken", emailAddress);
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/signup .............................+ */
    /* +-----------------------------------------------------------------------+ */

    public String buildAdminAcceptsUserSignUpUrl(String userId) {
        return buildUrl(API_ADMIN, "signup/accept", userId);
    }

    public String buildAdminDeletesUserSignUpUrl(String userId) {
        return buildUrl(API_ADMIN, "signup", userId);
    }

    public String buildAdminListsUserSignupsUrl() {
        return buildUrl(API_ADMIN, "signups");
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/users ..............................+ */
    /* +-----------------------------------------------------------------------+ */

    public String buildDeleteUserUrl(String userId) {
        return buildUrl(API_ADMIN_USER, userId);
    }

    public String buildGetUserDetailsUrl(String userId) {
        return buildUrl(API_ADMIN_USER, userId);
    }

    public String buildAdminListsUsersUrl() {
        return buildUrl(API_ADMIN, "users");
    }

    public String buildAdminListsAdminsUrl() {
        return buildUrl(API_ADMIN, "admins");
    }

    public String buildAdminDeletesUserUrl(String userId) {
        return buildUrl(API_ADMIN_USER, userId);
    }

    public String buildAdminShowsUserDetailsUrl(String userId) {
        return buildUrl(API_ADMIN_USER, userId);
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/projects ...........................+ */
    /* +-----------------------------------------------------------------------+ */

    private String createRootPath() {
        return protocol + "://" + hostname + ":" + port;
    }

    public String buildDeleteProjectUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildAdminCreatesProjectUrl() {
        return buildUrl(API_ADMIN_PROJECT);
    }

    public String buildAdminGetProjectDetailsUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildAdminListsProjectsUrl() {
        return buildUrl(API_ADMIN, "projects");
    }

    public String buildAdminShowsProjectDetailsUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildAdminAssignsUserToProjectUrl(String userId, String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "membership", userId);
    }

    public String buildAdminUnassignsUserFromProjectUrl(String userId, String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "membership", userId);
    }

    public String buildAdminFetchProjectInfoUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildUpdateProjectWhiteListUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "whitelist");
    }

    public String buildAdminDeletesProject(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildAdminFetchesScanLogsForProject(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "scan", "logs");
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ project ..................................+ */
    /* +-----------------------------------------------------------------------+ */

    public String buildFetchJobStatus(String projectId, String jobUUID) {
        return buildUrl(API_PROJECT, projectId, "job", jobUUID);
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/jobs ...............................+ */
    /* +-----------------------------------------------------------------------+ */
    public String buildAdminFetchAllRunningJobsUrl() {
        return buildUrl(API_ADMIN_JOBS, "running");
    }

    public String buildAdminDownloadsZipFileContainingFullScanDataFor(UUID sechubJobUUID) {
        return buildUrl(API_ADMIN_SCAN, "download", sechubJobUUID);
    }

    public String buildAdminCancelsJob(UUID jobUUID) {
        return buildUrl(API_ADMIN_JOBS, "cancel", jobUUID);
    }
    
    public String buildAdminRestartsJob(UUID jobUUID) {
        return buildUrl(API_ADMIN_JOBS, "restart", jobUUID);
    }
    
    public String buildAdminRestartsJobHard(UUID jobUUID) {
        return buildUrl(API_ADMIN_JOBS, "restart-hard", jobUUID);
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/scheduler/..........................+ */
    /* +-----------------------------------------------------------------------+ */
    public String buildAdminDisablesSchedulerJobProcessing() {
        return buildUrl(API_ADMIN_SCHEDULER, "disable/job-processing");
    }

    public String buildAdminEnablesSchedulerJobProcessing() {
        return buildUrl(API_ADMIN_SCHEDULER, "enable/job-processing");
    }

    public String buildAdminTriggersRefreshOfSchedulerStatus() {
        return buildUrl(API_ADMIN_SCHEDULER, "status", "refresh");
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/status/.............................+ */
    /* +-----------------------------------------------------------------------+ */
    public String buildAdminListsStatusEntries() {
        return buildUrl(API_ADMIN, "status");
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ admin/admin...............................+ */
    /* +-----------------------------------------------------------------------+ */
    public String buildAdminGrantsSuperAdminRightsTo(String targetUser) {
        return buildUrl(API_ADMIN_USER, targetUser, "grant", "superadmin");
    }

    public String buildAdminRevokesSuperAdminRightsFrom(String targetUser) {
        return buildUrl(API_ADMIN_USER, targetUser, "revoke", "superadmin");
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ integration test special (anonymous) .....+ */
    /* +-----------------------------------------------------------------------+ */
    public String buildCountProjectScanAccess(String projectId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/scan/access/count");
    }

    public String buildCountProjectScheduleAccess(String projectId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/schedule/access/count");
    }

    public String buildCountProjectProductResults(String projectId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/scan/productresult/count");
    }
    
    public String buildFetchAllProjectProductResultsButShrinked(String projectId, int maxLength) {
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/scan/productresult/all-shrinked/"+maxLength);
    }

    public String buildCountProjectScanReports(String projectId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/scan/report/count");
    }

    public String buildFetchEmailsFromMockMailServiceUrl(String emailAdress) {
        return buildUrl(API_ANONYMOUS, "integrationtest/mock/emails/to", emailAdress);
    }

    public String buildResetAllMockMailsUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/mock/emails");
    }

    /**
     * Integration test only URL!
     *
     * @return url for integration test check
     */
    public String buildIsAliveUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/alive");
    }

    public String buildCheckIsAliveUrl() {
        return buildUrl(API_ANONYMOUS, "check/alive");
    }

    public String buildGetFileUpload(String projectId, String jobUUID, String fileName) {
        return buildUrl(API_ANONYMOUS, "integrationtest/" + projectId + "/" + jobUUID + "/uploaded/" + fileName);
    }

    public String buildServerURL() {
        return createRootPath();
    }

    public String buildGetServerVersionUrl() {
        return buildUrl(API_ADMIN, "info/version");
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ integration test special (from user) .....+ */
    /* +-----------------------------------------------------------------------+ */

    public String buildIntegrationTestLogInfoUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/log/info");
    }
    
    public String buildCheckRoleUser() {
        return buildUrl(API_USER, "integrationtest/check/role/user");
    }

    public String buildCheckRoleOwner() {
        return buildUrl(API_OWNER, "integrationtest/check/role/owner");
    }

    public String buildFetchReport(String projectId, UUID sechubJobUUID) {
        return buildUrl(API_PROJECT, projectId, "report", sechubJobUUID.toString());
    }

    public String buildIntegrationTestChangeMappingDirectlyURL(String mappingId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/config/scan/mapping/", mappingId);
    }

    public String buildIntegrationTestFetchMappingDirectlyURL(String mappingId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/config/scan/mapping/", mappingId);
    }

    public String buildIntegrationTestGetIdForNameByNamePatternProvider(String namePatternProviderId, String name) {
        return buildUrl(API_ANONYMOUS, "integrationtest/config/namepattern", namePatternProviderId, name);
    }

    public String buildIntegrationTestRefreshScanConfigURL() {
        return buildUrl(API_ANONYMOUS, "integrationtest/config/scan/scanconfig/refresh");
    }

    public String buildClearMetaDataInspectionURL() {
        return buildUrl(API_ANONYMOUS, "integrationtest/metadata/inspections");
    }

    public String buildFetchMetaDataInspectionsURL() {
        return buildUrl(API_ANONYMOUS, "integrationtest/metadata/inspections");
    }

    public String buildSetProjectMockConfiguration(String projectId) {
        return buildUrl(API_PROJECT, projectId, "mockdata");
    }

    public String buildGetProjectMockConfiguration(String projectId) {
        return buildUrl(API_PROJECT, projectId, "mockdata");
    }

    public String buildUpdateMapping(String mappingId) {
        return buildUrl(API_ADMIN_CONFIG_MAPPING, mappingId);
    }

    public String buildGetMapping(String mappingId) {
        return buildUrl(API_ADMIN_CONFIG_MAPPING, mappingId);
    }

    public String buildIntegrationTestResetAndStopEventInspection() {
        return buildUrl(API_ANONYMOUS, "integrationtest/event/inspection/reset-and-stop");
    }
    
    public String buildIntegrationTestStartEventInspection() {
        return buildUrl(API_ANONYMOUS, "integrationtest/event/inspection/start");
    }
    
    public String buildIntegrationTestFetchEventInspectionStatus() {
        return buildUrl(API_ANONYMOUS, "integrationtest/event/inspection/status");
    }
    
    public String buildIntegrationTestFetchEventInspectionHistory() {
        return buildUrl(API_ANONYMOUS, "integrationtest/event/inspection/history");
    }

    public String buildintegrationTestDeleteAllWaitingJobsUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/jobs/waiting");
    }
    
    public String buildintegrationTestCancelAllScanJobsUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/scan/cancel/jobs");
    }
    
    public String buildintegrationTestRevertJobAsStillRunning(UUID sechubJobUUID) {
        return buildUrl(API_ANONYMOUS, "integrationtest/schedule/revert/job/"+sechubJobUUID.toString()+"/still-running");
    }

    

  

}
