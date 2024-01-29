// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.config;

import com.beust.jcommander.Parameter;

public class CommandLineSettings {
    @Parameter(names = { "--help" }, description = "Shows help and provides information on how to use OpenAPI Test Tool.", help = true)
    private boolean help;

    public boolean isHelpRequired() {
        return help;
    }

    @Parameter(names = { "--server" }, description = "Specifies the Sechub Server URI. You can also set the environment variable "
            + EnvironmentVariableConstants.SECHUB_SERVER + ", instead of using this parameter.", required = false)
    private String server;

    public String getServer() {
        return server;
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

    @Parameter(names = {
            "--trustAll" }, description = "When set to true, then HTTPS certificate checking will be disabled. May be useful when using self-signed certificates. Please try to avoid this setting for security reasons. You can also set the environment variable "
                    + EnvironmentVariableConstants.SECHUB_TRUSTALL + ", instead of using this parameter.", required = false)
    private Boolean trustAll;

    public Boolean getTrustAll() {
        return trustAll;
    }

}
