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
    private String username;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();

    private Set<SecHubClientListener> secHubClientListeners;

    public AbstractSecHubClient(URI serverUri, String username, String apiToken, boolean trustAll) {
        this.serverUri = serverUri;
        this.trustAll = trustAll;

        this.secHubClientListeners = new LinkedHashSet<>();

        setUsername(username);
        setApiToken(apiToken);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setApiToken(String apiToken) {
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
    }

    @Override
    public boolean isTrustAll() {
        return trustAll;
    }

    @Override
    public String getUsername() {
        return username;
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
     * will be informed. A listener can be added only one time no matter how many
     * times this method is called.
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