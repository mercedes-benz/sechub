// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.config;

import java.net.URI;

public class ConfigurationProvider {

    private EnvironmentVariableReader environmentVariableReader;
    private CommandLineSettings settings;

    public ConfigurationProvider(CommandLineSettings settings, EnvironmentVariableReader environmentVariableReader) {
        this.settings = settings;
        this.environmentVariableReader = environmentVariableReader;
    }
    
    public static ConfigurationProvider create(String[] args) {
        CommandLineParser parser = new CommandLineParser();
        CommandLineSettings commandLineSettings = parser.parse(args);
        
        ConfigurationProvider configProvider = new ConfigurationProvider(commandLineSettings, new EnvironmentVariableReader());
        
        return configProvider;
    }

    public String getUser() {
        String userId = settings.getUserId();
        if (userId == null) {
            userId = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_USERID);
        }

        if (userId == null) {
            throw new IllegalStateException(
                    "Sechub server's privileged user id is null. Please set the Sechub server's privileged user id as an environment variable (SECHUB_USERID) or as a program argument (--userId).");
        }

        return userId;
    }

    public String getApiToken() {
        String apiToken = settings.getApiToken();
        if (apiToken == null) {
            apiToken = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_APITOKEN);
        }
        if (apiToken == null) {
            throw new IllegalStateException(
                    "Privileged user's API token is null. Please set the privileged user's API token as an environment variable (SECHUB_APITOKEN) or as a program argument (--apiToken).");
        }

        return apiToken;
    }

    public boolean isTrustAll() {
        Boolean trustAll = settings.getTrustAll();
        if (trustAll == null) {
            trustAll = Boolean.parseBoolean(environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_TRUSTALL));
        }
        return trustAll;
    }

    public URI getServerUri() {
        URI serverUri = null;
        if (settings.getServer() != null) {
            serverUri = URI.create(settings.getServer());
        }
        if (serverUri == null) {
            String envServerUri = environmentVariableReader.readAsString(EnvironmentVariableConstants.SECHUB_SERVER);
            if (envServerUri != null) {
                serverUri = URI.create(envServerUri);
            }

        }
        if (serverUri == null) {
            throw new IllegalStateException(
                    "Sechub server's URI is not set. Please set the Sechub server's URI as an environment variable (SECHUB_SERVER) or as a program argument (--server).");
        }
        return serverUri;
    }

}
