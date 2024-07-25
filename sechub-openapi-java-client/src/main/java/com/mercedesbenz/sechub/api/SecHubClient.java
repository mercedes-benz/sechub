// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * The central java API entry to access SecHub
 *
 * @author Albert Tregnaghi
 *
 */
public interface SecHubClient {

    /**
     * Adds an listener to the client. For some action on client side the listener
     * will be informed. A listener can be added only one time no matter how many
     * times this method is called.
     *
     * @param listener
     */
    void addListener(SecHubClientListener listener);

    /**
     * Removes a listener from the client (if added).
     *
     * @param listener
     */
    void removeListener(SecHubClientListener listener);

    void setUserId(String userId);

    String getUserId();

    void setApiToken(String apiToken);

    String getSealedApiToken();

    URI getServerUri();

    boolean isTrustAll();

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Create.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    void createSignup(UserSignup signUp) throws SecHubClientException;

    void createProject(Project project) throws SecHubClientException;

    UUID createExecutorConfiguration(ExecutorConfiguration config) throws SecHubClientException;

    void createExecutionProfile(String profileName, ExecutionProfileCreate profile) throws SecHubClientException;

    /**
     * Uploads data as defined in given configuration
     *
     * @param projectId        the project id
     * @param jobUUID          SecHub Job UUID
     * @param configuration    SecHub Job configuration (contains information about
     *                         upload behavior (e.g. paths etc.)
     * @param workingDirectory directory where the relative paths inside
     *                         configuration model shall start from
     * @throws SecHubClientException
     */
    void upload(String projectId, UUID jobUUID, SecHubConfigurationModel configuration, Path workingDirectory) throws SecHubClientException;

    List<ExecutorConfigurationInfo> fetchAllExecutorConfigurationInfo() throws SecHubClientException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Status........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    SecHubStatus fetchSecHubStatus() throws SecHubClientException;

    void triggerRefreshOfSecHubSchedulerStatus() throws SecHubClientException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Check........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    boolean isServerAlive() throws SecHubClientException;

    boolean isProjectExisting(String projectId) throws SecHubClientException;

    boolean isUserAssignedToProject(String userId, String projectId) throws SecHubClientException;

    boolean isExecutionProfileExisting(String profileId) throws SecHubClientException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Fetch........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    List<OpenUserSignup> fetchAllOpenSignups() throws SecHubClientException;

    List<String> fetchAllProjectIds() throws SecHubClientException;

    List<String> fetchAllUserIds() throws SecHubClientException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Assign/Unassign................. + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    void acceptOpenSignup(String signupUserId) throws SecHubClientException;

    void assignUserToProject(String userId, String projectId) throws SecHubClientException;

    void unassignUserFromProject(String userId, String projectId) throws SecHubClientException;

    void addExecutorConfigurationToProfile(UUID uuidOfExecutorConfigToAdd, String profileId) throws SecHubClientException;

    void deleteProject(String projectId) throws SecHubClientException;

    void deleteExecutionProfile(String profileId) throws SecHubClientException;

    void deleteExecutorConfiguration(UUID executorUUID) throws SecHubClientException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Scheduling...................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    UUID createJob(SecHubConfigurationModel configuration) throws SecHubClientException;

    JobStatus fetchJobStatus(String projectId, UUID jobUUID) throws SecHubClientException;

    SecHubReport downloadSecHubReportAsJson(String projectId, UUID jobUUID) throws SecHubClientException;

    /**
     * Approve SecHub job for project. This will mark the job as ready to start
     * inside SecHub
     *
     * @param projectId
     * @param jobUUID
     * @throws SecHubClientException
     */
    void approveJob(String projectId, UUID jobUUID) throws SecHubClientException;

    /**
     * Resolve SecHub server version
     *
     * @return server version as string
     * @throws SecHubClientException
     */
    String getServerVersion() throws SecHubClientException;

    /**
     * Request a new API token for given email address
     *
     * @param emailAddress
     * @throws SecHubClientException
     */
    void requestNewApiToken(String emailAddress) throws SecHubClientException;

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Other........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    /**
     * Downloads the full scan log for a given sechub job uuid into wanted target
     * location. This call can only be done an administrator.
     *
     * @param sechubJobUUID
     * @param downloadFilePath path to download file. If path is a folder the
     *                         filename will be
     *                         "SecHub-${sechubJobUUID}-scanlog.zip". When null, a
     *                         temp folder will be used
     * @return path to download
     */
    Path downloadFullScanLog(UUID sechubJobUUID, Path downloadFilePath) throws SecHubClientException;

}
