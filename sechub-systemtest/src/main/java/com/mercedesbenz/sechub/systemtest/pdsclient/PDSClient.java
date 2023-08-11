package com.mercedesbenz.sechub.systemtest.pdsclient;

import java.net.URI;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
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
}
