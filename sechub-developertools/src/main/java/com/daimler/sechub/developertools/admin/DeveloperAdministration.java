// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import com.daimler.sechub.integrationtest.api.UserContext;
import com.daimler.sechub.integrationtest.internal.TestJSONHelper;
import com.daimler.sechub.integrationtest.internal.TestRestHelper;
import com.daimler.sechub.test.TestURLBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class DeveloperAdministration {

	private ConfigProvider provider;
	private AdminUserContext userContext;
	private TestRestHelper restHelper;
	private TestURLBuilder urlBuilder;

	public DeveloperAdministration(ConfigProvider provider) {
		this.provider = provider;
		this.userContext = new AdminUserContext();
		this.restHelper = new TestRestHelper(userContext) {
			@Override
			protected ResponseErrorHandler createErrorHandler() {
				return new DefaultResponseErrorHandler() {

					@Override
					public void handleError(ClientHttpResponse response) throws IOException {
						StringBuilder sb = new StringBuilder();
						String statusText = response.getStatusText();
						sb.append("status code::");
						sb.append(response.getStatusCode());
						if (statusText != null) {
							sb.append(", text:");
							sb.append(statusText);
						}
						try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"))) {
							String line = null;
							sb.append(",body:");
							while ((line = br.readLine()) != null) {
								sb.append("\n");
								sb.append(line);
							}
						} catch (IOException e) {
							provider.handleClientError("failed to read response body:" + e.getMessage());
						}
						provider.handleClientError(sb.toString());

					}
				};
			}

		};
	}

	public TestURLBuilder getUrlBuilder() {
		if (urlBuilder == null) {
			int port = provider.getPort();
			String server = provider.getServer();
			urlBuilder = new TestURLBuilder("https", port, server);
		}
		return urlBuilder;
	}

	public String fetchSignups() {
		return getRestHelper().getJSon(getUrlBuilder().buildAdminListsUserSignupsUrl());
	}

	public TestRestHelper getRestHelper() {
		return restHelper;
	}

	public String doSignup(String string) {
		getRestHelper().post(getUrlBuilder().buildAdminAcceptsUserSignUpUrl(string));
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

		String json = "{\"apiVersion\":\"1.0\",\r\n" + "		\"userId\":\"" + name + "\",\r\n" + "		\"emailAdress\":\"" + email + "\"}";
		return getRestHelper().postJSon(getUrlBuilder().buildUserSignUpUrl(), json);
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

	public String createProject(String projectId, String description, String owner, List<String> whiteListURLs) {
		/* @formatter:off */
		StringBuilder json = new StringBuilder();
		if (description==null || description.isEmpty()) {
			description = "description for project "+projectId;
		}
		TestJSONHelper jsonHelper = TestJSONHelper.get();
		json.append("{\n" +
				" \"apiVersion\":\"1.0\",\n" +
				" \"name\":\""+projectId+"\",\n" +
				" \"owner\":\""+owner+"\",\n" +
				" \"description\":\""+description+"\"");
		if (! whiteListURLs.isEmpty()) {
			json.append(",\n \"whiteList\" : {\"uris\":[");

			for (Iterator<String> it = whiteListURLs.iterator();it.hasNext();) {
				String url = it.next();
				json.append("\""+url+"\"");
				if (it.hasNext()){
					json.append(",");
				}
			}
			json.append("]\n");
			json.append("                 }\n");
		}

		json.append("}\n");
		jsonHelper.assertValidJson(json.toString());
		/* @formatter:on */
		return getRestHelper().postJSon(getUrlBuilder().buildAdminCreatesProjectUrl(), json.toString());
	}

	public String fetchProjectList() {
		return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminListsProjectsUrl());
	}

	public String fetchProjectInfo(String projectId) {
		return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminShowsProjectDetailsUrl(projectId));
	}

	public String fetchUserInfo(String userId) {
		return getRestHelper().getStringFromURL(getUrlBuilder().buildAdminShowsUserDetailsUrl(userId));
	}

	public List<String> fetchProjectWhiteList(String projectId) {
		List<String> result = new ArrayList<>();
		String json = getRestHelper().getJSon(getUrlBuilder().buildAdminFetchProjectInfoUrl(projectId));
		TestJSONHelper jsonHelper = TestJSONHelper.get();
		JsonNode jsonNode = jsonHelper.readTree(json);
		JsonNode whitelist = jsonNode.get("whiteList");
		if (whitelist instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) whitelist;
			for (JsonNode node : arrayNode) {
				String uriText = node.textValue();
				result.add(uriText);
			}

		}

		return result;
	}

	public String fetchProjectScanLogs(String projectId) {
		String json = getRestHelper().getJSon(getUrlBuilder().buildAdminFetchesScanLogsForProject(projectId));
		return json;
	}

	public String fetchJSONReport(String projectId, UUID sechubJobUUID) {
		String json = getRestHelper().getJSon(getUrlBuilder().buildFetchReport(projectId, sechubJobUUID));
		return json;
	}

	public String fetchJobStatus(String projectId, String jobUUID) {
		return getRestHelper().getStringFromURL(getUrlBuilder().buildFetchJobStatus(projectId, jobUUID));
	}

	public void updateProjectWhiteList(String projectId, List<String> result) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"apiVersion\":\"1.0\", \"whiteList\":{\"uris\":[");
		for (Iterator<String> it = result.iterator(); it.hasNext();) {
			sb.append("\"");
			sb.append(it.next());
			sb.append("\"");
			if (it.hasNext()) {
				sb.append(",");
			}

		}
		sb.append("]}}");

		getRestHelper().postJSon(getUrlBuilder().buildUpdateProjectWhiteListUrl(projectId), sb.toString());
	}

	public String assignUserToProject(String userId, String projectId) {
		getRestHelper().post(getUrlBuilder().buildAdminAssignsUserToProjectUrl(userId, projectId));
		return "assigned " + userId + " to project " + projectId;
	}

	public String unassignUserFromProject(String userId, String projectId) {
		getRestHelper().delete(getUrlBuilder().buildAdminUnassignsUserFromProjectUrl(userId, projectId));
		return "unassigned " + userId + " to project " + projectId;
	}

	public String deleteProject(String projectId) {
		getRestHelper().delete(getUrlBuilder().buildAdminDeletesProject(projectId));
		return "sent";
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
		return getRestHelper().getJSon(getUrlBuilder().buildAdminListsStatusEntries());
	}

	public String triggerDownloadFullScan(UUID sechubJobUUID) {

		String url = getUrlBuilder().buildAdminDownloadsZipFileContainingFullScanDataFor(sechubJobUUID);
		return commonTriggerDownloadInBrowser(url);
	}

	public String triggerDownloadReport(String projectId, UUID sechubJobUUID) {
		String url = getUrlBuilder().buildFetchReport(projectId, sechubJobUUID);
		return commonTriggerDownloadInBrowser(url);

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







}
