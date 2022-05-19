// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.CommandLineSettings;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.BaseTargetUriFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubWebScanConfigurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

public class OwaspZapScanConfigurationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapScanConfigurationFactory.class);

    SecHubWebScanConfigurationHelper sechubWebConfigHelper;
    EnvironmentVariableReader environmentVariableReader;
    BaseTargetUriFactory targetUriFactory;
    SechubWebConfigProvider webConfigProvider;
    RuleProvider ruleProvider;

    public OwaspZapScanConfigurationFactory() {
        sechubWebConfigHelper = new SecHubWebScanConfigurationHelper();
        environmentVariableReader = new EnvironmentVariableReader();
        targetUriFactory = new BaseTargetUriFactory();
        webConfigProvider = new SechubWebConfigProvider();
        ruleProvider = new RuleProvider();
    }

    public OwaspZapScanConfiguration create(CommandLineSettings settings) {
        if (settings == null) {
            throw new MustExitRuntimeException("Command line settings must not be null!", MustExitCode.COMMANDLINE_CONFIGURATION_INVALID);
        }
        /* Owasp Zap rule setup */
        OwaspZapFullRuleset fullRuleset = new OwaspZapFullRuleset();
    	DeactivatedRuleReferences deactivatedRuleReferences = new DeactivatedRuleReferences();
    	
        File fullRulesetFile = settings.getFullRulesetFile();
		File rulesDeactvationFile = settings.getRulesDeactvationFile();
		if (fullRulesetFile != null && rulesDeactvationFile != null) {
        	fullRuleset = ruleProvider.fetchFullRuleset(fullRulesetFile);
        	deactivatedRuleReferences = ruleProvider.fetchDeactivatedRuleReferences(rulesDeactvationFile);
        }

        /* Wrapper settings */
        OwaspZapServerConfiguration serverConfig = createOwaspZapServerConfig(settings);
        ProxyInformation proxyInformation = createProxyInformation(settings);

        /* SecHub settings */
        URI targetUri = targetUriFactory.create(settings.getTargetURL());
        SecHubWebScanConfiguration sechubWebConfig = webConfigProvider.getSecHubWebConfiguration(settings.getSecHubConfigFile());
        long maxScanDurationInMillis = sechubWebConfigHelper.fetchMaxScanDurationInMillis(sechubWebConfig);

        AuthenticationType authType = sechubWebConfigHelper.determineAuthenticationType(sechubWebConfig);

        /* we always use the SecHub job UUID as OWASP Zap context name */
        String contextName = settings.getJobUUID();
        if (contextName == null) {
            contextName = UUID.randomUUID().toString();
            LOG.warn("The job UUID was not set. Using randomly generated UUID: {} as fallback.", contextName);
        }

        /* @formatter:off */
		OwaspZapScanConfiguration scanConfig = OwaspZapScanConfiguration.builder()
												.setTargetUri(targetUri)
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
											  .build();
		/* @formatter:on */
        return scanConfig;
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
            throw new MustExitRuntimeException("Owasp Zap host is null. Please set the Owasp Zap host to the host use by the Owasp Zap.",
                    MustExitCode.ZAP_CONFIGURATION_INVALID);
        }

        if (zapPort <= 0) {
            throw new MustExitRuntimeException("Owasp Zap Port was set to " + zapPort + ". Please set the Owasp Zap port to the port used by the Owasp Zap.",
                    MustExitCode.ZAP_CONFIGURATION_INVALID);
        }
        if (zapApiKey == null) {
            throw new MustExitRuntimeException("Owasp Zap API-Key is null. Please set the Owasp Zap API-key to the same value set inside your Owasp Zap.",
                    MustExitCode.ZAP_CONFIGURATION_INVALID);
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

}
