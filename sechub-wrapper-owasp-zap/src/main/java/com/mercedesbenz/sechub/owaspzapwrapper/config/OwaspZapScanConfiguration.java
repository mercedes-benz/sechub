// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.net.URI;
import java.nio.file.Path;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;

public class OwaspZapScanConfiguration {
    private OwaspZapServerConfiguration serverConfig;
    private boolean verboseOutput = false;

    private boolean ajaxSpiderEnabled;
    private boolean activeScanEnabled;

    private Path reportFile;

    private String contextName;

    private URI targetUri;

    private AuthenticationType authenticationType;

    private long maxScanDurationInMillis;

    private SecHubWebScanConfiguration secHubWebScanConfiguration;

    private ProxyInformation proxyInformation;

    private OwaspZapFullRuleset fullRuleset;

    private DeactivatedRuleReferences deactivatedRuleReferences;

    private OwaspZapScanConfiguration() {
    }

    public OwaspZapServerConfiguration getServerConfig() {
        return serverConfig;
    }

    public boolean isVerboseOutput() {
        return verboseOutput;
    }

    public boolean isAjaxSpiderEnabled() {
        return ajaxSpiderEnabled;
    }

    public boolean isActiveScanEnabled() {
        return activeScanEnabled;
    }

    public Path getReportFile() {
        return reportFile;
    }

    public String getContextName() {
        return contextName;
    }

    public String getTargetUriAsString() {
        return getTargetUri().toString();
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public long getMaxScanDurationInMillis() {
        return maxScanDurationInMillis;
    }

    public SecHubWebScanConfiguration getSecHubWebScanConfiguration() {
        return secHubWebScanConfiguration;
    }

    /**
     * Resolves proxy information if available
     *
     * @return proxy information or <code>null</code> when no proxy information
     *         available
     */
    public ProxyInformation getProxyInformation() {
        return proxyInformation;
    }

    public OwaspZapFullRuleset getFullRuleset() {
        return fullRuleset;
    }

    public DeactivatedRuleReferences getDeactivatedRuleReferences() {
        return deactivatedRuleReferences;
    }

    public static OwaspZapBasicScanConfigurationBuilder builder() {
        return new OwaspZapBasicScanConfigurationBuilder();
    }

    public static class OwaspZapBasicScanConfigurationBuilder {
        private OwaspZapServerConfiguration serverConfig;

        private boolean verboseOutput = false;

        private boolean ajaxSpiderEnabled;
        private boolean activeScanEnabled;

        private Path reportFile;

        private String contextName;

        private URI targetUri;

        private AuthenticationType authenticationType;

        private long maxScanDurationInMillis;

        private SecHubWebScanConfiguration secHubWebScanConfiguration;

        private ProxyInformation proxyInformation;

        private OwaspZapFullRuleset fullRuleset;

        private DeactivatedRuleReferences deactivatedRuleReferences;

        public OwaspZapBasicScanConfigurationBuilder setServerConfig(OwaspZapServerConfiguration serverConfig) {
            this.serverConfig = serverConfig;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setVerboseOutput(boolean verboseOutput) {
            this.verboseOutput = verboseOutput;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setAjaxSpiderEnabled(boolean ajaxSpiderEnabled) {
            this.ajaxSpiderEnabled = ajaxSpiderEnabled;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setActiveScanEnabled(boolean activeScanEnabled) {
            this.activeScanEnabled = activeScanEnabled;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setReportFile(Path reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setTargetUri(URI targetUri) {
            this.targetUri = targetUri;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setAuthenticationType(AuthenticationType authenticationType) {
            this.authenticationType = authenticationType;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setMaxScanDurationInMillis(long maxScanDurationInMillis) {
            this.maxScanDurationInMillis = maxScanDurationInMillis;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setSecHubWebScanConfiguration(SecHubWebScanConfiguration secHubWebScanConfiguration) {
            this.secHubWebScanConfiguration = secHubWebScanConfiguration;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setProxyInformation(ProxyInformation proxyInformation) {
            this.proxyInformation = proxyInformation;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setFullRuleset(OwaspZapFullRuleset fullRuleset) {
            this.fullRuleset = fullRuleset;
            return this;
        }

        public OwaspZapBasicScanConfigurationBuilder setDeactivatedRuleReferences(DeactivatedRuleReferences deactivatedRuleReferences) {
            this.deactivatedRuleReferences = deactivatedRuleReferences;
            return this;
        }

        public OwaspZapScanConfiguration build() {
            OwaspZapScanConfiguration owaspZapBasicScanConfiguration = new OwaspZapScanConfiguration();
            owaspZapBasicScanConfiguration.serverConfig = this.serverConfig;
            owaspZapBasicScanConfiguration.verboseOutput = this.verboseOutput;
            owaspZapBasicScanConfiguration.ajaxSpiderEnabled = this.ajaxSpiderEnabled;
            owaspZapBasicScanConfiguration.activeScanEnabled = this.activeScanEnabled;
            owaspZapBasicScanConfiguration.reportFile = this.reportFile;
            owaspZapBasicScanConfiguration.contextName = this.contextName;
            owaspZapBasicScanConfiguration.targetUri = this.targetUri;
            owaspZapBasicScanConfiguration.authenticationType = this.authenticationType;

            owaspZapBasicScanConfiguration.maxScanDurationInMillis = this.maxScanDurationInMillis;

            owaspZapBasicScanConfiguration.secHubWebScanConfiguration = this.secHubWebScanConfiguration;

            owaspZapBasicScanConfiguration.proxyInformation = this.proxyInformation;

            owaspZapBasicScanConfiguration.fullRuleset = this.fullRuleset;
            owaspZapBasicScanConfiguration.deactivatedRuleReferences = this.deactivatedRuleReferences;

            return owaspZapBasicScanConfiguration;
        }
    }
}
