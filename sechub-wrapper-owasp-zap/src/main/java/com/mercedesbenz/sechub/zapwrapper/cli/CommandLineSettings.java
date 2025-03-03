// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.mercedesbenz.sechub.zapwrapper.config.ZAPAcceptedBrowserId;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.FileUtilities;

public class CommandLineSettings implements ZapWrapperConfiguration {
    @Parameter(names = { "--help" }, description = "Shows help and provides information on how to use the wrapper.", help = true)
    private boolean help;

    public boolean isHelpRequired() {
        return help;
    }

    @Parameter(names = { "--targetURL" }, description = "Specifies the target url to be scanned.", required = true)
    private String targetURL;

    public String getTargetURL() {
        return targetURL;
    }

    @Parameter(names = { "--report" }, description = "The output file, where the report will be written to.", required = true)
    private String reportFile;

    public Path getReportFile() {
        Path reportFileAsPath = Paths.get(reportFile);
        return reportFileAsPath.toAbsolutePath();
    }

    @Parameter(names = { "--jobUUID" }, description = "The Job-UUID, which will be used as internal identifier for the Zap scan context.", required = false)
    private String jobUUID;

    public String getJobUUID() {
        return jobUUID;
    }

    @Parameter(names = { "--sechubConfigfile" }, description = "The SecHub config file, containing additional configurations for the scan.", required = false)
    private String sechubConfigFile;

    public File getSecHubConfigFile() {
        return FileUtilities.stringToFile(sechubConfigFile);
    }

    @Parameter(names = { "--ajaxSpider" }, description = "Set this option to enable Zap ajaxSpider.", required = false)
    private boolean ajaxSpiderEnabled;

    public boolean isAjaxSpiderEnabled() {
        return ajaxSpiderEnabled;
    }

    @Parameter(names = { "--ajaxSpiderBrowserId" }, description = "Set the browser id you want to use for the AjaxSpider module. "
            + "Make sure the browser you want to use is installed on the system running the scan. "
            + "Supported browsers are: [firefox-headless, firefox, chrome-headless, chrome, htmlunit, safari].", required = false, validateWith = ZAPAcceptedBrowserIdValidator.class)
    private String ajaxSpiderBrowserId = ZAPAcceptedBrowserId.FIREFOX_HEADLESS.getBrowserId();

    public String getAjaxSpiderBrowserId() {
        return ajaxSpiderBrowserId;
    }

    @Parameter(names = { "--activeScan" }, description = "Set this option to enable Zap active scan.", required = false)
    private boolean activeScanEnabled;

    public boolean isActiveScanEnabled() {
        return activeScanEnabled;
    }

    @Parameter(names = { "--zapHost" }, description = "Specifies the Zap host address. You can also set the environment variable "
            + EnvironmentVariableConstants.ZAP_HOST_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String zapHost;

    public String getZapHost() {
        return zapHost;
    }

    @Parameter(names = { "--zapPort" }, description = "Specifies the Zap host port. You can also set the environment variable "
            + EnvironmentVariableConstants.ZAP_PORT_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private int zapPort;

    public int getZapPort() {
        return zapPort;
    }

    @Parameter(names = { "--zapApiKey" }, description = "Specifies the Zap host api key. You can also set the environment variable "
            + EnvironmentVariableConstants.ZAP_API_KEY_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String zapApiKey;

    public String getZapApiKey() {
        return zapApiKey;
    }

    @Parameter(names = { "--verbose" }, description = "Set this option to provide additional output while scanning.", required = false)
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

    @Parameter(names = { "--proxyRealm" }, description = "Specify a proxy realm. You can also set the environment variable "
            + EnvironmentVariableConstants.PROXY_REALM_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String proxyRealm;

    public String getProxyRealm() {
        return proxyRealm;
    }

    @Parameter(names = { "--proxyUsername" }, description = "Specify a proxy username. You can also set the environment variable "
            + EnvironmentVariableConstants.PROXY_USERNAME_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String proxyUsername;

    public String getProxyUsername() {
        return proxyUsername;
    }

    @Parameter(names = { "--proxyPassword" }, description = "Specify a proxy password. You can also set the environment variable "
            + EnvironmentVariableConstants.PROXY_PASSWORD_ENV_VARIABLE_NAME + ", instead of using this parameter.", required = false)
    private String proxyPassword;

    public String getProxyPassword() {
        return proxyPassword;
    }

    @Parameter(names = {
            "--deactivateRules" }, splitter = CommaParameterSplitter.class, variableArity = true, description = "Specify ZAP rule IDs you want to deactivate during the scan. "
                    + "If you specify multiple rule IDs use comma separated values like: 123,456,789", required = false)
    private List<String> deactivateRules = new ArrayList<>();

    public List<String> getDeactivateRules() {
        return deactivateRules;
    }

    @Parameter(names = {
            "--connectionCheck" }, description = "Set this option to enable an initial connection check performed by this wrapper application.", required = false)
    private boolean connectionCheckEnabled;

    public boolean isConnectionCheckEnabled() {
        return connectionCheckEnabled;
    }

    @Parameter(names = {
            "--maxNumberOfConnectionRetries" }, description = "Maximum number of times the wrapper tries to reach each URL. Including each URL constructed from the sechub includes.", required = false)
    private int maxNumberOfConnectionRetries = 3;

    public int getMaxNumberOfConnectionRetries() {
        return maxNumberOfConnectionRetries;
    }

    @Parameter(names = {
            "--retryWaittimeInMilliseconds" }, description = "Specify the time to wait between connection retries in milliseconds. The value cannot be less than 1000 milliseconds.", required = false)
    private int retryWaittimeInMilliseconds = 1000;

    public int getRetryWaittimeInMilliseconds() {
        if (retryWaittimeInMilliseconds < 1000) {
            retryWaittimeInMilliseconds = 1000;
        }
        return retryWaittimeInMilliseconds;
    }

    @Parameter(names = { "--pdsUserMessageFolder" }, description = "Folder where the user messages are written to. You can also set the environment variable "
            + EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER + ", instead of using this parameter.", required = false)
    private String pdsUserMessageFolder;

    public String getPDSUserMessageFolder() {
        return pdsUserMessageFolder;
    }

    @Parameter(names = {
            "--pdsEventFolder" }, description = "Folder where the ZAP wrapper listens for events of the PDS, like cancel requests for the current job. You can also set the environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER + ", instead of using this parameter.", required = false)
    private String pdsEventFolder;

    public String getPDSEventFolder() {
        return pdsEventFolder;
    }

    @Parameter(names = {
            "--groovyLoginScriptFile" }, description = "Groovy script file the ZAP wrapper uses for script based authentication when templates are defined. You can also set the environment variable "
                    + EnvironmentVariableConstants.ZAP_GROOVY_LOGIN_SCRIPT_FILE + ", instead of using this parameter.", required = false)
    private String groovyLoginScriptFile;

    public String getGroovyLoginScriptFile() {
        return groovyLoginScriptFile;
    }

    @Parameter(names = {
            "--pacFilePath" }, description = "PAC file the ZAP wrapper uses for script based authentication for the browsers profile, when templates are defined. You can also set the environment variable "
                    + EnvironmentVariableConstants.ZAP_LOGIN_PAC_FILE_PATH + ", instead of using this parameter.", required = false)
    private String pacFilePath;

    public String getPacFilePath() {
        return pacFilePath;
    }

    @Parameter(names = { "--no-headless" }, description = "Set this option to disable headless mode.", required = false)
    private boolean noHeadless;

    public boolean isNoHeadless() {
        return noHeadless;
    }
}
