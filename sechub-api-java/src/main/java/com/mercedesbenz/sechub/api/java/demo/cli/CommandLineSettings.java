// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import com.beust.jcommander.Parameter;

public class CommandLineSettings {
    @Parameter(names = { "--help" }, description = "Shows help and provides information on how to use OpenAPI Test Tool.", help = true)
    private boolean help;

    public boolean isHelpRequired() {
        return help;
    }

    @Parameter(names = { "--serverUri" }, description = "Specifies the Sechub Server URI. You can also set the environment variable "
            + EnvironmentVariableConstants.SECHUB_SERVER_URI + ", instead of using this parameter.", required = false)
    private String serverUri;

    public String getServerUri() {
        return serverUri;
    }

    @Parameter(names = { "--serverPort" }, description = "Specifies the Sechub Server port. You can also set the environment variable "
            + EnvironmentVariableConstants.SECHUB_SERVER_PORT + ", instead of using this parameter.", required = false)
    private int serverPort;

    public int getServerPort() {
        return serverPort;
    }

    @Parameter(names = { "--userId" }, description = "Specifies the Sechub Server privileged user id. You can also set the environment variable "
            + EnvironmentVariableConstants.SECHUB_USERID + ", instead of using this parameter.", required = false)
    private String userId;

    public String getUserId() {
        return userId;
    }

    @Parameter(names = { "--apiToken" }, description = "Specifies the privileged user's api token. You can also set the environment variable "
            + EnvironmentVariableConstants.SECHUB_APITOKEN + ", instead of using this parameter.", required = false)
    private String apiToken;

    public String getApiToken() {
        return apiToken;
    }

}
