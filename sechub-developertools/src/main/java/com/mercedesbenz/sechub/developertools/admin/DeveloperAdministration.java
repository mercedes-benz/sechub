// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.domain.scan.product.pds.PDSProductExecutorKeyConstants;
import com.mercedesbenz.sechub.domain.scan.product.pds.SecHubProductExecutionPDSKeyProvider;
import com.mercedesbenz.sechub.integrationtest.api.AsPDSUser;
import com.mercedesbenz.sechub.integrationtest.api.AsUser;
import com.mercedesbenz.sechub.integrationtest.api.FixedTestProject;
import com.mercedesbenz.sechub.integrationtest.api.FixedTestUser;
import com.mercedesbenz.sechub.integrationtest.api.InternalAccess;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestSecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.api.UserContext;
import com.mercedesbenz.sechub.integrationtest.api.WithSecHubClient;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.SimpleTestStringList;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper.RestHelperTarget;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;
import com.mercedesbenz.sechub.test.PDSTestURLBuilder;
import com.mercedesbenz.sechub.test.SecHubTestURLBuilder;
import com.mercedesbenz.sechub.test.TestPDSServerConfiguration;
import com.mercedesbenz.sechub.test.TestPDSServerProductConfig;
import com.mercedesbenz.sechub.test.TestPDSServerProductParameter;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfileList;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfigList;

public class DeveloperAdministration {

    private ConfigProvider provider;
    private AdminUserContext userContext;
    private TestRestHelper restHelper;
    private SecHubTestURLBuilder urlBuilder;
    private ErrorHandler errorHandler;
    private UIContext uiContext;

    private static final DeveloperProjectDetailInformation PROJECT_DETAIL_IMPORTER = new DeveloperProjectDetailInformation();

    public DeveloperAdministration(ConfigProvider provider, ErrorHandler errorHandler, UIContext uiContext) {
        this.provider = provider;
        this.errorHandler = errorHandler;
        this.userContext = new AdminUserContext();
        this.uiContext = uiContext;
        this.restHelper = createTestRestHelperWithErrorHandling(errorHandler, userContext);
    }

    public UIContext getUiContext() {
        return uiContext;
    }

    private TestRestHelper createTestRestHelperWithErrorHandling(ErrorHandler provider, UserContext user) {
        return createTestRestHelperWithErrorHandling(provider, user, RestHelperTarget.SECHUB_SERVER);
    }

    /**
     * Will update test API server connection data - makes it possible to use
     * directly test API methods without writing duplicates for developer admin ui
     * (faster development)
     *
     * @param server
     * @param portNumber
     */
    public void updateTestAPIServerConnection(String server, int portNumber) {
        IntegrationTestContext integrationTestContext = IntegrationTestContext.get();
        integrationTestContext.setHostname(server);
        integrationTestContext.setPort(portNumber);
        integrationTestContext.rebuild();
    }

    public void updateTestAPISuperAdmin(String userId, String apiToken) {
        IntegrationTestContext integrationTestContext = IntegrationTestContext.get();
        integrationTestContext.setSuperAdminUser(new FixedTestUser(userId, apiToken));
        integrationTestContext.rebuild();
    }

