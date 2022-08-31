// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.java.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.OpenAPITestToolRuntimeException;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.SecHubClientConfigurationRuntimeException;

public class OpenAPITestTool {
    private static final Logger LOG = LoggerFactory.getLogger(OpenAPITestTool.class);

    SecHubClient secHubClient = null;

    public static void main(String[] args) {
        new OpenAPITestTool().start(args);
    }

    private void start(String[] args) {
        try {
            LOG.info("Building connection configuration.");
            secHubClient = resolveConfiguration(args);

            LOG.info("Testing Sechub server using OpenAPI library.");
            SechubServerTest sechubServerTest = new SechubServerTest(secHubClient);

            sechubServerTest.runAllTests();
            LOG.info("Sechub server successfully tested.");
        } catch (OpenAPITestToolRuntimeException e) {
            LOG.error("Sechub server testing failed: ", e);
        }

    }

    private SecHubClient resolveConfiguration(String[] args) {
        CommandLineParser parser = new CommandLineParser();
        try {
            secHubClient = parser.parse(args);
            LOG.info("Granted the following Sechub connection parameters:");
            LOG.info("*** Sechub server URI: " + secHubClient.getHostUri());
            LOG.info("*** Sechub server port: " + secHubClient.getHostPort());
            LOG.info("*** Privileged user id: " + secHubClient.getUsername());
            LOG.info("*** Privileged user's API token: " + secHubClient.getSealedApiToken());
        } catch (SecHubClientConfigurationRuntimeException e) {
            LOG.error("An error occurred while parsing the command line arguments: ", e);
            throw new OpenAPITestToolRuntimeException("Connection configuration was invalid.");
        }
        return secHubClient;
    }
}
