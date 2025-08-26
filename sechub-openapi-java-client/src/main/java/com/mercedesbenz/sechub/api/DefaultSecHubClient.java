// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.api.internal.gen.*;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubConfiguration;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SealedObject;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class DefaultSecHubClient extends AbstractSecHubClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSecHubClient.class);

    private static final JsonMapper mapper = JsonMapperFactory.createMapper();

    private final ArchiveSupport archiveSupport = new ArchiveSupport();
    private final CheckSumSupport checkSumSupport = new CheckSumSupport();

    private final ConfigurationApi configurationApi;
    private final JobAdministrationApi jobAdministrationApi;
    private final OtherApi otherApi;
    private final ProjectAdministrationApi projectAdministrationApi;
    private final SecHubExecutionApi secHubExecutionApi;
    private final SignUpApi signUpApi;
    private final SystemApi systemApi;
    private final TestingApi testingApi;
    private final UserAdministrationApi userAdministrationApi;
    private final UserSelfServiceApi userSelfServiceApi;
    private final AssistantApi assistantApi;
    private final SecHubExecutionWorkaroundApi secHubExecutionWorkaroundApi;

    public static DefaultSecHubClientBuilder builder() {
        return new DefaultSecHubClientBuilder();
    }

    private DefaultSecHubClient(URI serverUri, String userId, String apiToken, boolean trustAll) {
        super(serverUri, userId, apiToken, trustAll);

        ApiClient apiClient = new ApiClientBuilder().createApiClient(this, mapper);
        configurationApi = new ConfigurationApi(apiClient);
        jobAdministrationApi = new JobAdministrationApi(apiClient);
        otherApi = new OtherApi(apiClient);
        projectAdministrationApi = new ProjectAdministrationApi(apiClient);
        secHubExecutionApi = new SecHubExecutionApi(apiClient);
        signUpApi = new SignUpApi(apiClient);
        systemApi = new SystemApi(apiClient);
        testingApi = new TestingApi(apiClient);
        userAdministrationApi = new UserAdministrationApi(apiClient);
        userSelfServiceApi = new UserSelfServiceApi(apiClient);
        assistantApi = new AssistantApi(apiClient);
        secHubExecutionWorkaroundApi = new SecHubExecutionWorkaroundApi(apiClient);
    }

    /**
     * Uploads data as defined in given configuration
     *
     * @param projectId        the project id
     * @param jobUUID          SecHub Job UUID
     * @param configuration    SecHub Job configuration (contains information about
     *                         upload behavior (e.g. paths etc.)
     * @param workingDirectory directory where the relative paths inside
     *                         configuration model shall start from
     * @throws ApiException
     */
    @Override
    public void userUpload(String projectId, UUID jobUUID, SecHubConfiguration configuration, Path workingDirectory) throws ApiException {
        requireNonNull(projectId, "projectId may not be null!");
        requireNonNull(configuration, "configuration may not be null!");

        Path uploadDirectory;
        try {
            uploadDirectory = Files.createTempDirectory("sechub_client_upload");
        } catch (IOException e) {
            throw new ApiException(e);
        }

        ArchiveSupport.ArchivesCreationResult createArchiveResult;
        try {
            // TODO: This is a workaround until the sechub commons model classes have been completely substituted by the
            //  generated classes from the new openapi module
            //  see https://github.com/mercedes-benz/sechub/issues/3284
            SecHubConfigurationModel secHubConfigurationModel = mapper.reader()
                    .forType(SecHubConfigurationModel.class)
                    .readValue(mapper.writeValueAsString(configuration));

            createArchiveResult = archiveSupport.createArchives(secHubConfigurationModel, workingDirectory, uploadDirectory);
        } catch (IOException e) {
            throw new ApiException(e);
        }

        inform((listener) -> listener.beforeUpload(jobUUID, configuration, createArchiveResult));

        try {
            userUploadsSourceCode(projectId, jobUUID, createArchiveResult);
            userUploadsBinaries(projectId, jobUUID, createArchiveResult);

            inform((listener) -> listener.afterUpload(jobUUID, configuration, createArchiveResult));

        } finally {
            LOG.debug("Remove temporary data from: {}", uploadDirectory);
            try {
                archiveSupport.deleteArchives(createArchiveResult);
            } catch (IOException e) {
                throw new ApiException(e);
            }
        }
    }

    @Override
    public boolean isServerAlive() {
        try {
            systemApi.anonymousCheckAliveHead();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public Path downloadFullScanLog(UUID sechubJobUUID, Path downloadFilePath) throws ApiException  {
        try {
            final File targetFile = calculateFullScanLogFile(sechubJobUUID, downloadFilePath);
            secHubExecutionWorkaroundApi.adminDownloadsFullScanDataForJob(sechubJobUUID, targetFile);
            return targetFile.toPath();
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................APIs............................ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    @Override
    public ConfigurationApi withConfigurationApi() {
        return configurationApi;
    }

    @Override
    public JobAdministrationApi withJobAdministrationApi() {
        return jobAdministrationApi;
    }

    @Override
    public OtherApi withOtherApi() {
        return otherApi;
    }

    @Override
    public ProjectAdministrationApi withProjectAdministrationApi() {
        return projectAdministrationApi;
    }

    @Override
    public SecHubExecutionApi withSecHubExecutionApi() {
        return secHubExecutionApi;
    }

    @Override
    public SignUpApi withSignUpApi() {
        return signUpApi;
    }

    @Override
    public SystemApi withSystemApi() {
        return systemApi;
    }

    @Override
    public TestingApi withTestingApi() {
        return testingApi;
    }

    @Override
    public UserAdministrationApi withUserAdministrationApi() {
        return userAdministrationApi;
    }

    @Override
    public UserSelfServiceApi withUserSelfServiceApi() {
        return userSelfServiceApi;
    }

    @Override
    public AssistantApi withAssistantApi() {
        return assistantApi;
    }

    private void userUploadsBinaries(String projectId, UUID jobUUID, ArchiveSupport.ArchivesCreationResult createArchiveResult) throws ApiException {
        if (!createArchiveResult.isBinaryArchiveCreated()) {
            return;
        }
        Path tarFilePath = createArchiveResult.getBinaryArchiveFile();

        String filesize = String.valueOf(tarFilePath.toFile().length());
        String checksum = checkSumSupport.createSha256Checksum(tarFilePath);

        secHubExecutionWorkaroundApi.userUploadsBinaries(projectId, jobUUID, checksum, filesize, tarFilePath);
    }

    private void userUploadsSourceCode(String projectId, UUID jobUUID, ArchiveSupport.ArchivesCreationResult createArchiveResult) throws ApiException {
        if (!createArchiveResult.isSourceArchiveCreated()) {
            return;
        }
        Path zipFilePath = createArchiveResult.getSourceArchiveFile();
        String checksum = checkSumSupport.createSha256Checksum(zipFilePath);

        secHubExecutionWorkaroundApi.userUploadsSourceCode(projectId, jobUUID, checksum, zipFilePath);
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