    private TestRestHelper createTestRestHelperWithErrorHandling(ErrorHandler provider, UserContext user, RestHelperTarget restHelperTarget) {
        return new TestRestHelper(user, restHelperTarget) {

            @Override
            protected ResponseErrorHandler createErrorHandler() {
                return new DefaultResponseErrorHandler() {

                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        StringBuilder httpResponseProblem = new StringBuilder();
                        String statusText = response.getStatusText();
                        httpResponseProblem.append("status code::");
                        httpResponseProblem.append(response.getStatusCode());
                        if (statusText != null) {
                            httpResponseProblem.append(", text:");
                            httpResponseProblem.append(statusText);
                        }
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"))) {
                            String line = null;
                            httpResponseProblem.append(",body:");
                            while ((line = br.readLine()) != null) {
                                httpResponseProblem.append("\n");
                                httpResponseProblem.append(line);
                            }
                        } catch (IOException e) {
                            provider.handleError("failed to read response body:" + e.getMessage());
                        }
                        StringBuilder errorOutput = new StringBuilder();
                        errorOutput.append("******************************************************************\n");
                        errorOutput.append("***                        SENT                                ***\n");
                        errorOutput.append("******************************************************************\n");
                        errorOutput.append("Last URL call:").append(TestRestHelper.getLastUrl());
                        if (TestRestHelper.getLastData() != null) {
                            errorOutput.append("\nWith data:").append(TestRestHelper.getLastData());
                        }
                        errorOutput.append("\n");
                        errorOutput.append("******************************************************************\n");
                        errorOutput.append("***                     RECEIVED                               ***\n");
                        errorOutput.append("******************************************************************\n");
                        errorOutput.append(httpResponseProblem).append("\n");
                        provider.handleError(errorOutput.toString());
                    }
                };
            }

        };
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public SecHubTestURLBuilder getUrlBuilder() {
        if (urlBuilder == null) {
            int port = provider.getPort();
            String server = provider.getServer();
            urlBuilder = new SecHubTestURLBuilder(provider.getProtocol(), port, server);
        }
        return urlBuilder;
    }

    public String fetchSignups() {
        return getRestHelper().getJSON(getUrlBuilder().buildAdminListsUserSignupsUrl());
    }

    public TestRestHelper getRestHelper() {
        return restHelper;
    }

    public PDSAdministration pds(String hostname, int port, String userid, String apiToken) {
        return new PDSAdministration(hostname, port, userid, apiToken);
    }

    public class PDSAdministration {

        private PDSTestURLBuilder pdsUrlBuilder;

        public PDSTestURLBuilder getUrlBuilder() {
            return pdsUrlBuilder;
        }

        public PDSAdministration(String hostname, int port, String userid, String apiToken) {
            pdsUrlBuilder = new PDSTestURLBuilder("https", port, hostname);
            TestUser user = new FixedTestUser(userid, apiToken, userid + "_pds@example.com");
            restHelper = new TestRestHelper(user, RestHelperTarget.SECHUB_PDS);
        }

        public String fetchServerConfigurationAsString() {
            return restHelper.getJSON(pdsUrlBuilder.buildAdminGetServerConfiguration());
        }

        public String getServerAlive() {
            return restHelper.headStringFromURL(pdsUrlBuilder.buildAnonymousCheckAlive());
        }

        public String createPDSJob(UUID sechubJobUUID, String productId, Map<String, String> params) {
            return TestAPI.createPDSJobFor(sechubJobUUID, params, productId, restHelper, pdsUrlBuilder);
        }

        public String getExecutionStatus() {
            return restHelper.getJSON(pdsUrlBuilder.buildAdminGetMonitoringStatus());
        }

        public String getJobResultOrError(String jobUUID) {
            return restHelper.getJSON(pdsUrlBuilder.buildGetJobResultOrErrorText(UUID.fromString(jobUUID)));
        }

        public String getJobStatus(String jobUUID) {
            return restHelper.getJSON(pdsUrlBuilder.buildGetJobStatus(UUID.fromString(jobUUID)));
        }

        public UUID getUUIDOfLastStartedJob() {
            return restHelper.getUUIDFromURL(pdsUrlBuilder.buildFetchLastStartedJobUUIDUrl());
        }

        public String markJobAsReadyToStart(UUID jobUUID) {
            AsPDSUser.markJobAsReadyToStart(jobUUID, restHelper, pdsUrlBuilder);
            return "triggered";
        }

        public String upload(UUID pdsJobUUID, File file, String uploadName) {
            AsPDSUser.upload(pdsUrlBuilder, restHelper, pdsJobUUID, uploadName, file);
            ;
            return "upload done - using uploadname:" + uploadName;
        }

        public TestPDSServerConfiguration fetchServerConfiguration() {
            return JSONConverter.get().fromJSON(TestPDSServerConfiguration.class, fetchServerConfigurationAsString());
        }

        public String getJobOutputStream(UUID jobUUID) {
            return restHelper.getStringFromURL(pdsUrlBuilder.buildAdminFetchesJobOutputStreamUrl(jobUUID));
        }

        public String getJobErrorStream(UUID jobUUID) {
            return restHelper.getStringFromURL(pdsUrlBuilder.buildAdminFetchesJobErrorStreamUrl(jobUUID));
        }

        public String getJobMessages(UUID jobUUID) {
            return restHelper.getStringFromURL(pdsUrlBuilder.buildGetJobMessages(jobUUID));
        }

        public ProductIdentifier findProductIdentifier(TestPDSServerConfiguration config, String productId) {
            for (TestPDSServerProductConfig c : config.products) {
                if (c.id.equals(productId)) {
                    switch (c.scanType) {
                    case "codeScan":
                        return ProductIdentifier.PDS_CODESCAN;
                    case "webScan":
                        return ProductIdentifier.PDS_WEBSCAN;
                    case "infraScan":
                        return ProductIdentifier.PDS_INFRASCAN;
                    default:
                        throw new IllegalStateException("Unsupported type:" + c.scanType);
                    }
                }
            }
            throw new IllegalStateException("Product id not found:" + productId);
        }

        public String createExampleProperitesAsString(TestPDSServerConfiguration config, String productId) {
            StringBuilder sb = new StringBuilder();
            for (TestPDSServerProductConfig c : config.products) {
                if (c.id.equals(productId)) {
                /* @formatter:off */
                    sb.append("# ###################################\n");
                    sb.append("# Default example configuration for\n");
                    sb.append("# PDS server at :").append(getUiContext().getPDSConfigurationUI().getHostName()).append(":")
                            .append(getUiContext().getPDSConfigurationUI().getPort()).append("\n");
                    sb.append("# You can paste these parts directly into PDS server configuration!\n");
                    sb.append("# ###################################\n");
                    sb.append("\n");
                    sb.append("# ---------------------\n");
                    sb.append("# sechub call needs:   \n");
                    sb.append("# ---------------------\n");
                    sb.append(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER+"=" + c.id + "\n");
                    sb.append("# Optional:\n");
                    sb.append("# "+SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTERNET.getKey().getId()+"=true #does stop executing this one for internet\n");
                    sb.append("# "+SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTRANET.getKey().getId()+"=true #does stop executing this one for intranet \n");
                    sb.append("# "+PDSProductExecutorKeyConstants.TIME_TO_WAIT_NEXT_CHECK_MILLIS+"=1000 #1000 is 1 second, 0 will fallback to 500 ms, if not set PDSInstallSetup values will be used\n");
                    sb.append("# "+PDSProductExecutorKeyConstants.TIME_OUT_IN_MINUTES+"=120 #would timeout pds connection after 2 hours, if not set PDSInstallSetup values will be used\n");
                    sb.append("# "+PDSProductExecutorKeyConstants.TRUST_ALL_CERTIFICATES+"=true # will trust any PDS server. Use this only in NON PRODUCTION environments (e.g when using self signed certificate)\n\n");
                    sb.append("# ---------------------\n");
                    sb.append("# mandatory parameters:\n");
                    sb.append("# ---------------------\n");
                    for (TestPDSServerProductParameter m : c.parameters.mandatory) {
                        sb.append("# ").append(m.description).append("\n");
                        sb.append(m.key).append("=\n\n");
                    }
                    sb.append("# --------------------\n");
                    sb.append("# optional parameters:\n");
                    sb.append("# --------------------\n");
                    for (TestPDSServerProductParameter m : c.parameters.optional) {
                        sb.append("# ").append(m.description).append("\n");
                        sb.append(m.key).append("=\n\n");
                    }
                }
            }
            /* @formatter:on */
            String properties = sb.toString();
            return properties;
        }

        public String fetchPDSAutoCleanupConfiguration() {
            TestAutoCleanupData configuration = asPDSUser(PDS_ADMIN).fetchAutoCleanupConfiguration();
            return TestJSONHelper.get().createJSON(configuration, true);
        }

        public String updatePDSAutoCleanupConfiguration(String json) {
            asPDSUser(PDS_ADMIN).updateAutoCleanupConfiguration(json);
            return "PDS auto cleanup data has been changed";
        }

    }

    public String acceptSignup(String userId) {
        getRestHelper().post(getUrlBuilder().buildAdminAcceptsUserSignUpUrl(userId));
        return "SENT";
    }

    public String declineSignup(String userId) {
        getRestHelper().delete(getUrlBuilder().buildAdminDeletesUserSignUpUrl(userId));
        return "SENT";
    }

    public String gGrantAdminRightsTo(String targetUser) {
        getRestHelper().post(getUrlBuilder().buildAdminGrantsSuperAdminRightsTo(targetUser));
        return "SENT";
    }

    public String revokeAddminRightsFrom(String targetUser) {
        getRestHelper().post(getUrlBuilder().buildAdminRevokesSuperAdminRightsFrom(targetUser));
        return "SENT";
    }

    public String createNewUserSignup(String name, String email) {

        String json = "{\"apiVersion\":\"1.0\",\r\n" + "		\"userId\":\"" + name + "\",\r\n" + "		\"emailAddress\":\"" + email + "\"}";
        return getRestHelper().postJson(getUrlBuilder().buildUserSignUpUrl(), json);
    }

    public String fetchUserList() {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminListsUsersUrl());
    }

    public String fetchAdminList() {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminListsAdminsUrl());
    }

    public String fetchRunningJobsList() {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminFetchAllRunningJobsUrl());
    }

    /*
     * --------------- Execution configuration ------------------------
     */
    public String fetchExecutionProfiles() {
        return asTestUser().fetchProductExecutionProfilesAsJSON();
    }

    public TestExecutorConfig fetchExecutorConfiguration(UUID uuid) {
        return asTestUser().fetchProductExecutorConfig(uuid);
    }

    public String fetchExecutorConfigurations() {
        return asTestUser().fetchProductExecutorConfigListAsJSON();
    }

    public TestExecutorConfigList fetchExecutorConfigurationList() {
        return asTestUser().fetchProductExecutorConfigList();
    }

    public String deletExecutionConfig(UUID uuid) {
        asTestUser().deleteProductExecutorConfig(uuid);
        return "Deleted product executor config:" + uuid;
    }

    public String deletExecutionProfile(String profileId) {
        InternalAccess.forceDeleteOfProfileEvenDefaults(asTestUser(), profileId);
        return "Deleted product execution profile:" + profileId;
    }

    public String createProject(String projectId, String description, String owner, List<String> whiteListURLs, Map<String, String> metaData) {
        /* @formatter:off */
		StringBuilder json = new StringBuilder();
		if (description == null) {
			description = "";
		}
		TestJSONHelper jsonHelper = TestJSONHelper.get();
		json.append("{\n" +
				" \"apiVersion\":\"1.0\",\n" +
				" \"name\":\""+projectId+"\",\n" +
				" \"owner\":\""+owner+"\",\n" +
				" \"description\":\""+description+"\"");
		if (! whiteListURLs.isEmpty()) {
			json.append(",\n \"whiteList\" : {\"uris\":[");

			for (Iterator<String> it = whiteListURLs.iterator(); it.hasNext();) {
				String url = it.next();
				json.append("\"" + url + "\"");
				if (it.hasNext()){
					json.append(",");
				}
			}
			json.append("]\n");
			json.append("                 }\n");
		}

		if (!metaData.isEmpty()) {
			json.append(",\n \"metaData\" : {\n");

			for(Iterator<Map.Entry<String, String>> it = metaData.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<String, String> pair = it.next();
				String key = pair.getKey();
				String value = pair.getValue();
				json.append("\"" + key + "\":\"" + value + "\"");
				if (it.hasNext()) {
					json.append(",\n");
				}
			}

			json.append("\n}\n");
		}

		json.append("}\n");
		jsonHelper.assertValidJson(json.toString());
		/* @formatter:on */
        return getRestHelper().postJson(getUrlBuilder().buildAdminCreatesProjectUrl(), json.toString());
    }

    public String fetchProjectList() {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminListsProjectsUrl());
    }

    public List<String> fetchProjectIdList() {
        String json = fetchProjectList();
        SimpleTestStringList list = JSONConverter.get().fromJSON(SimpleTestStringList.class, json);
        return list;
    }

    public String fetchProjectInfo(String projectId) {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminShowsProjectDetailsUrl(projectId));
    }

    public String fetchProjectDescription(String projectId) {
        String json = fetchProjectInfo(projectId);

        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode jsonNode = jsonHelper.readTree(json);
        return jsonNode.get("description").textValue();
    }

    public DeveloperProjectDetailInformation fetchProjectDetailInformation(String projectId) {
        String json = fetchProjectInfo(projectId);

        return PROJECT_DETAIL_IMPORTER.fromJSON(json);
    }

    public String fetchUserInfo(String userId) {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminShowsUserDetailsUrl(userId));
    }

    public String fetchUserInfoByEmailAddress(String emailAddress) {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminShowsUserDetailsForEmailAddressUrl(emailAddress));
    }

    public List<String> fetchProjectWhiteList(String projectId) {
        List<String> result = new ArrayList<>();
        String json = getRestHelper().getJSON(getUrlBuilder().buildAdminFetchProjectInfoUrl(projectId));
        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode jsonNode = jsonHelper.readTree(json);
        JsonNode whitelist = jsonNode.get("whiteList");
        if (whitelist instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) whitelist;
            for (JsonNode node : arrayNode) {
                String uriText = node.textValue();
                if (!uriText.trim().isEmpty()) {
                    result.add(uriText);
                }
            }
        }

        return result;
    }

    public String fetchProjectMetaData(String projectId) {
        String json = getRestHelper().getJSON(getUrlBuilder().buildAdminFetchProjectInfoUrl(projectId));
        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode jsonNode = jsonHelper.readTree(json);
        JsonNode metaData = jsonNode.get("metaData");

        return metaData.toPrettyString();
    }

    public String fetchProjectScanLogs(String projectId) {
        String json = getRestHelper().getJSON(getUrlBuilder().buildAdminFetchesScanLogsForProject(projectId));
        return json;
    }

    public String fetchJSONReport(String projectId, UUID sechubJobUUID) {
        String json = getRestHelper().getJSON(getUrlBuilder().buildFetchReport(projectId, sechubJobUUID));
        return json;
    }

    public String fetchJobStatus(String projectId, String jobUUID) {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildFetchJobStatus(projectId, jobUUID));
    }

    public void updateProjectWhiteList(String projectId, List<String> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"apiVersion\":\"1.0\", \"whiteList\":{\"uris\":[");
        for (Iterator<String> it = result.iterator(); it.hasNext();) {

            String uri = it.next();
            if (uri == null) {
                continue;
            }

            String trimmedURI = uri.trim();
            if (!trimmedURI.isEmpty()) {
                sb.append("\"");
                sb.append(trimmedURI);
                sb.append("\"");

                if (it.hasNext()) {
                    sb.append(",");
                }
            }

        }
        sb.append("]}}");

        getRestHelper().postJson(getUrlBuilder().buildUpdateProjectWhiteListUrl(projectId), sb.toString());
    }

    public void updateProjectMetaData(String projectId, String result) {

        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode jsonNode = jsonHelper.readTree(result);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"apiVersion\":\"1.0\", \"metaData\":\n" + jsonNode.toPrettyString() + "\n}");

        getRestHelper().postJson(getUrlBuilder().buildUpdateProjectMetaData(projectId), sb.toString());
    }

    public String updateProjectDescription(String projectId, String description) {
        String json = "{\n" + "  \"name\" : \"" + projectId + "\", \n" + "  \"description\" : \"" + description + "\"\n" + "}";

        String url = getUrlBuilder().buildAdminChangesProjectDescriptionUrl(projectId);

        return getRestHelper().postJson(url, json);
    }

    public String assignOwnerToProject(String userId, String projectId) {
        getRestHelper().post(getUrlBuilder().buildAdminChangesProjectOwnerUrl(projectId, userId));
        return "assigned " + userId + " as new owner to project " + projectId;
    }

    public String assignUserToProject(String userId, String projectId) {
        getRestHelper().post(getUrlBuilder().buildAdminAssignsUserToProjectUrl(projectId, userId));
        return "assigned " + userId + " to project " + projectId;
    }

    public String unassignUserFromProject(String userId, String projectId) {
        getRestHelper().delete(getUrlBuilder().buildAdminUnassignsUserFromProjectUrl(projectId, userId));
        return "unassigned " + userId + " to project " + projectId;
    }

    public String deleteProject(String projectId) {
        getRestHelper().delete(getUrlBuilder().buildAdminDeletesProject(projectId));
        return "sent";
    }

    public String deleteUser(String userId) {
        getRestHelper().delete(getUrlBuilder().buildAdminDeletesUserUrl(userId));
        return "sent";
    }

    public String changeUserEmailAddress(String userId, String newEmailAddress) {
        asTestUser().changeEmailAddress(userId, newEmailAddress);
        return "sent";
    }

    public String cancelJob(UUID jobUUID) {
        getRestHelper().post(getUrlBuilder().buildAdminCancelsJob(jobUUID));
        return "cancel triggered";
    }

    public String requestNewApiToken(String emailAddress) {
        getRestHelper().post(getUrlBuilder().buildAnonymousRequestNewApiToken(emailAddress));
        return "Sent request for new API token for email: " + emailAddress + " - New API token will be delivered to this address if user exists!";
    }

    public String enableSchedulerJobProcessing() {
        getRestHelper().post(getUrlBuilder().buildAdminEnablesSchedulerJobProcessing());
        return "triggered enable job processing";
    }

    public String disableSchedulerJobProcessing() {
        getRestHelper().post(getUrlBuilder().buildAdminDisablesSchedulerJobProcessing());
        return "triggered disable job processing";
    }

    public String refreshSchedulerStatus() {
        getRestHelper().post(getUrlBuilder().buildAdminTriggersRefreshOfSchedulerStatus());
        return "triggered refresh for scheduler status";
    }

    public String getStatusList() {
        return getRestHelper().getJSON(getUrlBuilder().buildAdminListsStatusEntries());
    }

    public String checkAlive() {
        return getRestHelper().headStringFromURL(getUrlBuilder().buildCheckIsAliveUrl());
    }

    public String checkVersion() {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildGetServerRuntimeDataUrl());
    }

    public String triggerDownloadFullScan(UUID sechubJobUUID) {

        String url = getUrlBuilder().buildAdminDownloadsZipFileContainingFullScanDataFor(sechubJobUUID);
        return commonTriggerDownloadInBrowser(url);
    }

    /**
     * Creates temporary test user object and provides direct access to integration
     * test object: AsUser. So all things available in integration tests can be done
     * directly without additional methods
     *
     * @return asUser object
     */
    AsUser createAsUserTestObject() {
        String user = provider.getUser();
        String token = provider.getApiToken();
        TestUser testUser = new FixedTestUser(user, token);
        return as(testUser);
    }

    public WithSecHubClient withSecHubClientOnDefinedBinPath() {
        WithSecHubClient withSechubClient = createAsUserTestObject().withSecHubClient();
        String pathToBinaryparentFolder = ConfigurationSetup.SECHUB_PATH_TO_SECHUB_CLIENT_BINARY.getStringValue(null, false);
        withSechubClient.fromPath(pathToBinaryparentFolder);
        if (ConfigurationSetup.isTrustAllDenied()) {
            withSechubClient.denyTrustAll();
        }
        return withSechubClient;

    }

    public String fetchGlobalMappings(String mappingId) {
        String url = getUrlBuilder().buildGetMapping(mappingId);
        return getRestHelper().getJSON(url);
    }

    public String updateGlobalMappings(String mappingId, String mappingDataAsJSON) {
        String url = getUrlBuilder().buildUpdateMapping(mappingId);
        return getRestHelper().putJSON(url, mappingDataAsJSON);
    }

    public String triggerDownloadReport(String projectId, UUID sechubJobUUID) {
        String url = getUrlBuilder().buildFetchReport(projectId, sechubJobUUID);
        return commonTriggerDownloadInBrowser(url);
    }

    public String restartJob(UUID jobUUID) {
        getRestHelper().post(getUrlBuilder().buildAdminRestartsJob(jobUUID));
        return "restart job triggered";
    }

    public String restartJobHard(UUID jobUUID) {
        getRestHelper().post(getUrlBuilder().buildAdminRestartsJobHard(jobUUID));
        return "restart job (hard) triggered";
    }

    public String fetchProjectFalsePositiveConfiguration(String projectId) {
        return getRestHelper().getJSON(getUrlBuilder().buildUserFetchesFalsePositiveConfigurationOfProject(projectId));
    }

    public String markFalsePositivesForProjectByJobData(String projectId, String json) {
        return getRestHelper().putJSON(getUrlBuilder().buildUserAddsFalsePositiveJobDataListForProject(projectId), json);
    }

    public void deleteFalsePositivesForProject(String projectId, UUID jobUUID, int findingId) {
        getRestHelper().delete(getUrlBuilder().buildUserRemovesFalsePositiveEntryFromProject(projectId, jobUUID.toString(), "" + findingId));
    }

    private String commonTriggerDownloadInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return "Cannot open your system browser for url:" + url + ", please copy url and download in your browser manually.";
        }
        return "Triggered download of " + url + " inside your system browser.";
    }

    private class AdminUserContext implements UserContext {

        @Override
        public String getUserId() {
            return provider.getUser();
        }

        @Override
        public String getApiToken() {
            return provider.getApiToken();
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        public TestUser toTestUser() {
            return new FixedTestUser(getUserId(), getApiToken(), getEmail());
        }

        @Override
        public void updateToken(String newToken) {
            /*
             * ignore - we do not need this here, because we just use the edited parts
             * inside text fields
             */
        }

        @Override
        public String getEmail() {
            return "superadmin@example.org";
        }

    }

    public UUID createExecutorConfig(TestExecutorConfig config) {
        return asTestUser().createProductExecutorConfig(config);
    }

    public void updateExecutorConfiguration(TestExecutorConfig config) {
        asTestUser().updateProdcutExecutorConfig(config.uuid, config);
    }

    public void createExecutionProfile(TestExecutionProfile profile) {
        asTestUser().createProductExecutionProfile(profile.id, profile);
    }

    public TestExecutionProfile fetchExecutionProfile(String profileId) {
        return asTestUser().fetchProductExecutionProfile(profileId);
    }

    public void updateExecutionProfile(TestExecutionProfile updatedProfile) {
        asTestUser().updateProductExecutionProfile(updatedProfile.id, updatedProfile);
    }

    private AsUser asTestUser() {
        return as(userContext.toTestUser());
    }

    public TestExecutionProfileList fetchExecutionProfileList() {
        return asTestUser().fetchProductExecutionProfiles();
    }

    public void addProjectIdsToProfile(String profileId, String... projectIds) {
        asTestUser().addProjectIdsToProfile(profileId, projectIds);
    }

    public void removeProjectIdsFromProfile(String profileId, String... projectIds) {
        asTestUser().removeProjectIdsFromProfile(profileId, projectIds);
    }

    public void addProjectIdsToProfile(String profileId, List<String> list) {
        addProjectIdsToProfile(profileId, list.toArray(new String[list.size()]));
    }

    public void removeProjectIdsFromProfile(String profileId, List<String> list) {
        removeProjectIdsFromProfile(profileId, list.toArray(new String[list.size()]));
    }

    public void changeProjectAccessLevel(String projectId, ProjectAccessLevel accessLevel) {
        asTestUser().changeProjectAccessLevel(new FixedTestProject(projectId), accessLevel);
    }

    public String fetchAutoCleanupConfiguration() {
        TestAutoCleanupData configuration = asTestUser().fetchAutoCleanupConfiguration();
        return TestJSONHelper.get().createJSON(configuration, true);
    }

    public String updateAutoCleanupConfiguration(String json) {
        asTestUser().updateAutoCleanupConfiguration(json);
        return "SecHub auto cleanup data has been changed";
    }

    public String fetchProjectJobInfoForUser(String projectId, int pageSize, int page) {
        TestSecHubJobInfoForUserListPage listPage = asTestUser().fetchUserJobInfoList(new FixedTestProject(projectId), pageSize, page);
        return TestJSONHelper.get().createJSON(listPage, true);
    }

    public String rotateEncryption(SecHubEncryptionData data) {
        return asTestUser().rotateEncryption(data);
    }

}
