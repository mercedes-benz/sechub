// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import static java.util.Objects.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public class DefaultSecHubClient extends AbstractSecHubClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSecHubClient.class);

    private static JsonMapper mapper = JsonMapperFactory.createMapper();

    private ArchiveSupport archiveSupport = new ArchiveSupport();
    private CheckSumSupport checkSumSupport = new CheckSumSupport();

    private ApiClient apiClient;
    private AnonymousApi anonymousApi;
    private AdminApi adminApi;
    private ProjectApi projectApi;

    private WorkaroundAdminApi workaroundAdminApi;

    private OpenApiSecHubClientConversionHelper conversionHelper;

    private WorkaroundProjectApi workaroundProjectApi;
    
    private SecHubStatusFactory sechubStatusFactory = new SecHubStatusFactory();

    public static DefaultSecHubClientBuilder builder() {
        return new DefaultSecHubClientBuilder();
    }

    private DefaultSecHubClient(URI serverUri, String userId, String apiToken, boolean trustAll) {
        super(serverUri, userId, apiToken, trustAll);

        apiClient = new ApiClientBuilder().createApiClient(this, mapper);

        anonymousApi = new AnonymousApi(getApiClient());

        adminApi = new AdminApi(getApiClient());
        workaroundAdminApi = new WorkaroundAdminApi(getApiClient());

        projectApi = new ProjectApi(getApiClient());
        workaroundProjectApi = new WorkaroundProjectApi(getApiClient());

        conversionHelper = new OpenApiSecHubClientConversionHelper();

    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Create.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @Override
    public void createSignup(UserSignup signUp) throws SecHubClientException {
        requireNonNull(signUp, "signUp may not be null!");

        runOrFail(() -> anonymousApi.userSignup(signUp.getDelegate()), "User signup failed");
    }

    @Override
    public void createProject(Project project) throws SecHubClientException {
        requireNonNull(project, "project may not be null!");

        runOrFail(() -> adminApi.adminCreatesProject(project.getDelegate()), "Cannot create project:" + project.getName());
    }

    @Override
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

    @Override
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
    @Override
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

    @Override
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
    @Override
    public SecHubStatus fetchSecHubStatus() throws SecHubClientException {

        Map<String, String> statusInformation = new TreeMap<>();

        runOrFail(() -> {
            List<OpenApiStatusInformationInner> statusInformationList = adminApi.adminListsStatusInformation();
            for (OpenApiStatusInformationInner info : statusInformationList) {
                String key = info.getKey();
                if (key != null) {
                    statusInformation.put(key, info.getValue());
                }
            }
        }, "Was not able to fetch SecHub status!");
        
        SecHubStatus status = sechubStatusFactory.createFromMap(statusInformation);
        return status;
    }

    @Override
    public void triggerRefreshOfSecHubSchedulerStatus() throws SecHubClientException {
        runOrFail(() -> adminApi.adminTriggersRefreshOfSchedulerStatus(), "Was not able to trigger scheduler refresh");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Check........................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @Override
    public boolean isServerAlive() throws SecHubClientException {
        try {
            anonymousApi.anonymousCheckAliveHead();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public boolean isProjectExisting(String projectId) throws SecHubClientException {
        requireNonNull(projectId, "projectId may not be null!");
        return runOrFail(() -> adminApi.adminListsAllProjects().contains(projectId),

                "Cannot check if project '" + projectId + "' exists!");
    }

    @Override
    public boolean isUserAssignedToProject(String userId, String projectId) throws SecHubClientException {
        requireNonNull(userId, "userId may not be null!");
        requireNonNull(projectId, "projectId may not be null!");

        return runOrFail(() -> {
            /* not very smart... but works : */
            OpenApiProjectDetails details = adminApi.adminShowsProjectDetails(projectId);
            List<String> userIds = details.getUsers();
            return userIds.contains(userId);
        }, "Cannot check if user '" + userId + "' is assigned to project '" + projectId + "'");
    }

    @Override
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
    @Override
    public List<OpenUserSignup> fetchAllOpenSignups() throws SecHubClientException {
        return runOrFail(() -> OpenUserSignup.fromDelegates(adminApi.adminListsOpenUserSignups()), "Cannot fetch open signups");
    }

    @Override
    public List<String> fetchAllProjectIds() throws SecHubClientException {
        return runOrFail(() -> adminApi.adminListsAllProjects(), "Cannot fetch all project names");
    }

    @Override
    public List<String> fetchAllUserIds() throws SecHubClientException {
        return runOrFail(() -> adminApi.adminListsAllUsers(), "Cannot fetch all user names");
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Assign/Unassign................. + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @Override
    public void acceptOpenSignup(String signupUsername) throws SecHubClientException {
        requireNonNull(signupUsername, "signupUsername may not be null!");

        runOrFail(() -> adminApi.adminAcceptsSignup(signupUsername), "Cannot accept open signups");
    }

    @Override
    public void assignUserToProject(String userId, String projectId) throws SecHubClientException {
        requireNonNull(userId, "userId may not be null!");
        requireNonNull(projectId, "projectId may not be null!");

        runOrFail(() -> adminApi.adminAssignsUserToProject(projectId, userId),

                "Was not able to assign user '" + userId + "' to project '" + projectId + "'");

    }

    @Override
    public void unassignUserFromProject(String userId, String projectId) throws SecHubClientException {
        requireNonNull(userId, "userId may not be null!");
        requireNonNull(projectId, "projectId may not be null!");

        runOrFail(() -> adminApi.adminUnassignsUserFromProject(projectId, userId),

                "Was not able to unassign user '" + userId + "' from project '" + projectId + "'");

    }

    @Override
    public void addExecutorConfigurationToProfile(UUID uuidOfExecutorConfigToAdd, String profileId) throws SecHubClientException {
        requireNonNull(uuidOfExecutorConfigToAdd, "uuidOfExecutorConfigToAdd may not be null!");
        requireNonNull(profileId, "profileId may not be null!");

        runOrFail(() -> {
            OpenApiExecutionProfileUpdate update = conversionHelper.fetchProfileAndConvertToUpdateObject(profileId, adminApi);

            OpenApiExecutionProfileUpdateConfigurationsInner newItem = new OpenApiExecutionProfileUpdateConfigurationsInner();
            newItem.setUuid(uuidOfExecutorConfigToAdd.toString());
            update.addConfigurationsItem(newItem);

            adminApi.adminUpdatesExecutionProfile(profileId, update);

        }, "Cannot add executor config: " + uuidOfExecutorConfigToAdd + " to profile:" + profileId);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Delete.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    @Override
    public void deleteProject(String projectId) throws SecHubClientException {
        requireNonNull(projectId, "projectId may not be null!");

        runOrFail(() -> adminApi.adminDeleteProject(projectId), "Was not able to delete project: " + projectId);
    }

    @Override
    public void deleteExecutionProfile(String profileId) throws SecHubClientException {
        requireNonNull(profileId, "profileId may not be null!");

        runOrFail(() -> adminApi.adminDeletesExecutionProfile(profileId), "Was not able to delete execution profile: " + profileId);
    }

    @Override
    public void deleteExecutorConfiguration(UUID executorUUID) throws SecHubClientException {
        requireNonNull(executorUUID, "executor uuid may not be null!");

        runOrFail(() -> adminApi.adminDeletesExecutorConfiguration(executorUUID.toString()), "Was not able to delete executor configuration: " + executorUUID);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Scheduling...................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @Override
    public UUID createJob(SecHubConfigurationModel configuration) throws SecHubClientException {
        requireNonNull(configuration, "configuration may not be null!");
        String projectId = configuration.getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("Project id missing inside configuration!");
        }

        OpenApiScanJob openApiScanJob = conversionHelper.convertToOpenApiScanJob(configuration);
        OpenApiJobId openApiJobId = runOrFail(() -> projectApi.userCreatesNewJob(projectId, openApiScanJob),
                "Was not able to create a SecHub job for project:" + projectId);
        String jobIdAsString = openApiJobId.getJobId();

        UUID uuid = UUID.fromString(jobIdAsString);
        return uuid;
    }

    @Override
    public JobStatus fetchJobStatus(String projectId, UUID jobUUID) throws SecHubClientException {
        OpenApiJobStatus status = runOrFail(() -> projectApi.userChecksJobStatus(projectId, jobUUID.toString()), "Fetch status");
        return JobStatus.from(status);
    }

    @Override
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
    @Override
    public void approveJob(String projectId, UUID jobUUID) throws SecHubClientException {
        runOrFail(() -> projectApi.userApprovesJob(projectId, jobUUID.toString()), "Job approve");
    }

    @Override
    public Path downloadFullScanLog(UUID sechubJobUUID, Path downloadFilePath) throws SecHubClientException {
        final File targetFile = calculateFullScanLogFile(sechubJobUUID, downloadFilePath);
        runOrFail(() -> workaroundAdminApi.adminDownloadsFullScanDataForJob(sechubJobUUID.toString(), targetFile), "Download full scan log");
        return targetFile.toPath();
    }

    /* +++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Version...................... + */
    /* +++++++++++++++++++++++++++++++++++++++++++++++++ */

    @Override
    public String getServerVersion() throws SecHubClientException {
        return runOrFail(() -> adminApi.adminChecksServerVersion().getServerVersion(), "Get server version");
    }

    @Override
    public void requestNewApiToken(String emailAddress) throws SecHubClientException {
        runOrFail(() -> anonymousApi.userRequestsNewApiToken(emailAddress), "User requests a new API Token");
    }

    private ApiClient getApiClient() {
        return apiClient;
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private void runOrFail(RunOrFail<ApiException> failable, String failureMessage) throws SecHubClientException {
        try {
            failable.runOrFail();
        } catch (Exception e) {
            throw createClientException(failureMessage, e);
        }
    }

    private <T> T runOrFail(Callable<T> callable, String failureMessage) throws SecHubClientException {
        try {
            return callable.call();
        } catch (Exception e) {
            throw createClientException(failureMessage, e);
        }
    }

    private SecHubClientException createClientException(String message, Exception cause) throws SecHubClientException {
        return new SecHubClientException(message + " - " + cause.getMessage(), cause);
    }

    public static class DefaultSecHubClientBuilder {
        private URI serverUri;
        private String userName;
        private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();
        private SealedObject sealedApiToken;
        private boolean trustAll;

        public DefaultSecHubClientBuilder server(URI serverUri) {
            this.serverUri = serverUri;
            return this;
        }

        public DefaultSecHubClientBuilder user(String userId) {
            this.userName = userId;
            return this;
        }

        public DefaultSecHubClientBuilder apiToken(String token) {
            sealedApiToken = apiTokenAccess.seal(token);
            return this;
        }

        public DefaultSecHubClientBuilder trustAll(boolean trustAll) {
            this.trustAll = trustAll;
            return this;
        }

        public SecHubClient build() {
            if (serverUri == null) {
                throw new IllegalStateException("server uri is not defined!");
            }
            if (userName == null) {
                throw new IllegalStateException("user name is not defined!");
            }
            if (sealedApiToken == null) {
                throw new IllegalStateException("token is not defined!");
            }
            return new DefaultSecHubClient(serverUri, userName, apiTokenAccess.unseal(sealedApiToken), trustAll);
        }
    }

}
