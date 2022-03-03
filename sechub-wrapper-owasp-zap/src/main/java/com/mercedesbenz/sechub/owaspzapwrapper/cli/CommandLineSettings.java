// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.Parameter;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants;

public class CommandLineSettings {
    @Parameter(names = { "--help", "-h" }, description = "Shows help and provides information on how to use the wrapper.", help = true)
    private boolean help;

    public boolean isHelpRequired() {
        return help;
    }

    @Parameter(names = { "--targetURL", "-t" }, description = "Specifies the target url to be scanned.", required = true)
    private String targetURL;

    public String getTargetURL() {
        return targetURL;
    }

    @Parameter(names = { "--report", "-r" }, description = "The output file, where the report will be written.", required = true)
    private String reportFile;

    public Path getReportFile() {
        Path reportFileAsPath = Paths.get(reportFile);
        return reportFileAsPath.toAbsolutePath();
    }

    @Parameter(names = { "--jobUUID",
            "-j" }, description = "The Job-UUID, which will be used as internal identifier for the owasp zap scan context.", required = false)
    private String jobUUID;

    public String getJobUUID() {
        return jobUUID;
    }

    @Parameter(names = { "--sechubConfigfile",
            "-s" }, description = "The sechub config file, containing additonal configurations for the scan.", required = false)
    private String sechubConfigFile;

    public File getSecHubConfigFile() {
        if (sechubConfigFile == null) {
            return null;
        }
        try {
            return new File(sechubConfigFile).toPath().toRealPath().toAbsolutePath().toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Not able to resolve absolute path of:" + sechubConfigFile);
        }

    }

    @Parameter(names = { "--ajaxSpider", "-as" }, description = "Set this option to enable owasp zap ajaxSpider.", required = false)
    private boolean ajaxSpiderEnabled;

    public boolean isAjaxSpiderEnabled() {
        return ajaxSpiderEnabled;
    }

    @Parameter(names = { "--activeScan", "-A" }, description = "Set this option to enable owasp zap active scan.", required = false)
    private boolean activeScanEnabled;

    public boolean isActiveScanEnabled() {
        return activeScanEnabled;
    }

    @Parameter(names = { "--zapHost", "-H" }, description = "Specifies the owasp zap host address. You can also set the environment variable "
            + EnvironmentVariableConstants.ZAP_HOST_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String zapHost;

    public String getZapHost() {
        return zapHost;
    }

    @Parameter(names = { "--zapPort", "-P" }, description = "Specifies the owasp zap host port. You can also set the environment variable "
            + EnvironmentVariableConstants.ZAP_PORT_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private int zapPort;

    public int getZapPort() {
        return zapPort;
    }

    @Parameter(names = { "--zapApiKey", "-K" }, description = "Specifies the owasp zap host api key. You can also set the environment variable "
            + EnvironmentVariableConstants.ZAP_API_KEY_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String zapApiKey;

    public String getZapApiKey() {
        return zapApiKey;
    }

    @Parameter(names = { "--verbose", "-v" }, description = "Set this option to provide more output during scanning.", required = false)
    private boolean verbose;

    public boolean isVerboseEnabled() {
        return verbose;
    }

    @Parameter(names = { "--proxyHost" }, description = "Specify a proxy host. You can also set the environment variable "
            + EnvironmentVariableConstants.PROXY_HOST_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String proxyHost;

    public String getProxyHost() {
        return proxyHost;
    }

    @Parameter(names = { "--proxyPort" }, description = "Specify a proxy port. You can also set the environment variable "
            + EnvironmentVariableConstants.PROXY_PORT_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private int proxyPort;

    public int getProxyPort() {
        return proxyPort;
    }
}
