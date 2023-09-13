// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo;

import static com.mercedesbenz.sechub.api.java.demo.Utils.*;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.config.ConfigurationProvider;
import com.mercedesbenz.sechub.api.java.demo.playground.AdminApiPlayground;

public class OpenAPITestTool {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAPITestTool.class);

    public static void main(String[] args) {
        new OpenAPITestTool().start(args);
    }

    private void start(String[] args) {
        try {
            ConfigurationProvider configProvider = ConfigurationProvider.create(args);

            String userName = configProvider.getUser();
            String apiToken = configProvider.getApiToken();
            URI serverUri = configProvider.getServerUri();
            boolean trustAll = configProvider.isTrustAll();

            LOG.trace("Granted the following Sechub connection parameters:");
            LOG.trace("*** Sechub server URI: {}", serverUri);
            LOG.trace("*** Privileged user id: {}", userName);
            LOG.trace("*** Privileged user's API token: {}", "*".repeat(apiToken.length()));
            LOG.trace("*** trustAll: {}", trustAll);

            SecHubClient client = new DefaultSecHubClient(serverUri, userName, apiToken, trustAll);

            // simple test here
            testAnonymousApi(client);

            // more sophisticated stuff
            new AdminApiPlayground(client).run();

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
