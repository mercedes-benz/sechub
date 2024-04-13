// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public abstract class AbstractSecHubClient implements SecHubClient {

    private boolean trustAll;
    private String userId;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();

    private Set<SecHubClientListener> secHubClientListeners;

    protected AbstractSecHubClient(URI serverUri, String userId, String apiToken, boolean trustAll) {
        this.serverUri = serverUri;
        this.trustAll = trustAll;

        this.secHubClientListeners = new LinkedHashSet<>();

        setUserId(userId);
        setApiToken(apiToken);
    }

    public void setUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId may be not null");
        }
        this.userId = userId;
    }

    public void setApiToken(String apiToken) {
        if (apiToken == null) {
            throw new IllegalArgumentException("api token may be not null");
        }
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
    }

    @Override
    public boolean isTrustAll() {
        return trustAll;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getSealedApiToken() {
        return apiTokenAccess.unseal(sealedApiToken);
    }

    @Override
    public URI getServerUri() {
        return serverUri;
    }

    protected File calculateFullScanLogFile(UUID sechubJobUUID, Path downloadFilePath) throws SecHubClientException {
        File file = null;
        if (downloadFilePath == null) {
            try {
                downloadFilePath = Files.createTempDirectory("sechub-fullscanlog");
            } catch (IOException e) {
                throw new SecHubClientException("Was not able to create temp directory", e);
            }
        }
        file = downloadFilePath.toFile();
        if (file.isDirectory()) {
            file = new File(file, "SecHub-" + sechubJobUUID + "-scanlog.zip");
        }
        file.getParentFile().mkdirs();
        return file;
    }

    /**
     * Adds a listener to the client. For some action on client side the listener
     * will be informed. Same listener instance can be added only one time no matter
     * how many times this method is called.
     *
     * @param listener
     */
    @Override
    public void addListener(SecHubClientListener listener) {
        if (listener == null) {
            return;
        }
        this.secHubClientListeners.add(listener);
    }

    /**
     * Removes a listener from the client (if added).
     *
     * @param listener
     */
    @Override
    public void removeListener(SecHubClientListener listener) {
        if (listener == null) {
            return;
        }
        this.secHubClientListeners.remove(listener);
    }

    void inform(SecHubClientListenerCaller r) {
        for (SecHubClientListener listener : secHubClientListeners) {
            r.inform(listener);
        }
    }

    interface SecHubClientListenerCaller {

        public void inform(SecHubClientListener listener);

    }

}