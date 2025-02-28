// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.zapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.helper.*;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

public class ZapScanContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScanContextFactory.class);

    private final EnvironmentVariableReader environmentVariableReader;
    private final BaseTargetUriFactory targetUriFactory;
    private final ZapWrapperDataSectionFileSupport dataSectionFileSupport;
    private final SecHubScanConfigProvider secHubScanConfigProvider;
    private final IncludeExcludeToZapURLHelper includeExcludeToZapURLHelper;

    public ZapScanContextFactory() {
        environmentVariableReader = new EnvironmentVariableReader();
        targetUriFactory = new BaseTargetUriFactory();
        dataSectionFileSupport = new ZapWrapperDataSectionFileSupport();
        secHubScanConfigProvider = new SecHubScanConfigProvider();
        includeExcludeToZapURLHelper = new IncludeExcludeToZapURLHelper();
    }

    ZapScanContextFactory(EnvironmentVariableReader environmentVariableReader, BaseTargetUriFactory targetUriFactory,
            ZapWrapperDataSectionFileSupport dataSectionFileSupport, SecHubScanConfigProvider secHubScanConfigProvider,
            IncludeExcludeToZapURLHelper includeExcludeToZapURLHelper) {
        this.environmentVariableReader = environmentVariableReader;
        this.targetUriFactory = targetUriFactory;
        this.dataSectionFileSupport = dataSectionFileSupport;
        this.secHubScanConfigProvider = secHubScanConfigProvider;
        this.includeExcludeToZapURLHelper = includeExcludeToZapURLHelper;
    }

    public ZapScanContext create(CommandLineSettings settings) throws ZapWrapperContextCreationException {
        if (settings == null) {
            throw new ZapWrapperContextCreationException("Command line settings must not be null!", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        /* Wrapper settings */
        ZapServerConfiguration serverConfig = createZapServerConfig(settings);
        ProxyInformation proxyInformation = createProxyInformation(settings);

        /* SecHub settings */
        URL targetUrl = targetUriFactory.create(settings.getTargetURL());

        SecHubScanConfiguration sechubScanConfig = secHubScanConfigProvider.fetchSecHubScanConfiguration(settings.getSecHubConfigFile(),
                environmentVariableReader);
        SecHubWebScanConfiguration sechubWebConfig = resolveSecHubWebConfiguration(sechubScanConfig);

        List<File> apiDefinitionFiles = fetchApiDefinitionFiles(sechubScanConfig);

        File clientCertificateFile = fetchClientCertificateFile(sechubScanConfig);

        Map<String, File> headerValueFiles = fetchHeaderValueFiles(sechubScanConfig);

        File groovyScriptFile = fetchGroovyScriptFile(settings);
        Map<String, String> templateVariables = fetchTemplateVariables(sechubScanConfig);
        assertValidScriptLoginConfiguration(groovyScriptFile, templateVariables);

        /* we always use the SecHub job UUID as Zap context name */
        String contextName = settings.getJobUUID();
        if (contextName == null) {
            contextName = UUID.randomUUID().toString();
            LOG.warn("The job UUID was not set. Using randomly generated UUID: {} as fallback.", contextName);
        }

        Set<String> includeSet = createUrlsIncludedInContext(targetUrl, sechubWebConfig);
        Set<String> excludeSet = createUrlsExcludedFromContext(targetUrl, sechubWebConfig);

        ZapProductMessageHelper productMessagehelper = createZapProductMessageHelper(settings);
        ZapPDSEventHandler zapEventHandler = createZapEventhandler(settings);

        /* @formatter:off */
        ZapScanContext scanContext = ZapScanContext.builder()
												.setTargetUrl(targetUrl)
												.setVerboseOutput(settings.isVerboseEnabled())
												.setReportFile(settings.getReportFile())
												.setContextName(contextName)
												.setAjaxSpiderEnabled(settings.isAjaxSpiderEnabled())
												.setAjaxSpiderBrowserId(settings.getAjaxSpiderBrowserId())
												.setActiveScanEnabled(settings.isActiveScanEnabled())
												.setServerConfig(serverConfig)
												.setSecHubWebScanConfiguration(sechubWebConfig)
												.setProxyInformation(proxyInformation)
												.setZapRuleIDsToDeactivate(fetchZapRuleIDsToDeactivate(settings))
												.setApiDefinitionFiles(apiDefinitionFiles)
												.setClientCertificateFile(clientCertificateFile)
												.setHeaderValueFiles(headerValueFiles)
												.setZapURLsIncludeSet(includeSet)
												.setZapURLsExcludeSet(excludeSet)
												.setConnectionCheckEnabled(settings.isConnectionCheckEnabled())
												.setMaxNumberOfConnectionRetries(settings.getMaxNumberOfConnectionRetries())
												.setRetryWaittimeInMilliseconds(settings.getRetryWaittimeInMilliseconds())
												.setZapProductMessageHelper(productMessagehelper)
												.setZapPDSEventHandler(zapEventHandler)
												.setGroovyScriptLoginFile(groovyScriptFile)
												.setTemplateVariables(templateVariables)
												.setPacFilePath(fetchPacFilePath(settings))
												.setNoHeadless(settings.isNoHeadless())
											  .build();
		/* @formatter:on */
        return scanContext;
    }

    private List<String> fetchZapRuleIDsToDeactivate(CommandLineSettings settings) {
        List<String> deactivateRules = settings.getDeactivateRules();

        if (!deactivateRules.isEmpty()) {
            return deactivateRules;
        }
        String zapRuleIDsToDeactivate = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES);
        if (zapRuleIDsToDeactivate == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(zapRuleIDsToDeactivate.split(","));
    }

    private ZapServerConfiguration createZapServerConfig(CommandLineSettings settings) throws ZapWrapperContextCreationException {
        String zapHost = settings.getZapHost();
        int zapPort = settings.getZapPort();
        String zapApiKey = settings.getZapApiKey();

        if (zapHost == null) {
            zapHost = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_HOST_ENV_VARIABLE_NAME);
        }
        if (zapPort <= 0) {
            zapPort = environmentVariableReader.readAsInt(EnvironmentVariableConstants.ZAP_PORT_ENV_VARIABLE_NAME);
        }
        if (zapApiKey == null) {
            zapApiKey = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_API_KEY_ENV_VARIABLE_NAME);
        }

        if (zapHost == null) {
            throw new ZapWrapperContextCreationException("Zap host is null. Please set the Zap host to the host use by the Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }

        if (zapPort <= 0) {
            throw new ZapWrapperContextCreationException("Zap Port was set to " + zapPort + ". Please set the Zap port to the port used by the Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (zapApiKey == null) {
            throw new ZapWrapperContextCreationException("Zap API-Key is null. Please set the Zap API-key to the same value set inside your Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new ZapServerConfiguration(zapHost, zapPort, zapApiKey);
    }

    private ProxyInformation createProxyInformation(CommandLineSettings settings) {
        String proxyHost = settings.getProxyHost();
        int proxyPort = settings.getProxyPort();
        String proxyRealm = settings.getProxyRealm();
        String proxyUsername = settings.getProxyUsername();
        String proxyPassword = settings.getProxyPassword();

        if (proxyHost == null) {
            proxyHost = environmentVariableReader.readAsString(EnvironmentVariableConstants.PROXY_HOST_ENV_VARIABLE_NAME);
        }
        if (proxyPort <= 0) {
            proxyPort = environmentVariableReader.readAsInt(EnvironmentVariableConstants.PROXY_PORT_ENV_VARIABLE_NAME);
        }
        // optional values
        if (proxyRealm == null) {
            proxyRealm = environmentVariableReader.readAsString(EnvironmentVariableConstants.PROXY_REALM_ENV_VARIABLE_NAME);
        }
        if (proxyUsername == null) {
            proxyUsername = environmentVariableReader.readAsString(EnvironmentVariableConstants.PROXY_USERNAME_ENV_VARIABLE_NAME);
        }
        if (proxyPassword == null) {
            proxyPassword = environmentVariableReader.readAsString(EnvironmentVariableConstants.PROXY_PASSWORD_ENV_VARIABLE_NAME);
        }

        if (proxyHost == null || proxyPort <= 0) {
            LOG.info("No proxy settings were provided. Continuing without proxy...");
            return null;
        }
        /* @formatter:off */
        return ProxyInformation.builder()
                               .setHost(proxyHost)
                               .setPort(proxyPort)
                               .setRealm(proxyRealm)
                               .setUsername(proxyUsername)
                               .setPassword(proxyPassword)
                               .build();
        /* @formatter:on */
    }

    private SecHubWebScanConfiguration resolveSecHubWebConfiguration(SecHubScanConfiguration sechubConfig) {
        if (!sechubConfig.getWebScan().isPresent()) {
            return new SecHubWebScanConfiguration();
        }
        return sechubConfig.getWebScan().get();
    }

    private List<File> fetchApiDefinitionFiles(SecHubScanConfiguration sechubScanConfig) {
        // use the extracted sources folder path, where all text files are uploaded and
        // extracted
        String extractedSourcesFolderPath = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EXTRACTED_SOURCES_FOLDER);
        return dataSectionFileSupport.fetchApiDefinitionFiles(extractedSourcesFolderPath, sechubScanConfig);
    }

    private File fetchClientCertificateFile(SecHubScanConfiguration sechubScanConfig) {
        // use the extracted sources folder path, where all text files are uploaded and
        // extracted
        String extractedSourcesFolderPath = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EXTRACTED_SOURCES_FOLDER);
        return dataSectionFileSupport.fetchClientCertificateFile(extractedSourcesFolderPath, sechubScanConfig);
    }

    private Map<String, File> fetchHeaderValueFiles(SecHubScanConfiguration sechubScanConfig) {
        // use the extracted sources folder path, where all text files are uploaded and
        // extracted
        String extractedSourcesFolderPath = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EXTRACTED_SOURCES_FOLDER);
        return dataSectionFileSupport.fetchHeaderValueFiles(extractedSourcesFolderPath, sechubScanConfig);
    }

    private Set<String> createUrlsIncludedInContext(URL targetUrl, SecHubWebScanConfiguration sechubWebConfig) {
        Set<String> includeSet = new HashSet<>();
        if (sechubWebConfig.getIncludes().isPresent()) {
            includeSet.addAll(includeExcludeToZapURLHelper.createListOfUrls(targetUrl, sechubWebConfig.getIncludes().get()));
        }
        // if no includes are specified everything is included
        if (includeSet.isEmpty()) {
            includeSet.add(targetUrl + UrlUtil.REGEX_PATTERN_WILDCARD_STRING);
        }
        // needed as entry point to start the scan
        includeSet.add(targetUrl.toString());
        return includeSet;
    }

    private Set<String> createUrlsExcludedFromContext(URL targetUrl, SecHubWebScanConfiguration sechubWebConfig) {
        Set<String> excludeSet = new HashSet<>();
        if (sechubWebConfig.getExcludes().isPresent()) {
            excludeSet.addAll(includeExcludeToZapURLHelper.createListOfUrls(targetUrl, sechubWebConfig.getExcludes().get()));
        }
        return excludeSet;
    }

    private ZapProductMessageHelper createZapProductMessageHelper(CommandLineSettings settings) throws ZapWrapperContextCreationException {
        String userMessagesFolder = settings.getPDSUserMessageFolder();
        if (userMessagesFolder == null) {
            userMessagesFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER);
        }
        if (userMessagesFolder == null) {
            throw new ZapWrapperContextCreationException("PDS configuration invalid. Cannot send user messages, because environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER + " is not set.", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new ZapProductMessageHelper(userMessagesFolder);
    }

    private ZapPDSEventHandler createZapEventhandler(CommandLineSettings settings) throws ZapWrapperContextCreationException {
        String pdsJobEventsFolder = settings.getPDSEventFolder();
        if (pdsJobEventsFolder == null) {
            pdsJobEventsFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER);
        }

        if (pdsJobEventsFolder == null) {
            throw new ZapWrapperContextCreationException("PDS configuration invalid. Cannot send check for job events, because environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER + " is not set.", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new ZapPDSEventHandler(pdsJobEventsFolder);
    }

    private File fetchGroovyScriptFile(CommandLineSettings settings) {
        String groovyScriptFile = settings.getGroovyLoginScriptFile();
        if (groovyScriptFile == null) {
            groovyScriptFile = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_GROOVY_LOGIN_SCRIPT_FILE);
        }
        if (groovyScriptFile == null) {
            return null;
        }
        return new File(groovyScriptFile);
    }

    private Map<String, String> fetchTemplateVariables(SecHubScanConfiguration sechubScanConfig) {
        TemplateDataResolver templateDataResolver = new TemplateDataResolver();
        TemplateData templateData = templateDataResolver.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, sechubScanConfig);
        if (templateData == null) {
            return new LinkedHashMap<>();
        }
        return templateData.getVariables();
    }

    /**
     * This method verifies that the script login configuration is valid. No script
     * login configured is a valid configuration as well.
     *
     * @param groovyScriptFile
     * @param templateVariables
     * @throws ZapWrapperContextCreationException
     *
     */
    private void assertValidScriptLoginConfiguration(File groovyScriptFile, Map<String, String> templateVariables) throws ZapWrapperContextCreationException {
        // no script login was defined
        if (groovyScriptFile == null && templateVariables.isEmpty()) {
            return;
        }
        // A script was defined, but no template data where defined
        if (groovyScriptFile != null && templateVariables.isEmpty()) {
            throw new ZapWrapperContextCreationException(
                    "When a groovy login script is defined, the variables: '" + ZapTemplateDataVariableKeys.USERNAME_KEY + "' and '"
                            + ZapTemplateDataVariableKeys.PASSWORD_KEY + "' must be set inside webscan template data!",
                    ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        // No script was defined, but template data where defined
        if (groovyScriptFile == null && !templateVariables.isEmpty()) {
            throw new ZapWrapperContextCreationException("When no groovy login script is defined, no template data variables must be defined!",
                    ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        // if a script and the template data are defined, the mandatory variables must
        // be present
        if (groovyScriptFile != null && !templateVariables.isEmpty()) {
            if (templateVariables.get(ZapTemplateDataVariableKeys.USERNAME_KEY) == null
                    || templateVariables.get(ZapTemplateDataVariableKeys.PASSWORD_KEY) == null) {
                throw new ZapWrapperContextCreationException(
                        "For script authentication webscans using templates, the variables: '" + ZapTemplateDataVariableKeys.USERNAME_KEY + "' and '"
                                + ZapTemplateDataVariableKeys.PASSWORD_KEY + "' must be set inside webscan template data!",
                        ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
            }
        }
    }

    /**
     * This method returns a PAC file for script based authentication. If a file is
     * specified it must exist on the filesystem. As always, command line parameters
     * take precedence over environment variables.
     *
     * @param settings
     * @return a file that exists on the filesystem or <code>null</code> if nothing
     *         was specified.
     *
     *
     * @throws ZapWrapperContextCreationException in case the specified file does
     *                                            not exist on the filesystem
     */
    private File fetchPacFilePath(CommandLineSettings settings) throws ZapWrapperContextCreationException {
        String pacFilePath = settings.getPacFilePath();

        if (pacFilePath == null) {
            pacFilePath = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_LOGIN_PAC_FILE_PATH);
        }
        if (pacFilePath == null) {
            return null;
        }
        File pacFile = new File(pacFilePath);
        if (!pacFile.isFile()) {
            throw new ZapWrapperContextCreationException(
                    "A pac file was specified for script login, that does not exist on the filesystem!\n:Pac file path was:  " + pacFilePath,
                    ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        return pacFile;
    }

}
