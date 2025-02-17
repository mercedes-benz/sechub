// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.util;

import com.mercedesbenz.sechub.api.*;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * This is just a dummy class to make the plugin build work.
 * This client does not do anything.
 *
 *
 */
public class DoNothingSecHubClient implements SecHubClient {

    @Override
    public void addListener(SecHubClientListener listener) {

    }

    @Override
    public void removeListener(SecHubClientListener listener) {

    }

    @Override
    public void setUserId(String userId) {

    }

    @Override
    public String getUserId() {
        return "";
    }

    @Override
    public void setApiToken(String apiToken) {

    }

    @Override
    public String getSealedApiToken() {
        return "";
    }

    @Override
    public URI getServerUri() {
        return null;
    }

    @Override
    public boolean isTrustAll() {
        return false;
    }

    @Override
    public void createSignup(UserSignup signUp) throws SecHubClientException {

    }

    @Override
    public void createProject(Project project) throws SecHubClientException {

    }

    @Override
    public UUID createExecutorConfiguration(ExecutorConfiguration config) throws SecHubClientException {
        return null;
    }

    @Override
    public void createExecutionProfile(String profileName, ExecutionProfileCreate profile) throws SecHubClientException {

    }

    @Override
    public void upload(String projectId, UUID jobUUID, SecHubConfigurationModel configuration, Path workingDirectory) throws SecHubClientException {

    }

    @Override
    public List<ExecutorConfigurationInfo> fetchAllExecutorConfigurationInfo() throws SecHubClientException {
        return List.of();
    }

    @Override
    public SecHubStatus fetchSecHubStatus() throws SecHubClientException {
        return null;
    }

    @Override
    public void triggerRefreshOfSecHubSchedulerStatus() throws SecHubClientException {

    }

    @Override
    public boolean isServerAlive() throws SecHubClientException {
        return false;
    }

    @Override
    public boolean isProjectExisting(String projectId) throws SecHubClientException {
        return false;
    }

    @Override
    public boolean isUserAssignedToProject(String userId, String projectId) throws SecHubClientException {
        return false;
    }

    @Override
    public boolean isExecutionProfileExisting(String profileId) throws SecHubClientException {
        return false;
    }

    @Override
    public List<OpenUserSignup> fetchAllOpenSignups() throws SecHubClientException {
        return List.of();
    }

    @Override
    public List<String> fetchAllProjectIds() throws SecHubClientException {
        return List.of();
    }

    @Override
    public List<String> fetchAllUserIds() throws SecHubClientException {
        return List.of();
    }

    @Override
    public void acceptOpenSignup(String signupUserId) throws SecHubClientException {

    }

    @Override
    public void assignUserToProject(String userId, String projectId) throws SecHubClientException {

    }

    @Override
    public void unassignUserFromProject(String userId, String projectId) throws SecHubClientException {

    }

    @Override
    public void addExecutorConfigurationToProfile(UUID uuidOfExecutorConfigToAdd, String profileId) throws SecHubClientException {

    }

    @Override
    public void deleteProject(String projectId) throws SecHubClientException {

    }

    @Override
    public void deleteExecutionProfile(String profileId) throws SecHubClientException {

    }

    @Override
    public void deleteExecutorConfiguration(UUID executorUUID) throws SecHubClientException {

    }

    @Override
    public UUID createJob(SecHubConfigurationModel configuration) throws SecHubClientException {
        return null;
    }

    @Override
    public JobStatus fetchJobStatus(String projectId, UUID jobUUID) throws SecHubClientException {
        return null;
    }

    @Override
    public SecHubReport downloadSecHubReportAsJson(String projectId, UUID jobUUID) throws SecHubClientException {
        return null;
    }

    @Override
    public void approveJob(String projectId, UUID jobUUID) throws SecHubClientException {

    }

    @Override
    public String getServerVersion() throws SecHubClientException {
        return "";
    }

    @Override
    public void requestNewApiToken(String emailAddress) throws SecHubClientException {

    }

    @Override
    public Path downloadFullScanLog(UUID sechubJobUUID, Path downloadFilePath) throws SecHubClientException {
        return null;
    }
}
