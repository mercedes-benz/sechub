// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.net.URI;
import java.nio.file.Path;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;

public class OwaspZapScanContext {
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

    private Path apiDefinitionFile;

    private OwaspZapScanContext() {
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

    /**
     *
     * @return api defintion file or <code>null</code> if not available
     */
    public Path getApiDefinitionFile() {
        return apiDefinitionFile;
    }

    public static OwaspZapBasicScanContextBuilder builder() {
        return new OwaspZapBasicScanContextBuilder();
    }

    public static class OwaspZapBasicScanContextBuilder {
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

        private Path apiDefinitionFile;

        public OwaspZapBasicScanContextBuilder setServerConfig(OwaspZapServerConfiguration serverConfig) {
            this.serverConfig = serverConfig;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setVerboseOutput(boolean verboseOutput) {
            this.verboseOutput = verboseOutput;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setAjaxSpiderEnabled(boolean ajaxSpiderEnabled) {
            this.ajaxSpiderEnabled = ajaxSpiderEnabled;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setActiveScanEnabled(boolean activeScanEnabled) {
            this.activeScanEnabled = activeScanEnabled;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setReportFile(Path reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setTargetUri(URI targetUri) {
            this.targetUri = targetUri;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setAuthenticationType(AuthenticationType authenticationType) {
            this.authenticationType = authenticationType;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setMaxScanDurationInMillis(long maxScanDurationInMillis) {
            this.maxScanDurationInMillis = maxScanDurationInMillis;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setSecHubWebScanConfiguration(SecHubWebScanConfiguration secHubWebScanConfiguration) {
            this.secHubWebScanConfiguration = secHubWebScanConfiguration;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setProxyInformation(ProxyInformation proxyInformation) {
            this.proxyInformation = proxyInformation;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setFullRuleset(OwaspZapFullRuleset fullRuleset) {
            this.fullRuleset = fullRuleset;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setDeactivatedRuleReferences(DeactivatedRuleReferences deactivatedRuleReferences) {
            this.deactivatedRuleReferences = deactivatedRuleReferences;
            return this;
        }

        public OwaspZapBasicScanContextBuilder setApiDefinitionFile(Path apiDefinitionFile) {
            this.apiDefinitionFile = apiDefinitionFile;
            return this;
        }

        public OwaspZapScanContext build() {
            OwaspZapScanContext owaspZapBasicScanConfiguration = new OwaspZapScanContext();
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

            owaspZapBasicScanConfiguration.apiDefinitionFile = this.apiDefinitionFile;

            return owaspZapBasicScanConfiguration;
        }
    }
}
