// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.api.internal.ApiClientBuilder;
import com.mercedesbenz.sechub.api.internal.WorkaroundAdminApi;
import com.mercedesbenz.sechub.api.internal.gen.AdminApi;
import com.mercedesbenz.sechub.api.internal.gen.AnonymousApi;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetch;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetchConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdate;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdateConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectDetails;
import com.mercedesbenz.sechub.commons.core.FailableRunnable;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;

/**
 * The central java API entry to access SecHub
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubClient {

    private static JsonMapper mapper = JsonMapperFactory.createMapper();

    private String username;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private boolean trustAll;

    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();
    private ApiClient apiClient;
    private AnonymousApi anonymousApi;
    private AdminApi adminApi;

    private WorkaroundAdminApi workaroundAdminApi;

    public SecHubClient(URI serverUri, String username, String apiToken) {
        this(serverUri, username, apiToken, false);
    }

    public SecHubClient(URI serverUri, String username, String apiToken, boolean trustAll) {

        this.username = username;
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
        this.serverUri = serverUri;
        this.trustAll = trustAll;

        apiClient = new ApiClientBuilder().createApiClient(this, mapper);

        anonymousApi = new AnonymousApi(getApiClient());
        adminApi = new AdminApi(getApiClient());
        workaroundAdminApi = new WorkaroundAdminApi(getApiClient());
    }

    private ApiClient getApiClient() {
        return apiClient;
    }

    public String getUsername() {
        return username;
    }

    public String getSealedApiToken() {
        return apiTokenAccess.unseal(sealedApiToken);
    }

    public URI getServerUri() {
        return serverUri;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Create.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public void createSignup(UserSignup signUp) throws SecHubClientException {
        runOrFail(() -> anonymousApi.userSignup(signUp.getDelegate()), "User signup failed");
    }

    public void createProject(Project project) throws SecHubClientException {
        runOrFail(() -> adminApi.adminCreatesProject(project.getDelegate()), "Cannot create project:" + project.getName());
    }

    public UUID createExecutorConfiguration(ExecutorConfiguration config) throws SecHubClientException {
        return runOrFail(() -> workaroundAdminApi.adminCreatesExecutorConfiguration(config.getDelegate()), "Cannot accept open signups");
    }

    public void createExecutionProfile(String profileName, ExecutionProfileCreate profile) throws SecHubClientException {
        runOrFail(() -> adminApi.adminCreatesExecutionProfile(profileName, profile.getDelegate()), "Was not able to create profile:" + profileName);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Check........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public boolean checkIsServerAlive() throws SecHubClientException {
        try {
            anonymousApi.anonymousCheckAliveGet();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Fetch........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public List<OpenUserSignup> fetchAllOpenSignups() throws SecHubClientException {
        return runOrFail(() -> OpenUserSignup.fromDelegates(adminApi.adminListsOpenUserSignups()), "Cannot fetch open signups");
    }

    public List<String> fetchAllProjectIds() throws SecHubClientException {
        return runOrFail(() -> adminApi.adminListsAllProjects(), "Cannot fetch all project names");
    }

    public List<String> fetchAllUserIds() throws SecHubClientException {
        return runOrFail(() -> adminApi.adminListsAllUsers(), "Cannot fetch all user names");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Accept.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public void acceptOpenSignup(String signupUsername) throws SecHubClientException {
        runOrFail(() -> adminApi.adminAcceptsSignup(signupUsername), "Cannot accept open signups");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private void runOrFail(FailableRunnable<ApiException> failable, String failureMessage) throws SecHubClientException {
        try {
            failable.runOrFail();
        } catch (ApiException e) {
            throw createClientException(failureMessage, e);
        }
    }

    private SecHubClientException createClientException(String message, Exception cause) throws SecHubClientException {
        return new SecHubClientException(message + " - " + cause.getMessage(), cause);
    }

    private <T> T runOrFail(Callable<T> callable, String failureMessage) throws SecHubClientException {
        try {
            return callable.call();
        } catch (ApiException e) {
            throw createClientException(failureMessage, e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                RuntimeException re = (RuntimeException) e;
                throw re;
            }
            throw new IllegalStateException("Unhandled exception - should not happen", e);
        }
    }

    public void addExecutorToProfile(UUID uuidOfExecutorConfigToAdd, String profileId) throws SecHubClientException {
        if (uuidOfExecutorConfigToAdd == null) {
            throw new IllegalArgumentException("uuid may not be null!");
        }
        if (profileId == null) {
            throw new IllegalArgumentException("profileId may not be null!");
        }

        runOrFail(() -> {
            OpenApiExecutionProfileUpdate update = fetchProfileAsUpdateObject(profileId);

            OpenApiExecutionProfileUpdateConfigurationsInner newItem = new OpenApiExecutionProfileUpdateConfigurationsInner();
            newItem.setUuid(uuidOfExecutorConfigToAdd.toString());
            update.addConfigurationsItem(newItem);

            adminApi.adminUpdatesExecutionProfile(profileId, update);

        }, "Cannot add executor config: " + uuidOfExecutorConfigToAdd + " to profile:" + profileId);
    }

    public boolean isProjectExisting(String projectId) throws SecHubClientException {
        return runOrFail(() -> adminApi.adminListsAllProjects().contains(projectId),

                "Cannot check if project '" + projectId + "' exists!");
    }

    public void assignUserToProject(String userId, String projectId) throws SecHubClientException {
        runOrFail(() -> adminApi.adminAssignsUserToProject(projectId, userId),

                "Was not able to assign user '" + userId + "' to project '" + projectId + "'");

    }

    public void unassignUserFromProject(String userId, String projectId) throws SecHubClientException {
        runOrFail(() -> adminApi.adminUnassignsUserFromProject(projectId, userId),

                "Was not able to unassign user '" + userId + "' from project '" + projectId + "'");

    }

    public boolean isUserAssignedToProject(String userId, String projectId) throws SecHubClientException {
        return runOrFail(() -> {
            /* not very smart... but works : */
            OpenApiProjectDetails details = adminApi.adminShowsProjectDetails(projectId);
            List<String> userIds = details.getUsers();
            return userIds.contains(userId);
        }, "");
    }

    
    private OpenApiExecutionProfileUpdate fetchProfileAsUpdateObject(String profileId) throws ApiException {
        OpenApiExecutionProfileUpdate update = new OpenApiExecutionProfileUpdate();

        OpenApiExecutionProfileFetch fetched = adminApi.adminFetchesExecutionProfile(profileId);
        update.setDescription(fetched.getDescription());
        update.setEnabled(fetched.getEnabled());
        List<OpenApiExecutionProfileFetchConfigurationsInner> fetchedConfigurations = fetched.getConfigurations();

        for (OpenApiExecutionProfileFetchConfigurationsInner fetchedConfiguration : fetchedConfigurations) {
            /* we only need the uuid on server side - everything else is ignored */
            String uuid = fetchedConfiguration.getUuid();

            /* add to update again */
            OpenApiExecutionProfileUpdateConfigurationsInner existingItem = new OpenApiExecutionProfileUpdateConfigurationsInner();
            existingItem.setUuid(uuid);
            update.addConfigurationsItem(existingItem);
        }
        return update;
    }

   
}
