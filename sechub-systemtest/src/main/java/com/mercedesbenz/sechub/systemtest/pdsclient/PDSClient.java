// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.pdsclient;

import java.net.URI;
import java.util.UUID;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiResponse;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.systemtest.pdsclient.internal.PDSApiClient;
import com.mercedesbenz.sechub.systemtest.pdsclient.internal.PDSApiClientBuilder;
import com.mercedesbenz.sechub.systemtest.pdsclient.internal.PDSCommonApi;

public class PDSClient {
    private static JsonMapper mapper = JsonMapperFactory.createMapper();

    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();
    private String username;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private boolean trustAll;

    private PDSApiClient pdsApiClient;

    private PDSCommonApi commonApi;

    public PDSClient(URI serverUri, String username, String apiToken) {
        this(serverUri, username, apiToken, false);
    }

    public PDSClient(URI serverUri, String username, String apiToken, boolean trustAll) {
        this.username = username;
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
        this.serverUri = serverUri;
        this.trustAll = trustAll;
        this.pdsApiClient = new PDSApiClientBuilder().createApiClient(this, mapper);
        this.commonApi = new PDSCommonApi(pdsApiClient);

    }

    public boolean checkIsServerAlive() throws PDSClientException {
        try {
            commonApi.anonymousCheckAliveHead();
            return true;
        } catch (ApiException e) {
            return false;
        }
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

    public boolean isJobExisting(UUID pdsJobUUID) throws PDSClientException {
        return isJobExisting(pdsJobUUID.toString());
    }

    public boolean isJobExisting(String pdsJobUUID) throws PDSClientException {
        try {
            ApiResponse<PDSJobStatus> status = fetchJobStatusApiResponse(pdsJobUUID);
            int statusCode = status.getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return true;
            }
            return true;

        } catch (ApiException e) {
            throw new PDSClientException("Cannot check if job " + pdsJobUUID
                    + " exists or not. Either your PDS solution is not accessible from this network, or your PDS admin credentials are not set or wrong ? Used user: '"
                    + getUsername(), e);
        }
    }

    public PDSJobStatus fetchJobStatus(UUID pdsJobUUID) throws PDSClientException {
        try {
            ApiResponse<PDSJobStatus> status = fetchJobStatusApiResponse(pdsJobUUID.toString());
            return status.getData();

        } catch (ApiException e) {
            throw new PDSClientException("Get job status failed", e);
        }
    }

    private ApiResponse<PDSJobStatus> fetchJobStatusApiResponse(String pdsJobUUID) throws ApiException {
        ApiResponse<PDSJobStatus> status = commonApi.getAuthorized("/api/job/" + pdsJobUUID + "/status", PDSJobStatus.class);
        return status;
    }

    public SecHubMessagesList fetchJobMessages(String pdsJobUUID) throws PDSClientException {
        try {
            ApiResponse<String> jsonResonse = commonApi.getAuthorized("/api/job/" + pdsJobUUID + "/messages", String.class);
            String json = jsonResonse.getData();
            return JSONConverter.get().fromJSON(SecHubMessagesList.class, json);
        } catch (ApiException e) {
            throw new PDSClientException("Fetch job messages failed", e);
        }
    }

    public String fetchJobOutputStreamContentAsText(String pdsJobUUID) throws PDSClientException {
        try {
            return commonApi.getAuthorized("/api/admin/job/" + pdsJobUUID + "/stream/output", String.class).getData();
        } catch (ApiException e) {
            throw new PDSClientException("Cannot fetch output stream for PDS job:" + pdsJobUUID, e);
        }
    }

    public String fetchJobErrorStreamContentAsText(String pdsJobUUID) throws PDSClientException {
        try {
            return commonApi.getAuthorized("/api/admin/job/" + pdsJobUUID + "/stream/error", String.class).getData();
        } catch (ApiException e) {
            throw new PDSClientException("Cannot fetch error stream for PDS job:" + pdsJobUUID, e);
        }
    }

    public String fetchJobResultAsText(String pdsJobUUID) throws PDSClientException {
        try {
            return commonApi.getAuthorized("/api/admin/job/" + pdsJobUUID + "/result", String.class).getData();
        } catch (ApiException e) {
            throw new PDSClientException("Cannot fetch result for PDS job:" + pdsJobUUID, e);
        }
    }

    public String fetchJobMetaDataAsText(String pdsJobUUID) throws PDSClientException {
        try {
            return commonApi.getAuthorized("/api/admin/job/" + pdsJobUUID + "/metadata", String.class).getData();
        } catch (ApiException e) {
            throw new PDSClientException("Cannot fetch meta data for PDS job:" + pdsJobUUID, e);
        }
    }

}
