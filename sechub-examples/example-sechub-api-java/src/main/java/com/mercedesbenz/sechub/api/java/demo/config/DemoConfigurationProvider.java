// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.config;

import java.net.URI;

public class DemoConfigurationProvider {

    private DemoEnvironmentVariableReader demoEnvironmentVariableReader;
    private DemoCommandLineSettings settings;

    public DemoConfigurationProvider(DemoCommandLineSettings settings, DemoEnvironmentVariableReader demoEnvironmentVariableReader) {
        this.settings = settings;
        this.demoEnvironmentVariableReader = demoEnvironmentVariableReader;
    }
    
    public static DemoConfigurationProvider create(String[] args) {
        DemoCommandLineParser parser = new DemoCommandLineParser();
        DemoCommandLineSettings demoCommandLineSettings = parser.parse(args);
        
        DemoConfigurationProvider configProvider = new DemoConfigurationProvider(demoCommandLineSettings, new DemoEnvironmentVariableReader());
        
        return configProvider;
    }

    public String getUserId() {
        String userId = settings.getUserId();
        if (userId == null) {
            userId = demoEnvironmentVariableReader.readAsString(DemoEnvironmentVariableConstants.SECHUB_USERID);
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
            apiToken = demoEnvironmentVariableReader.readAsString(DemoEnvironmentVariableConstants.SECHUB_APITOKEN);
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
            trustAll = Boolean.parseBoolean(demoEnvironmentVariableReader.readAsString(DemoEnvironmentVariableConstants.SECHUB_TRUSTALL));
        }
        return trustAll;
    }

    public URI getServerUri() {
        URI serverUri = null;
        if (settings.getServer() != null) {
            serverUri = URI.create(settings.getServer());
        }
        if (serverUri == null) {
            String envServerUri = demoEnvironmentVariableReader.readAsString(DemoEnvironmentVariableConstants.SECHUB_SERVER);
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
