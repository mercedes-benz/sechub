// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.util.UUID;

public class TestURLBuilder {

	public enum RestDocPathParameter {
		JOB_UUID("jobUUID"),

		PROJECT_ID("projectId"),

		USER_ID("userId"),

		ONE_TIME_TOKEN("oneTimeToken"),

		EMAIL_ADDRESS("emailAddress"),

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
		 * @return The name of the parameter - e.g. when path element is "{userId}" then this method returns "userId".
		 */
		public String paramName() {
			return restDocName;
		}

		/**
		 * @return path element in url. For example: when pathName is "userId" this method returns "{userId}"
		 */
		public String pathElement() {
			return urlPart;
		}
	}

	private static final String API_ADMIN = "/api/admin";
	private static final String API_USER = "/api/user";
	private static final String API_ANONYMOUS = "/api/anonymous";


	private static final String API_ADMIN_USER = API_ADMIN + "/user";
	private static final String API_ADMIN_PROJECT = API_ADMIN + "/project";
	private static final String API_ADMIN_JOB = API_ADMIN + "/job";
	private static final String API_ADMIN_JOBS = API_ADMIN_JOB+"s";
	private static final String API_ADMIN_SCHEDULER = API_ADMIN+"/scheduler";

	private static final String API_ADMIN_SCAN = API_ADMIN+"/scan";
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
		return buildUrl(API_PROJECT, projectId, "job", jobUUID,"approve");
	}

	public String buildGetJobStatusUrl(String projectId, String jobUUID) {
		return buildUrl(API_PROJECT, projectId, "job", jobUUID);
	}

	public String buildGetJobReportUrl(String projectId, UUID jobUUID) {
		return buildUrl(API_PROJECT, projectId, "report", jobUUID);
	}

	public String buildUploadSourceCodeUrl(String projectId, UUID jobUUID) {
		return buildUploadSourceCodeUrl(projectId,jobUUID.toString());
	}

	public String buildUploadSourceCodeUrl(String projectId, String jobUUID) {
		return buildUrl(API_PROJECT, projectId, "job", jobUUID,"sourcecode");
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
		return buildUrl(API_ANONYMOUS,"refresh/apitoken",emailAddress);
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
		return buildUrl(API_ADMIN_PROJECT, projectId,"scan","logs");
	}


	/* +-----------------------------------------------------------------------+ */
	/* +............................ project ..................................+ */
	/* +-----------------------------------------------------------------------+ */

	public String buildFetchJobStatus(String projectId, String jobUUID) {
		return buildUrl(API_PROJECT,projectId,"job",jobUUID);
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
	/* +-----------------------------------------------------------------------+ */
	/* +............................ admin/scheduler/..........................+ */
	/* +-----------------------------------------------------------------------+ */
	public String buildAdminDisablesSchedulerJobProcessing() {
		return buildUrl(API_ADMIN_SCHEDULER, "disable/job-processing");
	}

	public String buildAdminEnablesSchedulerJobProcessing() {
		return buildUrl(API_ADMIN_SCHEDULER, "enable/job-processing");
	}
	/* +-----------------------------------------------------------------------+ */
	/* +............................ admin/admin...............................+ */
	/* +-----------------------------------------------------------------------+ */
	public String buildAdminGrantsSuperAdminRightsTo(String targetUser) {
		return buildUrl(API_ADMIN_USER, targetUser, "grant","superadmin");
	}

	public String buildAdminRevokesSuperAdminRightsFrom(String targetUser) {
		return buildUrl(API_ADMIN_USER, targetUser, "revoke","superadmin");
	}


	/* +-----------------------------------------------------------------------+ */
	/* +............................ integration test special (anonymous) .....+ */
	/* +-----------------------------------------------------------------------+ */

	public String buildFetchEmailsFromMockMailServiceUrl(String emailAdress) {
		return buildUrl(API_ANONYMOUS, "integrationtest/mock/emails/to", emailAdress);
	}

	public String buildResetAllMockMailsUrl() {
		return buildUrl(API_ANONYMOUS, "integrationtest/mock/emails");
	}

	public String buildIsAliveUrl() {
		return buildUrl(API_ANONYMOUS, "integrationtest/alive");
	}

	public String buildGetFileUpload(String projectId, String jobUUID,String fileName) {
		return buildUrl(API_ANONYMOUS,"integrationtest/"+projectId+"/"+jobUUID+"/uploaded/"+fileName);
	}

	public String buildServerURL() {
		return createRootPath();
	}
	public String buildGetServerVersionUrl() {
		return buildUrl(API_ANONYMOUS,"info/version");
	}


	/* +-----------------------------------------------------------------------+ */
	/* +............................ integration test special (from user) .....+ */
	/* +-----------------------------------------------------------------------+ */

	public String buildCheckRoleUser() {
		return  buildUrl(API_USER , "integrationtest/check/role/user");
	}
	public String buildCheckRoleOwner() {
		return  buildUrl(API_USER , "integrationtest/check/role/owner");
	}

	public String buildFetchReport(String projectId, UUID sechubJobUUID) {
		return buildUrl(API_PROJECT, projectId, "report",sechubJobUUID.toString());
	}






}
