// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import com.mercedesbenz.sechub.api.java.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.SecHubClientConfigurationRuntimeException;

public class SecHubClientConfigurationFactory {

    EnvironmentVariableReader environmentVariableReader;

    public SecHubClientConfigurationFactory() {
        environmentVariableReader = new EnvironmentVariableReader();
    }

    public SecHubClient create(CommandLineSettings settings) {
        String serverUri = settings.getServerUri();
        int serverPort = settings.getServerPort();
        String userId = settings.getUserId();
        String apiToken = settings.getApiToken();

        if (serverUri == null) {
            serverUri = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_SERVER_URI);
        }
        if (serverPort <= 0) {
            serverPort = environmentVariableReader.readAsInt(EnvironmentVariableConstants.SECHUB_SERVER_PORT);
        }
        if (userId == null) {
            userId = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_USERID);
        }
        if (apiToken == null) {
            apiToken = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_APITOKEN);
        }

        if (serverUri == null) {
            throw new SecHubClientConfigurationRuntimeException(
                    "Sechub server's URI is null. Please set the Sechub server's URI as an environment variable (SECHUB_SERVER_URI) or as a program argument (--serverUri).");
        }

        if (serverPort <= 0) {
            throw new SecHubClientConfigurationRuntimeException("Sechub server's port is set to " + serverPort
                    + ". Please set the Sechub server's port as an environment variable (SECHUB_SERVER_PORT) or as a program argument (--serverPort).");
        }

        if (userId == null) {
            throw new SecHubClientConfigurationRuntimeException(
                    "Sechub server's privileged user id is null. Please set the Sechub server's privileged user id as an environment variable (SECHUB_USERID) or as a program argument (--userId).");
        }

        if (apiToken == null) {
            throw new SecHubClientConfigurationRuntimeException(
                    "Privileged user's API token is null. Please set the privileged user's API token as an environment variable (SECHUB_APITOKEN) or as a program argument (--apiToken).");
        }

        return SecHubClient.create(userId, apiToken, serverUri, serverPort);
    }
}
