// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.sechubaccess;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.MockedSecHubClient;
import com.mercedesbenz.sechub.api.OldDefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.webserver.ApplicationProfiles;

import jakarta.annotation.PostConstruct;

/**
 * Main class for communication with SecHub server. The {@link SecHubClient} is
 * not provided directly but via {@link #createExecutorForResult(Class)} and
 * {@link #createExecutorWithoutResult()} methods which provide a fluent and
 * secured access.
 *
 *
 * @author Albert Tregnaghi
 *
 */
@Service
@Profile(ApplicationProfiles.CLASSIC_AUTH_ENABLED)
public class SecHubAccessService {

    static final Logger LOG = LoggerFactory.getLogger(SecHubAccessService.class);

    @Value("${web-server.sechub.server-url}")
    private String secHubServerUrl;

    @Value("${web-server.sechub.trust-all-certificates:false}")
    private boolean trustAllCertificates;

    @Value("${web-server.client.mocked:false}")
    private boolean useMockedClient;

    @Value("${web-server.sechub.userid}")
    private String userId;

    @Value("${web-server.sechub.apitoken}")
    private String apiToken;

    private SecHubClient client;

    @PostConstruct
    void initSecHubClient() {
        URI serverUri = URI.create(secHubServerUrl);
        /*
         * TODO Albert Tregnaghi, 2024-02-28: currently we have ONE client - maybe this
         * is okay, but when we use real user credentials/are delegate for it etc. it
         * could become necessary to have different clients ?! Means this is an open
         * question
         */
        /* @formatter:off */
        try {
            if (useMockedClient) {
                this.client = MockedSecHubClient.from(serverUri, userId, apiToken, trustAllCertificates);
            } else {

                this.client = OldDefaultSecHubClient.builder().
                        server(serverUri).
                        user(userId).
                        apiToken(apiToken).
                        trustAll(trustAllCertificates).
                        build();
            }
        }finally {
            // reset sensitive data - is now stored secure in client object
            userId= null;
            apiToken=null;
        }
        /* @formatter:on */
    }

    public boolean isSecHubServerAlive() {
        try {
            return client.isServerAlive();
        } catch (SecHubClientException e) {
            return false;
        }
    }

    public String getServerVersion() {
        try {
            return client.getServerVersion();
        } catch (SecHubClientException e) {
            return "invalid";
        }
    }

    public URI getSecHubServerUri() {
        return client.getServerUri();
    }

    public <T> SecHubClientExecutor<T> createExecutorForResult(Class<T> clazz) {
        return new SecHubClientExecutor<>(client, clazz);
    }

    public SecHubClientExecutor<Void> createExecutorWithoutResult() {
        return new SecHubClientExecutor<>(client, Void.class);
    }

}
