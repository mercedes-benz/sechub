// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.*;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

public class ZapScanContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScanContextFactory.class);

    private final EnvironmentVariableReader environmentVariableReader;
    private final BaseTargetUriFactory targetUriFactory;
    private final RuleProvider ruleProvider;
    private final ZapWrapperDataSectionFileSupport dataSectionFileSupport;
    private final SecHubScanConfigProvider secHubScanConfigProvider;
    private final IncludeExcludeToZapURLHelper includeExcludeToZapURLHelper;

    public ZapScanContextFactory() {
        environmentVariableReader = new EnvironmentVariableReader();
        targetUriFactory = new BaseTargetUriFactory();
        ruleProvider = new RuleProvider();
        dataSectionFileSupport = new ZapWrapperDataSectionFileSupport();
        secHubScanConfigProvider = new SecHubScanConfigProvider();
        includeExcludeToZapURLHelper = new IncludeExcludeToZapURLHelper();
    }

    ZapScanContextFactory(EnvironmentVariableReader environmentVariableReader, BaseTargetUriFactory targetUriFactory, RuleProvider ruleProvider,
            ZapWrapperDataSectionFileSupport dataSectionFileSupport, SecHubScanConfigProvider secHubScanConfigProvider,
            IncludeExcludeToZapURLHelper includeExcludeToZapURLHelper) {
        this.environmentVariableReader = environmentVariableReader;
        this.targetUriFactory = targetUriFactory;
        this.ruleProvider = ruleProvider;
        this.dataSectionFileSupport = dataSectionFileSupport;
        this.secHubScanConfigProvider = secHubScanConfigProvider;
        this.includeExcludeToZapURLHelper = includeExcludeToZapURLHelper;
    }

    public ZapScanContext create(CommandLineSettings settings) {
        if (settings == null) {
            throw new ZapWrapperRuntimeException("Command line settings must not be null!", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        /* Zap rule setup */
        ZapFullRuleset fullRuleset = ruleProvider.fetchFullRuleset(settings.getFullRulesetFile());
        DeactivatedRuleReferences deactivatedRuleReferences = createDeactivatedRuleReferencesFromSettingsOrEnv(settings);

        DeactivatedRuleReferences ruleReferencesFromFile = ruleProvider.fetchDeactivatedRuleReferences(settings.getRulesDeactvationFile());
        for (RuleReference reference : ruleReferencesFromFile.getDeactivatedRuleReferences()) {
            deactivatedRuleReferences.addRuleReference(reference);
        }

        /* Wrapper settings */
        ZapServerConfiguration serverConfig = createZapServerConfig(settings);
        ProxyInformation proxyInformation = createProxyInformation(settings);

        /* SecHub settings */
        URL targetUrl = targetUriFactory.create(settings.getTargetURL());

        SecHubScanConfiguration sechubScanConfig = secHubScanConfigProvider.getSecHubWebConfiguration(settings.getSecHubConfigFile());
        SecHubWebScanConfiguration sechubWebConfig = getSecHubWebConfiguration(sechubScanConfig);

        List<File> apiDefinitionFiles = fetchApiDefinitionFiles(sechubScanConfig);

        File clientCertificateFile = fetchClientCertificateFile(sechubScanConfig);

        Map<String, File> headerValueFiles = fetchHeaderValueFiles(sechubScanConfig);

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
												.setFullRuleset(fullRuleset)
												.setDeactivatedRuleReferences(deactivatedRuleReferences)
												.addApiDefinitionFiles(apiDefinitionFiles)
												.setClientCertificateFile(clientCertificateFile)
												.addHeaderValueFiles(headerValueFiles)
												.addZapURLsIncludeSet(includeSet)
												.addZapURLsExcludeSet(excludeSet)
												.setConnectionCheckEnabled(settings.isConnectionCheckEnabled())
												.setMaxNumberOfConnectionRetries(settings.getMaxNumberOfConnectionRetries())
												.setRetryWaittimeInMilliseconds(settings.getRetryWaittimeInMilliseconds())
												.setZapProductMessageHelper(productMessagehelper)
												.setZapPDSEventHandler(zapEventHandler)
												.setGroovyScriptLoginFile(fetchGroovyScriptFile(settings))
											  .build();
		/* @formatter:on */
        return scanContext;
    }

    private DeactivatedRuleReferences createDeactivatedRuleReferencesFromSettingsOrEnv(CommandLineSettings settings) {
        LOG.info("Reading rules to deactivate from command line if set.");
        String deactivatedRuleRefsAsString = settings.getDeactivatedRuleReferences();

        // if no rules to deactivate were specified via the command line,
        // look for rules specified via the corresponding env variable
        if (deactivatedRuleRefsAsString == null) {
            LOG.info("Reading rules to deactivate from env variable {} if set.", EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES);
            deactivatedRuleRefsAsString = environmentVariableReader.readAsString(EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES);
        }

        // if no rules to deactivate were set at all, continue without
        if (deactivatedRuleRefsAsString == null) {
            LOG.info("Env variable {} was not set.", EnvironmentVariableConstants.ZAP_DEACTIVATED_RULE_REFERENCES);
            return new DeactivatedRuleReferences();
        }

        DeactivatedRuleReferences deactivatedRuleReferences = new DeactivatedRuleReferences();
        String[] deactivatedRuleRefs = deactivatedRuleRefsAsString.split(",");
        for (String ruleRef : deactivatedRuleRefs) {
            // The info is not needed here, it is only for the JSON file and meant to be
            // used as an additional description for the user
            String info = "";
            RuleReference ref = new RuleReference(ruleRef, info);
            deactivatedRuleReferences.addRuleReference(ref);
        }

        return deactivatedRuleReferences;
    }

    private ZapServerConfiguration createZapServerConfig(CommandLineSettings settings) {
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
            throw new ZapWrapperRuntimeException("Zap host is null. Please set the Zap host to the host use by the Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }

        if (zapPort <= 0) {
            throw new ZapWrapperRuntimeException("Zap Port was set to " + zapPort + ". Please set the Zap port to the port used by the Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (zapApiKey == null) {
            throw new ZapWrapperRuntimeException("Zap API-Key is null. Please set the Zap API-key to the same value set inside your Zap.",
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

    private SecHubWebScanConfiguration getSecHubWebConfiguration(SecHubScanConfiguration sechubConfig) {
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

    private ZapProductMessageHelper createZapProductMessageHelper(CommandLineSettings settings) {
        String userMessagesFolder = settings.getPDSUserMessageFolder();
        if (userMessagesFolder == null) {
            userMessagesFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER);
        }
        if (userMessagesFolder == null) {
            throw new ZapWrapperRuntimeException("PDS configuration invalid. Cannot send user messages, because environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER + " is not set.", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new ZapProductMessageHelper(userMessagesFolder);
    }

    private ZapPDSEventHandler createZapEventhandler(CommandLineSettings settings) {
        String pdsJobEventsFolder = settings.getPDSEventFolder();
        if (pdsJobEventsFolder == null) {
            pdsJobEventsFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER);
        }

        if (pdsJobEventsFolder == null) {
            throw new ZapWrapperRuntimeException("PDS configuration invalid. Cannot send check for job events, because environment variable "
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
}
