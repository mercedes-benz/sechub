// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.BaseTargetUriFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.IncludeExcludeToOwaspZapURLHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapProductMessageHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapURLType;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubWebScanConfigurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

public class OwaspZapScanContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapScanContextFactory.class);

    SecHubWebScanConfigurationHelper sechubWebConfigHelper;
    EnvironmentVariableReader environmentVariableReader;
    BaseTargetUriFactory targetUriFactory;
    RuleProvider ruleProvider;
    ApiDefinitionFileProvider apiDefinitionFileProvider;
    SecHubScanConfigProvider secHubScanConfigProvider;
    IncludeExcludeToOwaspZapURLHelper includeExcludeToOwaspZapURLHelper;

    public OwaspZapScanContextFactory() {
        sechubWebConfigHelper = new SecHubWebScanConfigurationHelper();
        environmentVariableReader = new EnvironmentVariableReader();
        targetUriFactory = new BaseTargetUriFactory();
        ruleProvider = new RuleProvider();
        apiDefinitionFileProvider = new ApiDefinitionFileProvider();
        secHubScanConfigProvider = new SecHubScanConfigProvider();
        includeExcludeToOwaspZapURLHelper = new IncludeExcludeToOwaspZapURLHelper();
    }

    public OwaspZapScanContext create(CommandLineSettings settings) {
        if (settings == null) {
            throw new ZapWrapperRuntimeException("Command line settings must not be null!", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        /* Owasp Zap rule setup */
        OwaspZapFullRuleset fullRuleset = ruleProvider.fetchFullRuleset(settings.getFullRulesetFile());
        DeactivatedRuleReferences deactivatedRuleReferences = createDeactivatedRuleReferencesFromSettingsOrEnv(settings);

        DeactivatedRuleReferences ruleReferencesFromFile = ruleProvider.fetchDeactivatedRuleReferences(settings.getRulesDeactvationFile());
        for (RuleReference reference : ruleReferencesFromFile.getDeactivatedRuleReferences()) {
            deactivatedRuleReferences.addRuleReference(reference);
        }

        /* Wrapper settings */
        OwaspZapServerConfiguration serverConfig = createOwaspZapServerConfig(settings);
        ProxyInformation proxyInformation = createProxyInformation(settings);

        /* SecHub settings */
        URL targetUrl = targetUriFactory.create(settings.getTargetURL());

        SecHubScanConfiguration sechubScanConfig = secHubScanConfigProvider.getSecHubWebConfiguration(settings.getSecHubConfigFile());
        SecHubWebScanConfiguration sechubWebConfig = getSecHubWebConfiguration(sechubScanConfig);
        long maxScanDurationInMillis = sechubWebConfigHelper.fetchMaxScanDurationInMillis(sechubWebConfig);

        AuthenticationType authType = sechubWebConfigHelper.determineAuthenticationType(sechubWebConfig);

        Path apiDefinitionFile = createPathToApiDefinitionFileOrNull(sechubScanConfig);

        /* we always use the SecHub job UUID as OWASP Zap context name */
        String contextName = settings.getJobUUID();
        if (contextName == null) {
            contextName = UUID.randomUUID().toString();
            LOG.warn("The job UUID was not set. Using randomly generated UUID: {} as fallback.", contextName);
        }

        List<SecHubMessage> userMessages = new LinkedList<>();
        Set<URL> includeSet = createUrlsIncludedInContext(targetUrl, sechubWebConfig, userMessages);
        Set<URL> excludeSet = createUrlsExcludedFromContext(targetUrl, sechubWebConfig, userMessages);

        String userMessagesFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER);
        if (userMessagesFolder == null) {
            throw new IllegalStateException(
                    "PDS configuration invalid. Cannot send user messages, because environment variable PDS_JOB_USER_MESSAGES_FOLDER is not set.");
        }
        OwaspZapProductMessageHelper productMessagehelper = new OwaspZapProductMessageHelper(userMessagesFolder);
        checkForIncludeExcludeErrors(userMessages, productMessagehelper);

        /* @formatter:off */
		OwaspZapScanContext scanContext = OwaspZapScanContext.builder()
												.setTargetUrl(targetUrl)
												.setVerboseOutput(settings.isVerboseEnabled())
												.setReportFile(settings.getReportFile())
												.setContextName(contextName)
												.setAjaxSpiderEnabled(settings.isAjaxSpiderEnabled())
												.setActiveScanEnabled(settings.isActiveScanEnabled())
												.setServerConfig(serverConfig)
												.setAuthenticationType(authType)
												.setMaxScanDurationInMillis(maxScanDurationInMillis)
												.setSecHubWebScanConfiguration(sechubWebConfig)
												.setProxyInformation(proxyInformation)
												.setFullRuleset(fullRuleset)
												.setDeactivatedRuleReferences(deactivatedRuleReferences)
												.setApiDefinitionFile(apiDefinitionFile)
												.setOwaspZapURLsIncludeSet(includeSet)
												.setOwaspZapURLsExcludeSet(excludeSet)
												.setConnectionCheckEnabled(settings.isConnectionCheckEnabled())
												.setMaxNumberOfConnectionRetries(settings.getMaxNumberOfConnectionRetries())
												.setRetryWaittimeInMilliseconds(settings.getRetryWaittimeInMilliseconds())
												.setOwaspZapProductMessageHelper(productMessagehelper)
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

    private OwaspZapServerConfiguration createOwaspZapServerConfig(CommandLineSettings settings) {
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
            throw new ZapWrapperRuntimeException("Owasp Zap host is null. Please set the Owasp Zap host to the host use by the Owasp Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }

        if (zapPort <= 0) {
            throw new ZapWrapperRuntimeException("Owasp Zap Port was set to " + zapPort + ". Please set the Owasp Zap port to the port used by the Owasp Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (zapApiKey == null) {
            throw new ZapWrapperRuntimeException("Owasp Zap API-Key is null. Please set the Owasp Zap API-key to the same value set inside your Owasp Zap.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return new OwaspZapServerConfiguration(zapHost, zapPort, zapApiKey);
    }

    private ProxyInformation createProxyInformation(CommandLineSettings settings) {
        String proxyHost = settings.getProxyHost();
        int proxyPort = settings.getProxyPort();

        if (proxyHost == null) {
            proxyHost = environmentVariableReader.readAsString(EnvironmentVariableConstants.PROXY_HOST_ENV_VARIABLE_NAME);
        }
        if (proxyPort <= 0) {
            proxyPort = environmentVariableReader.readAsInt(EnvironmentVariableConstants.PROXY_PORT_ENV_VARIABLE_NAME);
        }

        if (proxyHost == null || proxyPort <= 0) {
            LOG.info("No proxy settings were provided. Continuing without proxy...");
            return null;
        }
        return new ProxyInformation(proxyHost, proxyPort);
    }

    private SecHubWebScanConfiguration getSecHubWebConfiguration(SecHubScanConfiguration sechubConfig) {
        if (!sechubConfig.getWebScan().isPresent()) {
            return new SecHubWebScanConfiguration();
        }
        return sechubConfig.getWebScan().get();
    }

    private Path createPathToApiDefinitionFileOrNull(SecHubScanConfiguration sechubScanConfig) {
        // use the extracted sources folder path, where all text files are uploaded and
        // extracted
        String extractedSourcesFolderPath = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EXTRACTED_SOURCES_FOLDER);
        return apiDefinitionFileProvider.fetchApiDefinitionFile(extractedSourcesFolderPath, sechubScanConfig);
    }

    private Set<URL> createUrlsIncludedInContext(URL targetUrl, SecHubWebScanConfiguration sechubWebConfig, List<SecHubMessage> userMessages) {
        Set<URL> includeSet = new HashSet<>();
        includeSet.add(targetUrl);
        if (sechubWebConfig.getIncludes().isPresent()) {
            includeSet.addAll(
                    includeExcludeToOwaspZapURLHelper.createListOfUrls(OwaspZapURLType.INCLUDE, targetUrl, sechubWebConfig.getIncludes().get(), userMessages));
        }
        return includeSet;
    }

    private Set<URL> createUrlsExcludedFromContext(URL targetUrl, SecHubWebScanConfiguration sechubWebConfig, List<SecHubMessage> userMessages) {
        Set<URL> excludeSet = new HashSet<>();
        if (sechubWebConfig.getExcludes().isPresent()) {
            excludeSet.addAll(
                    includeExcludeToOwaspZapURLHelper.createListOfUrls(OwaspZapURLType.EXCLUDE, targetUrl, sechubWebConfig.getExcludes().get(), userMessages));
        }
        return excludeSet;
    }

    private void checkForIncludeExcludeErrors(List<SecHubMessage> userMessages, OwaspZapProductMessageHelper productMessageHelper) {
        if (userMessages == null) {
            return;
        }
        if (userMessages.isEmpty()) {
            return;
        }
        productMessageHelper.writeProductMessages(userMessages);
    }
}
