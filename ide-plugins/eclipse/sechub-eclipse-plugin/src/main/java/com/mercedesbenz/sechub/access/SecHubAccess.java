// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.access;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveJobData;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositives;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;

public class SecHubAccess {

	private static final Logger LOG = LoggerFactory.getLogger(SecHubAccess.class);

	private SecHubClient client;

	public SecHubAccess(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {
		initSecHubClient(secHubServerUrl, userId, apiToken, trustAllCertificates);
	}

	public class ServerAccessStatus {
		private boolean alive;
		private boolean loginFailure;
		private SortedSet<String> userProjectIds = new TreeSet<String>();

		public boolean isAlive() {
			return alive;
		}

		public boolean isLoginFaiure() {
			return loginFailure;
		}

		public Set<String> getUserProjectIds() {
			return Collections.unmodifiableSortedSet(userProjectIds);
		}
	}

	public ServerAccessStatus fetchServerAccessStatus() {
		ServerAccessStatus accessData = new ServerAccessStatus();
		if (client == null) {
			LOG.debug("SecHub client is not initialized");
		} else {
			try {
				accessData.alive = client.isServerAlive(); // alive check currently needs credentials

			} catch (Exception e) {
				accessData.alive = false;
			}
		}
		return accessData;
	}

	private void initSecHubClient(String secHubServerUrl, String userId, String apiToken,
			boolean trustAllCertificates) {

		if (isInputMissingOrEmpty(secHubServerUrl, userId, apiToken)) {
			return;
		}
		try {
			URI serverUri = URI.create(secHubServerUrl);

			/* @formatter:off */
            this.client = DefaultSecHubClient.builder()
                    .server(serverUri)
                    .user(userId)
                    .apiToken(apiToken)
                    .trustAll(trustAllCertificates)
                    .build();
            /* @formatter:on */

		} catch (IllegalArgumentException e) {
			LOG.error("Failed to initialize SecHub client", e);
		}
	}

	private boolean isInputMissingOrEmpty(String secHubServerUrl, String userId, String apiToken) {
		return secHubServerUrl.isBlank() || userId == null || apiToken == null;
	}

	public List<ProjectData> fetchProjectList() throws ApiException {
		return client.withProjectAdministrationApi().getAssignedProjectDataList();
	}

	public SecHubJobInfoForUserListPage fetchJobInfoList(String projectId, int size, int page) throws ApiException {
		Map<String, String> map = Map.of();
		return client.withOtherApi().userListsJobsForProject(projectId, String.valueOf(size), String.valueOf(page),
				false, map);

	}

	public SecHubReport downloadJobReport(String projectId, UUID jobUUID) throws ApiException {
		return client.withSecHubExecutionApi().userDownloadJobReport(projectId, jobUUID);
	}

	public FalsePositiveProjectConfiguration fetchFalsePositiveProjectData(String projectId) throws ApiException {
		return client.withSecHubExecutionApi().userFetchFalsePositiveConfigurationOfProject(projectId);
	}

	public void markJobFalsePositives(String projectId, UUID jobUUID, String comment, List<Integer> findingIds) throws ApiException {
		FalsePositives falsePositives= new FalsePositives();
		falsePositives.setApiVersion("1.0");
		
		for (Integer findingId: findingIds) {
			FalsePositiveJobData dataItem = new FalsePositiveJobData();
			dataItem.setFindingId(findingId);
			dataItem.setComment(comment);
			dataItem.setJobUUID(jobUUID);
			falsePositives.addJobDataItem(dataItem);
		}
		
		client.withSecHubExecutionApi().userMarkFalsePositives(projectId, falsePositives);
		
	}

	public void unmarkJobFalsePositives(String projectId, UUID jobUUID, List<Integer> list) throws ApiException {
		for (Integer findingId: list) {
			client.withSecHubExecutionApi().userUnmarksJobFalsePositives(projectId, ""+jobUUID, ""+findingId);
		}
		
	}
}
