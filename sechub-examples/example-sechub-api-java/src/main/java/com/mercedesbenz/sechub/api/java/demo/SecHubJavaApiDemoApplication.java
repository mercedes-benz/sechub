// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo;

import static com.mercedesbenz.sechub.api.java.demo.DemoUtils.*;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.OldDefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.config.DemoConfigurationProvider;
import com.mercedesbenz.sechub.api.java.demo.playground.DemoAdminApiPlayground;

public class SecHubJavaApiDemoApplication {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubJavaApiDemoApplication.class);

    public static void main(String[] args) {
        new SecHubJavaApiDemoApplication().start(args);
    }

    private void start(String[] args) {
        try {
            DemoConfigurationProvider configProvider = DemoConfigurationProvider.create(args);

            String userName = configProvider.getUserId();
            String apiToken = configProvider.getApiToken();
            URI serverUri = configProvider.getServerUri();
            boolean trustAll = configProvider.isTrustAll();

            LOG.trace("Granted the following Sechub connection parameters:");
            LOG.trace("*** Sechub server URI: {}", serverUri);
            LOG.trace("*** Privileged user id: {}", userName);
            LOG.trace("*** Privileged user's API token: {}", "*".repeat(apiToken.length()));
            LOG.trace("*** trustAll: {}", trustAll);

            /* create the client */
            SecHubClient client = OldDefaultSecHubClient.builder().server(serverUri).user(userName).apiToken(apiToken).trustAll(trustAll).build();

            // test anonymous parts
            testAnonymousApi(client);

            // test admin parts
            new DemoAdminApiPlayground(client).run();

            LOG.info("Sechub server successfully tested.");
            System.out.println("[ OK ] SecHub was accessible with generated Java API");

        } catch (Exception e) {
            LOG.error("Sechub server testing failed: ", e);
        }

    }

    private void testAnonymousApi(SecHubClient client) throws Exception {
        logTitle("Start testing anonymous API");

        boolean serverAlive = client.isServerAlive();

        assumeEquals(true, serverAlive, "SecHub server is alive");

    }

}
