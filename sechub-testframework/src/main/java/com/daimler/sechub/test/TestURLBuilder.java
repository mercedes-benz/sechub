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

        FINDING_ID("findingId"),

        PROFILE_ID("profileId"),

        UUID_PARAMETER("uuid"),;

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

    /**
     * Special url builder - only for PDS parts
     * 
     * @author Albert Tregnaghi
     *
     */
    public class ProductDelegationServerUrlsBuilder {

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
    }

    public ProductDelegationServerUrlsBuilder pds() {
        return new ProductDelegationServerUrlsBuilder();
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

    public String buildUserAddsFalsePositiveJobDataListForProject(String projectId) {
        return buildUrl(API_PROJECT, projectId, "false-positives");
    }

    public String buildUserRemovesFalsePositiveEntryFromProject(String projectId, String jobUUID, String findingId) {
        return buildUrl(API_PROJECT, projectId, "false-positive", jobUUID, findingId);
    }

    public String buildUserFetchesFalsePositiveConfigurationOfProject(String projectId) {
        return buildUrl(API_PROJECT, projectId, "false-positives");
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
    
    public String buildAdminChangesProjectDescriptionUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildAdminAssignsOwnerToProjectUrl(String projectId, String userId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "owner", userId);
    }
    
    public String buildAdminAssignsUserToProjectUrl(String projectId, String userId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "membership", userId);
    }

    public String buildAdminUnassignsUserFromProjectUrl(String projectId, String userId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "membership", userId);
    }

    public String buildAdminFetchProjectInfoUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId);
    }

    public String buildUpdateProjectWhiteListUrl(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "whitelist");
    }
    
    public String buildUpdateProjectMetaData(String projectId) {
        return buildUrl(API_ADMIN_PROJECT, projectId, "metadata");
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

    public String buildGetFalsePositiveConfigurationOfProject(String projectId) {
        return buildUrl(API_PROJECT, projectId, "false-positives");
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
    /* +............................ admin/config..............................+ */
    /* +-----------------------------------------------------------------------+ */

    public String buildAdminCreatesProductExecutionProfile(String profileId) {
        return buildUrl(API_ADMIN_CONFIG, "execution/profile", profileId);
    }

    public String buildAdminUpdatesProductExecutionProfile(String profileId) {
        return buildUrl(API_ADMIN_CONFIG, "execution/profile", profileId);
    }

    public String buildAdminAddsProjectToExecutionProfile(String profileId, String projectId) {
        return buildUrl(API_ADMIN_CONFIG, "execution/profile", profileId, "project", projectId);
    }

    public String buildAdminRemovesProjectFromExecutionProfile(String profileId, String projectId) {
        return buildUrl(API_ADMIN_CONFIG, "execution/profile", profileId, "project", projectId);
    }

    public String buildAdminFetchesProductExecutionProfile(String profileId) {
        return buildUrl(API_ADMIN_CONFIG, "execution/profile", profileId);
    }

    public String buildAdminDeletesProductExecutionProfile(String profileId) {
        return buildUrl(API_ADMIN_CONFIG, "execution/profile", profileId);
    }

    public String buildAdminFetchesListOfProductExecutionProfiles() {
        return buildUrl(API_ADMIN_CONFIG, "execution/profiles");
    }

    public String buildAdminCreatesProductExecutorConfig() {
        return buildUrl(API_ADMIN_CONFIG, "executor");
    }

    public String buildAdminFetchesProductExecutorConfig(String uuid) {
        return buildUrl(API_ADMIN_CONFIG, "executor", uuid);
    }

    public String buildAdminFetchesProductExecutorConfig(UUID uuid) {
        return buildAdminFetchesProductExecutorConfig(uuid.toString());
    }

    public String buildAdminUpdatesProductExecutorConfig(String uuid) {
        return buildUrl(API_ADMIN_CONFIG, "executor", uuid);
    }

    public String buildAdminUpdatesProductExecutorConfig(UUID uuid) {
        return buildAdminUpdatesProductExecutorConfig(uuid.toString());
    }

    public String buildAdminFetchesListOfProductExecutionConfigurations() {
        return buildUrl(API_ADMIN_CONFIG, "executors");
    }

    public String buildAdminDeletesProductExecutorConfig(String uuid) {
        return buildUrl(API_ADMIN_CONFIG, "executor", uuid);
    }

    public String buildAdminDeletesProductExecutorConfig(UUID uuid) {
        return buildAdminDeletesProductExecutorConfig(uuid.toString());
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
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/scan/productresult/all-shrinked/" + maxLength);
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
    
    public String buildSetSchedulerStrategyIdUrl(String strategyId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/scheduler/strategy/" + strategyId);
    }

    /**
     * Integration test only URL!
     *
     * @return url for integration test check
     */
    public String buildIntegrationTestIsAliveUrl() {
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

    public String buildIntegrationTestDeleteAllWaitingJobsUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/jobs/waiting");
    }

    public String buildIntegrationTestCancelAllScanJobsUrl() {
        return buildUrl(API_ANONYMOUS, "integrationtest/scan/cancel/jobs");
    }

    public String buildIntegrationTestRevertJobAsStillRunning(UUID sechubJobUUID) {
        return buildUrl(API_ANONYMOUS, "integrationtest/schedule/revert/job/" + sechubJobUUID.toString() + "/still-running");
    }

    public String buildIntegrationTestRevertJobAsStillNotApproved(UUID sechubJobUUID) {
        return buildUrl(API_ANONYMOUS, "integrationtest/schedule/revert/job/" + sechubJobUUID.toString() + "/still-not-approved");
    }

    public String buildIntegrationTestFakeProductResult(String projectId, UUID sechubJobUUID, String productIdentifier) {
        return buildUrl(API_ANONYMOUS, "integrationtest/project/" + projectId + "/job/" + sechubJobUUID + "/scan/productresult/" + productIdentifier);
    }

    public String buildintegrationTestDeleteProductResults(UUID sechubJobUUID) {
        return buildUrl(API_ANONYMOUS, "integrationtest/job/" + sechubJobUUID + "/productresults");
    }

    public String buildIntegrationTestCountProductResults(UUID sechubJobUUID) {
        return buildUrl(API_ANONYMOUS, "integrationtest/job/" + sechubJobUUID + "/productresults-count");
    }

    public String buildintegrationTestDeleteAllProductExecutorConfigurations() {
        return buildUrl(API_ANONYMOUS, "integrationtest/config/executors");
    }

    public String buildintegrationTestIsExecutionProfileExisting(String profileId) {
        return buildUrl(API_ANONYMOUS, "integrationtest/config/execution/profile/" + profileId + "/exists");
    }

    public String buildBaseURL() {
        return buildUrl("");
    }

}
