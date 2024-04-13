// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.sechubaccess;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.MockedSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;

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
public class SecHubAccessService {

    static final Logger LOG = LoggerFactory.getLogger(SecHubAccessService.class);

    @Value("${webui.sechub.server-url}")
    private String secHubServerUrl;

    @Value("${webui.sechub.trust-all-certificates:false}")
    private boolean trustAllCertificates;

    @Value("${webui.client.mocked:false}")
    private boolean useMockedClient;

    @Value("${webui.sechub.userid}")
    private String userId;

    @Value("${webui.sechub.apitoken}")
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

                this.client = DefaultSecHubClient.builder().
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
