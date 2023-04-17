// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo;

import static com.mercedesbenz.sechub.api.java.demo.Utils.*;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.java.AnonymousApi;
import com.mercedesbenz.sechub.api.java.ApiException;
import com.mercedesbenz.sechub.api.java.SecHubAccess;
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

            SecHubAccess sechubAccess = new SecHubAccess(serverUri, userName, apiToken, trustAll);

            testAnonymousApi(sechubAccess);

            // more sophisticated stuff
            new AdminApiPlayground(sechubAccess).run();

            LOG.info("Sechub server successfully tested.");
            System.out.println("[ OK ] SecHub was accessible with generated Java API");

        } catch (Exception e) {
            LOG.error("Sechub server testing failed: ", e);
        }

    }

    private void testAnonymousApi(SecHubAccess access) throws ApiException {
        logTitle("Start testing anonymous API");
        AnonymousApi anonymousApi = access.getAnonymousApi();

        anonymousApi.anonymousCheckAliveGet();
        logSuccess("Sechub server is alive (GET).");

        anonymousApi.anonymousCheckAliveHead();
        logSuccess("Sechub server is alive (HEAD).");

    }

    

    

}
