// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import static java.util.Objects.requireNonNull;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;

public class SecHubAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubAccess.class);
    private SecHubClient client;

    public SecHubAccess(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {
        initSecHubClient(secHubServerUrl, userId, apiToken, trustAllCertificates);
    }

    public boolean isSecHubServerAlive() {
        if (client == null) {
            LOG.debug("SecHub client is not initialized");
            return false;
        }
        try {
            return client.isServerAlive();
        } catch (ApiException e) {
            LOG.debug("Failed to check SecHub server status", e);
            return false;
        }
    }

    private void initSecHubClient(String secHubServerUrl, String userId, String apiToken, boolean trustAllCertificates) {

        requireNonNull(secHubServerUrl, "Parameter 'secHubServerUrl' must not be null");
        requireNonNull(userId, "Parameter 'userId' must not be null");
        requireNonNull(apiToken, "Parameter 'apiToken' must not be null");

        URI serverUri;
        try {
            serverUri = URI.create(secHubServerUrl);
        } catch (IllegalArgumentException e) {
            LOG.error("Parameter 'secHubServerUrl' must contain a valid secHub server URL", e);
            throw new IllegalStateException("Invalid parameter 'secHubServerUrl': %s".formatted(secHubServerUrl), e);
        }

        /* @formatter:off */
        this.client = DefaultSecHubClient.builder()
                .server(serverUri)
                .user(userId)
                .apiToken(apiToken)
                .trustAll(trustAllCertificates)
                .build();
        /* @formatter:on */
    }
}
