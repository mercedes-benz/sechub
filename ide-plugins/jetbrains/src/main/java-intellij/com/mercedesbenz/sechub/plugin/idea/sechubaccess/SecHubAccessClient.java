// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;

class SecHubAccessClient implements SecHubAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubAccessClient.class);
    private SecHubClient client;

    public SecHubAccessClient(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {
        initSecHubClient(secHubServerUrl, userId, apiToken, trustAllCertificates);
    }

    @Override
    public boolean isSecHubServerAlive() {
        try {
            return client.isServerAlive();
        } catch (ApiException e) {
            LOG.debug("Failed to check SecHub server status", e);
            return false;
        }
    }

    @Override
    public List<ProjectData> getSecHubProjects() {
        try {
            return client.withProjectAdministrationApi().getAssignedProjectDataList();
        } catch (ApiException e) {
            LOG.error("Failed to retrieve SecHub reports", e);
            throw new RuntimeException("Failed to retrieve SecHub reports", e);
        }
    }

    @Override
    public SecHubJobInfoForUserListPage getSecHubJobPage(String projectId, int size, int page) {
        requireNonNull(projectId, "Parameter 'projectId' must not be null");
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than 0");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' must not be negative");
        }
        try {
            return client.withOtherApi().userListsJobsForProject(projectId, String.valueOf(size), String.valueOf(page), false, null);
        } catch (ApiException e) {
            LOG.error("Failed to retrieve SecHub jobs for project: {}", projectId, e);
            throw new RuntimeException("Failed to retrieve SecHub jobs for project: " + projectId, e);
        }
    }

    @Override
    public SecHubReport getSecHubReport(String projectId, UUID jobUUID) {
        requireNonNull(projectId, "Parameter 'projectId' must not be null");
        requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");
        try {
            return client.withSecHubExecutionApi().userDownloadJobReport(projectId, jobUUID);
        } catch (ApiException e) {
            LOG.error("Failed to retrieve SecHub report for project: {}, job UUID: {}", projectId, jobUUID, e);
            throw new RuntimeException("Failed to retrieve SecHub report for project: " + projectId + ", job UUID: " + jobUUID, e);
        }
    }

    private void initSecHubClient(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {

        requireNonNull(secHubServerUrl, "Parameter 'secHubServerUrl' must not be null");
        requireNonNull(userId, "Parameter 'userId' must not be null");
        requireNonNull(apiToken, "Parameter 'apiToken' must not be null");

        URI serverUri;
        try {
            serverUri = URI.create(secHubServerUrl);
        } catch (IllegalArgumentException e) {
            LOG.error("Parameter 'secHubServerUrl' must contain a valid secHub server URL", e);
            throw new IllegalStateException("Invalid parameter 'secHubServerUrl': %s".formatted(secHubServerUrl), e);
        }

        if (serverUri.getScheme() == null || serverUri.getHost() == null) {
            LOG.error("Parameter 'secHubServerUrl' must contain a valid secHub server URL with scheme and host");
            throw new IllegalStateException("Invalid parameter 'secHubServerUrl': %s".formatted(secHubServerUrl));
        }

        /* @formatter:off */
        this.client = DefaultSecHubClient.builder()
                .server(serverUri)
                .user(userId)
                .apiToken(apiToken)
                .trustAll(trustAllCertificates)
                .build();
        /* @formatter:on */
    }
}
