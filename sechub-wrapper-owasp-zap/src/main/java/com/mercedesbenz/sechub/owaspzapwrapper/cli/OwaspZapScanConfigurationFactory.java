// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.integrationtest.TextFileReader;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapServerConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.BaseTargetUriFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubWebScanConfigurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

public class OwaspZapScanConfigurationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapScanConfigurationFactory.class);

    private SecHubWebScanConfigurationHelper sechubWebConfigHelper = new SecHubWebScanConfigurationHelper();
    private EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
    private BaseTargetUriFactory targetUriFactory = new BaseTargetUriFactory();

    public OwaspZapScanConfiguration create(CommandLineSettings settings) {
        /* Wrapper settings */
        OwaspZapServerConfiguration serverConfig = createOwaspZapServerConfig(settings);
        ProxyInformation proxyInformation = createProxyInformation(settings);

        /* SecHub settings */
        URI targetUri = targetUriFactory.create(settings.getTargetURL());
        SecHubWebScanConfiguration sechubWebConfig = createSecHubWebConfigFromSecHubConfigFile(settings);
        long maxScanDurationInMinutes = sechubWebConfigHelper.retrieveMaxScanDurationInMillis(sechubWebConfig);

        AuthenticationType authType = sechubWebConfigHelper.determineAuthenticationType(sechubWebConfig);

        /* we always use the SecHub job UUID as OWASP Zap context name */
        String contextName = settings.getJobUUID();
        if (contextName == null) {
            contextName = UUID.randomUUID().toString();
        }

        /* @formatter:off */
		OwaspZapScanConfiguration scanConfig = OwaspZapScanConfiguration.builder()
												.setTargetUri(targetUri)
												.setVerboseOutput(settings.isVerboseEnabled())
												.setReportFile(settings.getReportFile())
												.setContextName(contextName.toString())
												.setAjaxSpiderEnabled(settings.isAjaxSpiderEnabled())
												.setActiveScanEnabled(settings.isActiveScanEnabled())
												.setServerConfig(serverConfig)
												.setAuthenticationType(authType)
												.setMaxScanDurationinMillis(maxScanDurationInMinutes)
												.setSecHubWebScanConfiguration(sechubWebConfig)
												.setAdditionalProxyInformation(proxyInformation)
											  .build();
		/* @formatter:on */
        return scanConfig;
    }

    private SecHubWebScanConfiguration createSecHubWebConfigFromSecHubConfigFile(CommandLineSettings settings) {
        TextFileReader fileReader = new TextFileReader();
        String sechubConfigJSON = fileReader.loadTextFile(settings.getSecHubConfigFile());
        if (sechubConfigJSON != null) {
            SecHubScanConfiguration sechubConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJSON);
            return sechubConfig.getWebScan().get();
        }
        return new SecHubWebScanConfiguration();
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

        if (zapHost == null || zapPort <= 0 || zapApiKey == null) {
            throw new IllegalStateException(
                    "Owasp Zap server config was not set correctly. Please check the Owasp Zap Server environment variables/command line parameters.");
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
            LOG.info("No additional proxy was found. Continuing without addtional proxy...");
            return null;
        }
        return new ProxyInformation(proxyHost, proxyPort);
    }

}
