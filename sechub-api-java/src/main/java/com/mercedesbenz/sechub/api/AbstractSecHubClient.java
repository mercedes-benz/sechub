package com.mercedesbenz.sechub.api;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public abstract class AbstractSecHubClient implements SecHubClient {

    private boolean trustAll;
    private String username;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();

    private List<SecHubClientListener> secHubClientListeners;

    public AbstractSecHubClient() {
        secHubClientListeners = new LinkedList<>();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setApiToken(String apiToken) {
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
    }

    public void setServerUri(URI serverUri) {
        this.serverUri = serverUri;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
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

    /**
     * Adds an listener to the client. For some action on client side the listener
     * will be informed. A listener can be added only one time no matter how many
     * times this method is called.
     *
     * @param listener
     */
    @Override
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
    @Override
    public void removeListener(SecHubClientListener listener) {
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