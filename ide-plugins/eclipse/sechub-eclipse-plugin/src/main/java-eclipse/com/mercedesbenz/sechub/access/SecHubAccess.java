// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.access;

import static org.hamcrest.Matchers.allOf;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;

public class SecHubAccess {

	private static final Logger LOG = LoggerFactory.getLogger(SecHubAccess.class);

	private SecHubClient client;

	public SecHubAccess(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {
		initSecHubClient(secHubServerUrl, userId, apiToken, trustAllCertificates);
	}
	
	public class ServerAccessStatus{
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
		}else {
			try {
				accessData.alive=client.isServerAlive(); // alive check currently needs credentials
				
			} catch (Exception e) {
				accessData.alive=false;
			}
		}
		// TODO 2024-09-12 de-jcup: When client supports fetching of accessible project ids, add this to the userProjectIds + set login failure if not possible 
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

	public List<ProjectData> fetchProjectList() {
		try {
			return client.withProjectAdministrationApi().getAssignedProjectDataList();
		} catch (ApiException e) {
			LOG.error("Was not abl to retrieve project list");
			return List.of();
		}
	}

	public SecHubJobInfoForUserListPage fetchJobInfoListOrNull(String projectId, int size, int page ) {
		try {
			Map<String, String> map = Map.of();
			return client.withOtherApi().userListsJobsForProject(projectId, String.valueOf(size), String.valueOf(page), false, map);
		} catch (ApiException e) {
			LOG.error("Was not abl to retrieve job list");
			return null;
		}
		
	}
}
