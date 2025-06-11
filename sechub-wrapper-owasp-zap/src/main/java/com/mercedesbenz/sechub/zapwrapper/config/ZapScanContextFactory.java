// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.helper.BaseTargetUriFactory;
import com.mercedesbenz.sechub.zapwrapper.helper.IncludeExcludeToZapURLHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapProductMessageHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapWrapperDataSectionFileSupport;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

public class ZapScanContextFactory {
    private static final int MAXIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES = 5;
    private static final int MINIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES = 0;

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

    public ZapScanContext create(ZapWrapperConfiguration configuration) throws ZapWrapperContextCreationException {
        if (configuration == null) {
            throw new ZapWrapperContextCreationException("Command line settings must not be null!", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        /* Wrapper settings */
        ZapServerConfiguration serverConfig = createZapServerConfig(configuration);
        ProxyInformation proxyInformation = createProxyInformation(configuration);

        int loginScriptFailureRetries = resolveLoginScriptFailureRetries();

        /* SecHub settings */
        URL targetUrl = targetUriFactory.create(configuration.getTargetURL());

        SecHubScanConfiguration sechubScanConfig = secHubScanConfigProvider.fetchSecHubScanConfiguration(configuration.getSecHubConfigFile(),
                environmentVariableReader);
        SecHubWebScanConfiguration sechubWebConfig = resolveSecHubWebConfiguration(sechubScanConfig);

        List<File> apiDefinitionFiles = fetchApiDefinitionFiles(sechubScanConfig);

        File clientCertificateFile = fetchClientCertificateFile(sechubScanConfig);

        Map<String, File> headerValueFiles = fetchHeaderValueFiles(sechubScanConfig);

        ZapProductMessageHelper productMessagehelper = createZapProductMessageHelper(configuration);

        File groovyScriptFile = fetchGroovyScriptFile(configuration);
        Map<String, String> templateVariables = fetchTemplateVariables(sechubScanConfig);
        assertValidScriptLoginConfiguration(groovyScriptFile, templateVariables, productMessagehelper);

        /* we always use the SecHub job UUID as Zap context name */
        String contextName = configuration.getJobUUID();
        if (contextName == null) {
            contextName = UUID.randomUUID().toString();
            LOG.warn("The job UUID was not set. Using randomly generated UUID: {} as fallback.", contextName);
        }

        Set<String> includeSet = createUrlsIncludedInContext(targetUrl, sechubWebConfig);
        Set<String> excludeSet = createUrlsExcludedFromContext(targetUrl, sechubWebConfig);

        ZapPDSEventHandler zapEventHandler = createZapEventhandler(configuration);

        /* @formatter:off */
        ZapScanContext scanContext = ZapScanContext.builder()
												.setTargetUrl(targetUrl)
												.setVerboseOutput(configuration.isVerboseEnabled())
												.setReportFile(configuration.getReportFile())
												.setContextName(contextName)
												.setAjaxSpiderEnabled(configuration.isAjaxSpiderEnabled())
												.setAjaxSpiderBrowserId(configuration.getAjaxSpiderBrowserId())
												.setActiveScanEnabled(configuration.isActiveScanEnabled())
												.setServerConfig(serverConfig)
												.setSecHubWebScanConfiguration(sechubWebConfig)
												.setProxyInformation(proxyInformation)
												.setZapRuleIDsToDeactivate(fetchZapRuleIDsToDeactivate(configuration))
												.setApiDefinitionFiles(apiDefinitionFiles)
												.setClientCertificateFile(clientCertificateFile)
												.setHeaderValueFiles(headerValueFiles)
												.setZapURLsIncludeSet(includeSet)
												.setZapURLsExcludeSet(excludeSet)
												.setConnectionCheckEnabled(configuration.isConnectionCheckEnabled())
												.setMaxNumberOfConnectionRetries(configuration.getMaxNumberOfConnectionRetries())
												.setRetryWaittimeInMilliseconds(configuration.getRetryWaittimeInMilliseconds())
												.setZapProductMessageHelper(productMessagehelper)
												.setZapPDSEventHandler(zapEventHandler)
												.setGroovyScriptLoginFile(groovyScriptFile)
												.setMaxGroovyScriptLoginFailureRetries(loginScriptFailureRetries)
												.setPacFilePath(fetchPacFilePath(configuration))
												.setNoHeadless(configuration.isNoHeadless())
											  .build();
		/* @formatter:on */
        return scanContext;
    }

    private int resolveLoginScriptFailureRetries() {
        int loginScriptFailureRetries = environmentVariableReader.readAsInt(EnvironmentVariableConstants.WRAPPER_LOGINSCRIPT_FAILURE_RETRIES);
        if (loginScriptFailureRetries > MAXIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES) {
            loginScriptFailureRetries = MAXIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES;
            LOG.warn("Configured login script failure retries was bigger than maximum: {} - set back to: {}", MAXIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES,
                    loginScriptFailureRetries);
        }
        if (loginScriptFailureRetries < MINIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES) {
            loginScriptFailureRetries = MINIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES;
            LOG.warn("Configured login script failure retries was smaler than minimum: {} - set back to: {}", MINIMUM_ACCEPTED_LOGINSCRIPT_FAILURE_RETRIES,
                    loginScriptFailureRetries);
        }
        return loginScriptFailureRetries;
    }

    private List<String> fetchZapRuleIDsToDeactivate(ZapWrapperConfiguration configuration) {
        List<String> deactivateRules = configuration.getDeactivateRules();

        if (!deactivateRules.isEmpty()) {
            return deactivateRules;
        }
        String zapRuleIDsToDeactivate = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES);
        if (zapRuleIDsToDeactivate == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(zapRuleIDsToDeactivate.split(","));
    }

    private ZapServerConfiguration createZapServerConfig(ZapWrapperConfiguration configuration) throws ZapWrapperContextCreationException {
        String zapHost = configuration.getZapHost();
        int zapPort = configuration.getZapPort();
        String zapApiKey = configuration.getZapApiKey();

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

    private ProxyInformation createProxyInformation(ZapWrapperConfiguration configuration) {
        String proxyHost = configuration.getProxyHost();
        int proxyPort = configuration.getProxyPort();
        String proxyRealm = configuration.getProxyRealm();
        String proxyUsername = configuration.getProxyUsername();
        String proxyPassword = configuration.getProxyPassword();

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

    private ZapProductMessageHelper createZapProductMessageHelper(ZapWrapperConfiguration configuration) throws ZapWrapperContextCreationException {
        String userMessagesFolder = configuration.getPDSUserMessageFolder();
        if (userMessagesFolder == null) {
            userMessagesFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER);
        }
        if (userMessagesFolder == null) {
            throw new ZapWrapperContextCreationException("PDS configuration invalid. Cannot send user messages, because environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER + " is not set.", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new ZapProductMessageHelper(userMessagesFolder);
    }

    private ZapPDSEventHandler createZapEventhandler(ZapWrapperConfiguration configuration) throws ZapWrapperContextCreationException {
        String pdsJobEventsFolder = configuration.getPDSEventFolder();
        if (pdsJobEventsFolder == null) {
            pdsJobEventsFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER);
        }

        if (pdsJobEventsFolder == null) {
            throw new ZapWrapperContextCreationException("PDS configuration invalid. Cannot send check for job events, because environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER + " is not set.", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new ZapPDSEventHandler(pdsJobEventsFolder);
    }

    private File fetchGroovyScriptFile(ZapWrapperConfiguration configuration) {
        String groovyScriptFile = configuration.getGroovyLoginScriptFile();
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
     * @param groovyScriptFile  Groovy script file
     * @param templateVariables Template variables
     *
     * @throws ZapWrapperContextCreationException
     *
     */
    private void assertValidScriptLoginConfiguration(File groovyScriptFile, Map<String, String> templateVariables, ZapProductMessageHelper messageHelper)
            throws ZapWrapperContextCreationException {
        // no script login was defined
        if (groovyScriptFile == null && templateVariables.isEmpty()) {
            return;
        }
        // no script found, but template data were configured
        if (groovyScriptFile == null && !templateVariables.isEmpty()) {
            SecHubMessage secHubMessage = new SecHubMessage(SecHubMessageType.ERROR,
                    "The SecHub configuration file contains a template data section, but no template was loaded for the scan. If you are no SecHub administrator get in touch with your Sechub contact to verify your project is assigned with the login template.");
            messageHelper.writeSingleProductMessage(secHubMessage);
            throw new ZapWrapperContextCreationException(
                    "The SecHub configuration file contains a template data section, but no template was loaded for the scan.",
                    ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        // A script was defined, but no template data where defined
        if (groovyScriptFile != null && templateVariables.isEmpty()) {
            throw new ZapWrapperContextCreationException(
                    "When a groovy login script is defined, some variables - e.g. for username, password etc - must be set inside webscan template data. But found nothing.",
                    ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
    }

    /**
     * This method returns a PAC file for script based authentication. If a file is
     * specified it must exist on the filesystem. As always, command line parameters
     * take precedence over environment variables.
     *
     * @param configuration
     * @return a file that exists on the filesystem or <code>null</code> if nothing
     *         was specified.
     *
     *
     * @throws ZapWrapperContextCreationException in case the specified file does
     *                                            not exist on the filesystem
     */
    private File fetchPacFilePath(ZapWrapperConfiguration configuration) throws ZapWrapperContextCreationException {
        String pacFilePath = configuration.getPacFilePath();

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
