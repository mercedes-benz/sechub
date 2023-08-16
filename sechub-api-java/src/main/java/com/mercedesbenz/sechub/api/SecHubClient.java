// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import static java.util.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.api.internal.ApiClientBuilder;
import com.mercedesbenz.sechub.api.internal.OpenApiSecHubClientConversionHelper;
import com.mercedesbenz.sechub.api.internal.WorkaroundAdminApi;
import com.mercedesbenz.sechub.api.internal.WorkaroundProjectApi;
import com.mercedesbenz.sechub.api.internal.gen.AdminApi;
import com.mercedesbenz.sechub.api.internal.gen.AnonymousApi;
import com.mercedesbenz.sechub.api.internal.gen.ProjectApi;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetch;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdate;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdateConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiJobId;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiJobStatus;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfExecutorConfigurations;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfExecutorConfigurationsExecutorConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectDetails;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiScanJob;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiStatusInformationInner;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchivesCreationResult;
import com.mercedesbenz.sechub.commons.core.RunOrFail;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

/**
 * The central java API entry to access SecHub
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubClient {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubClient.class);

    private static JsonMapper mapper = JsonMapperFactory.createMapper();

    private String username;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private boolean trustAll;

    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();
    private ArchiveSupport archiveSupport = new ArchiveSupport();
    private CheckSumSupport checkSumSupport = new CheckSumSupport();

    private ApiClient apiClient;
    private AnonymousApi anonymousApi;
    private AdminApi adminApi;
    private ProjectApi projectApi;

    private WorkaroundAdminApi workaroundAdminApi;

    private OpenApiSecHubClientConversionHelper conversionHelper;

    private WorkaroundProjectApi workaroundProjectApi;

    private List<SecHubClientListener> secHubClientListeners;

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

        projectApi = new ProjectApi(getApiClient());
        workaroundProjectApi = new WorkaroundProjectApi(getApiClient());

        conversionHelper = new OpenApiSecHubClientConversionHelper(adminApi);
        secHubClientListeners = new LinkedList<>();
    }

    /**
     * Adds an listener to the client. For some action on client side the listener
     * will be informed. A listener can be added only one time no matter how many
     * times this method is called.
     *
     * @param listener
     */
    public void addListener(SecHubClientListener listener) {
        if (secHubClientListeners.contains(listener)) {
            /* already added - ignore */
            return;
        }
        this.secHubClientListeners.add(listener);
    }

    /**
     * Removes a listener from the client (if added).
     *
     * @param listener
     */
    public void removeListener(SecHubClientListener listener) {
        this.secHubClientListeners.remove(listener);
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
        requireNonNull(signUp, "signUp may not be null!");

        runOrFail(() -> anonymousApi.userSignup(signUp.getDelegate()), "User signup failed");
    }

    public void createProject(Project project) throws SecHubClientException {
        requireNonNull(project, "project may not be null!");

        runOrFail(() -> adminApi.adminCreatesProject(project.getDelegate()), "Cannot create project:" + project.getName());
    }

    public UUID createExecutorConfiguration(ExecutorConfiguration config) throws SecHubClientException {
        requireNonNull(config, "config may not be null!");

        return runOrFail(() -> {
            OpenApiExecutorConfiguration delegate = config.getDelegate();
            OpenApiExecutorConfigurationSetup setup = delegate.getSetup();
            /*
             * necessary because two different lists - delegate has its own, we overwrite
             * here
             */
            setup.setJobParameters(ExecutorConfigurationSetupJobParameter.toDelegates(config.getSetup().getJobParameters()));
            UUID result = workaroundAdminApi.adminCreatesExecutorConfiguration(delegate);
            return result;
        }, "Cannot create executor configuration");
    }

    public void createExecutionProfile(String profileName, ExecutionProfileCreate profile) throws SecHubClientException {
        requireNonNull(profileName, "profileName may not be null!");
        requireNonNull(profile, "profile may not be null!");

        runOrFail(() -> adminApi.adminCreatesExecutionProfile(profileName, profile.getDelegate()), "Was not able to create profile:" + profileName);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Upload.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

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
    public void upload(String projectId, UUID jobUUID, SecHubConfigurationModel configuration, Path workingDirectory) throws SecHubClientException {
        requireNonNull(projectId, "projectId may not be null!");
        requireNonNull(configuration, "configuration may not be null!");

        Path uploadDirectory = runOrFail(() -> Files.createTempDirectory("sechub_client_upload"), "Temp directory creation was not possible");
        ArchivesCreationResult createArchiveResult = runOrFail(() -> archiveSupport.createArchives(configuration, workingDirectory, uploadDirectory),
                "Cannot create archives!");

        inform((listener) -> listener.beforeUpload(jobUUID, configuration, createArchiveResult));

        try {
            uploadSources(projectId, jobUUID, createArchiveResult);
            uploadBinaries(projectId, jobUUID, createArchiveResult);

            inform((listener) -> listener.afterUpload(jobUUID, configuration, createArchiveResult));

        } finally {
            LOG.debug("Remove temporary data from: {}", uploadDirectory);
            try {
                archiveSupport.deleteArchives(createArchiveResult);
            } catch (IOException e) {
                throw new IllegalStateException("Was not able to remove temporary archive data!", e);
            }
        }
    }

    private void uploadBinaries(String projectId, UUID jobUUID, ArchivesCreationResult createArchiveResult) throws SecHubClientException {
        if (!createArchiveResult.isBinaryArchiveCreated()) {
            return;
        }
        Path tarFile = createArchiveResult.getBinaryArchiveFile();

        String filesize = String.valueOf(tarFile.toFile().length());
        String checksum = checkSumSupport.createSha256Checksum(tarFile);

        runOrFail(() -> workaroundProjectApi.userUploadsBinaries(projectId, jobUUID.toString(), checksum, filesize, tarFile), "Binary upload (tar)");
    }

    private void uploadSources(String projectId, UUID jobUUID, ArchivesCreationResult createArchiveResult) throws SecHubClientException {
        if (!createArchiveResult.isSourceArchiveCreated()) {
            return;
        }
        Path zipFile = createArchiveResult.getSourceArchiveFile();
        String checksum = checkSumSupport.createSha256Checksum(zipFile);

        runOrFail(() -> workaroundProjectApi.userUploadsSourceCode(projectId, jobUUID.toString(), checksum, zipFile), "Source upload (zip)");
    }

    public List<ExecutorConfigurationInfo> fetchAllExecutorConfigurationInfo() throws SecHubClientException {
        OpenApiListOfExecutorConfigurations configList = runOrFail(() -> adminApi.adminFetchesExecutorConfigurationList(), "Fetch executor configurations");

        List<OpenApiListOfExecutorConfigurationsExecutorConfigurationsInner> list = configList.getExecutorConfigurations();
        List<ExecutorConfigurationInfo> result = new ArrayList<>();

        for (OpenApiListOfExecutorConfigurationsExecutorConfigurationsInner inner : list) {

            ExecutorConfigurationInfo info = new ExecutorConfigurationInfo();
            info.setEnabled(inner.getEnabled() == null ? false : inner.getEnabled());
            info.setName(inner.getName());
            info.setUuid(UUID.fromString(inner.getUuid()));

            result.add(info);
        }
        return result;
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Status........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public SecHubStatus fetchSecHubStatus() throws SecHubClientException {
        SecHubStatus status = new SecHubStatus();
        runOrFail(() -> {
            List<OpenApiStatusInformationInner> statusInformationList = adminApi.adminListsStatusInformation();
            for (OpenApiStatusInformationInner info : statusInformationList) {
                String key = info.getKey();
                if (key != null) {
                    status.statusInformation.put(key, info.getValue());
                }
            }
        }, "Was not able to fetch SecHub status!");
        return status;
    }

    public void triggerRefreshOfSecHubSchedulerStatus() throws SecHubClientException {
        runOrFail(() -> adminApi.adminTriggersRefreshOfSchedulerStatus(), "Was not able to trigger scheduler refresh");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Check........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public boolean isServerAlive() throws SecHubClientException {
        try {
            anonymousApi.anonymousCheckAliveGet();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    public boolean isProjectExisting(String projectId) throws SecHubClientException {
        requireNonNull(projectId, "projectId may not be null!");
        return runOrFail(() -> adminApi.adminListsAllProjects().contains(projectId),

                "Cannot check if project '" + projectId + "' exists!");
    }

    public boolean isUserAssignedToProject(String userId, String projectId) throws SecHubClientException {
        requireNonNull(userId, "userId may not be null!");
        requireNonNull(projectId, "projectId may not be null!");

        return runOrFail(() -> {
            /* not very smart... but works : */
            OpenApiProjectDetails details = adminApi.adminShowsProjectDetails(projectId);
            List<String> userIds = details.getUsers();
            return userIds.contains(userId);
        }, "");
    }

    public boolean isExecutionProfileExisting(String profileId) throws SecHubClientException {
        try {
            OpenApiExecutionProfileFetch result = adminApi.adminFetchesExecutionProfile(profileId);
            return result != null;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                /* not found */
                return false;
            }
            throw new SecHubClientException("Was not able check if profile " + profileId + " does exist.", e);
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
    /* + ................Assign/Unassign................. + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public void acceptOpenSignup(String signupUsername) throws SecHubClientException {
        requireNonNull(signupUsername, "signupUsername may not be null!");

        runOrFail(() -> adminApi.adminAcceptsSignup(signupUsername), "Cannot accept open signups");
    }

    public void assignUserToProject(String userId, String projectId) throws SecHubClientException {
        requireNonNull(userId, "userId may not be null!");
        requireNonNull(projectId, "projectId may not be null!");

        runOrFail(() -> adminApi.adminAssignsUserToProject(projectId, userId),

                "Was not able to assign user '" + userId + "' to project '" + projectId + "'");

    }

    public void unassignUserFromProject(String userId, String projectId) throws SecHubClientException {
        requireNonNull(userId, "userId may not be null!");
        requireNonNull(projectId, "projectId may not be null!");

        runOrFail(() -> adminApi.adminUnassignsUserFromProject(projectId, userId),

                "Was not able to unassign user '" + userId + "' from project '" + projectId + "'");

    }

    public void addExecutorConfigurationToProfile(UUID uuidOfExecutorConfigToAdd, String profileId) throws SecHubClientException {
        requireNonNull(uuidOfExecutorConfigToAdd, "uuidOfExecutorConfigToAdd may not be null!");
        requireNonNull(profileId, "profileId may not be null!");

        runOrFail(() -> {
            OpenApiExecutionProfileUpdate update = conversionHelper.fetchProfileAndConvertToUpdateObject(profileId);

            OpenApiExecutionProfileUpdateConfigurationsInner newItem = new OpenApiExecutionProfileUpdateConfigurationsInner();
            newItem.setUuid(uuidOfExecutorConfigToAdd.toString());
            update.addConfigurationsItem(newItem);

            adminApi.adminUpdatesExecutionProfile(profileId, update);

        }, "Cannot add executor config: " + uuidOfExecutorConfigToAdd + " to profile:" + profileId);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Delete.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    public void deleteProject(String projectId) throws SecHubClientException {
        requireNonNull(projectId, "projectId may not be null!");

        runOrFail(() -> adminApi.adminDeleteProject(projectId), "Was not able to delete project: " + projectId);
    }

    public void deleteExecutionProfile(String profileId) throws SecHubClientException {
        requireNonNull(profileId, "profileId may not be null!");

        runOrFail(() -> adminApi.adminDeletesExecutionProfile(profileId), "Was not able to delete execution profile: " + profileId);
    }

    public void deleteExecutorConfiguration(UUID executorUUID) throws SecHubClientException {
        requireNonNull(executorUUID, "executor uuid may not be null!");

        runOrFail(() -> adminApi.adminDeletesExecutorConfiguration(executorUUID.toString()), "Was not able to delete executor configuration: " + executorUUID);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Scheduling...................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public UUID createJob(SecHubConfigurationModel configuration) throws SecHubClientException {
        requireNonNull(configuration, "configuration may not be null!");
        String projectId = configuration.getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("Project id missing inside configuration!");
        }

        String configAsJson = JSONConverter.get().toJSON(configuration, true);
        LOG.debug("configAsJson=\n{}", configAsJson);

        OpenApiScanJob openApiScanJob = JSONConverter.get().fromJSON(OpenApiScanJob.class, configAsJson);
        if (LOG.isDebugEnabled()) {
            String openApiJSON = JSONConverter.get().toJSON(openApiScanJob, true);
            LOG.debug("openApiJSON=\n{}", openApiJSON);
        }
        OpenApiJobId openApiJobId = runOrFail(() -> projectApi.userCreatesNewJob(projectId, openApiScanJob),
                "Was not able to create a SecHub job for project:" + projectId);
        String jobIdAsString = openApiJobId.getJobId();

        UUID uuid = UUID.fromString(jobIdAsString);
        return uuid;
    }

    public JobStatus fetchJobStatus(String projectId, UUID jobUUID) throws SecHubClientException {
        OpenApiJobStatus status = runOrFail(() -> projectApi.userChecksJobStatus(projectId, jobUUID.toString()), "Fetch status");
        return JobStatus.from(status);
    }

    public SecHubReport downloadSecHubReportAsJson(String projectId, UUID jobUUID) throws SecHubClientException {
        SecHubReport report = runOrFail(() -> workaroundProjectApi.userDownloadsJobReport(projectId, jobUUID.toString()), "Download SecHub report (JSON)");
        inform((listener) -> listener.afterReportDownload(jobUUID, report));

        return report;
    }

    /**
     * Approve SecHub job for project. This will mark the job as ready to start
     * inside SecHub
     *
     * @param projectId
     * @param jobUUID
     * @throws SecHubClientException
     */
    public void approveJob(String projectId, UUID jobUUID) throws SecHubClientException {
        runOrFail(() -> projectApi.userApprovesJob(projectId, jobUUID.toString()), "Job approve");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private void runOrFail(RunOrFail<ApiException> failable, String failureMessage) throws SecHubClientException {
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

    private void inform(SecHubClientListenerCaller r) {
        for (SecHubClientListener listener : secHubClientListeners) {
            r.inform(listener);
        }
    }

    private interface SecHubClientListenerCaller {

        public void inform(SecHubClientListener listener);

    }
}
