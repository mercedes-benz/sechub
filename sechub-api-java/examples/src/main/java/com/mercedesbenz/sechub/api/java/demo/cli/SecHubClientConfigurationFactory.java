// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import java.net.URI;

import com.mercedesbenz.sechub.api.java.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.SecHubClientConfigurationRuntimeException;

public class SecHubClientConfigurationFactory {

    EnvironmentVariableReader environmentVariableReader;

    public SecHubClientConfigurationFactory() {
        environmentVariableReader = new EnvironmentVariableReader();
    }

    public SecHubClient create(CommandLineSettings settings) {
        URI serverUri = null;
        if (settings.getServer() != null)
            serverUri = URI.create(settings.getServer());

        String userId = settings.getUserId();
        String apiToken = settings.getApiToken();
        String trustAll = settings.getTrustAll();

        String envServerUri = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_SERVER);
        if (serverUri == null && envServerUri != null) {
            serverUri = URI.create(envServerUri);
        }
        if (userId == null) {
            userId = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_USERID);
        }
        if (apiToken == null) {
            apiToken = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_APITOKEN);
        }
        if (trustAll == null) {
            trustAll = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_TRUSTALL);
        }

        if (!isServerUriValid(serverUri)) {
            throw new SecHubClientConfigurationRuntimeException(
                    "Sechub server's URI is invalid. Please set the Sechub server's URI as an environment variable (SECHUB_SERVER) or as a program argument (--server).");
        }

        if (userId == null) {
            throw new SecHubClientConfigurationRuntimeException(
                    "Sechub server's privileged user id is null. Please set the Sechub server's privileged user id as an environment variable (SECHUB_USERID) or as a program argument (--userId).");
        }

        if (apiToken == null) {
            throw new SecHubClientConfigurationRuntimeException(
                    "Privileged user's API token is null. Please set the privileged user's API token as an environment variable (SECHUB_APITOKEN) or as a program argument (--apiToken).");
        }

        if (!isTrustAllValid(trustAll)) {
            throw new SecHubClientConfigurationRuntimeException(
                    "trustAll value is invalid. Please set the trustAll as an environment variable (SECHUB_TRUSTALL) or as a program argument (--trustAll). Correct values are true and false.");
        }
        return trustAll == null ? SecHubClient.create(userId, apiToken, serverUri)
                : SecHubClient.create(userId, apiToken, serverUri, Boolean.parseBoolean(trustAll));
    }

    private boolean isServerUriValid(URI serverUri) {
        if (serverUri == null)
            return false;
        if (!serverUri.getScheme().equals("https"))
            return false;
        if (!serverUri.getPath().isEmpty())
            return false;
        if (serverUri.getPort() <= 0 || serverUri.getPort() > 65535)
            return false;
        return true;
    }

    private boolean isTrustAllValid(String trustAll) {
        if (trustAll == null || trustAll.equals("false") || trustAll.equals("true"))
            return true;
        return false;
    }
}
