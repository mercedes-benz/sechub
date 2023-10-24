// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.zapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapProductMessageHelper;

public class ZapScanContext {
    private ZapServerConfiguration serverConfig;
    private boolean verboseOutput = false;

    private boolean ajaxSpiderEnabled;
    private boolean activeScanEnabled;

    private Path reportFile;

    private String contextName;

    private URL targetUrl;

    private AuthenticationType authenticationType;

    private long maxScanDurationInMilliSeconds;

    private SecHubWebScanConfiguration secHubWebScanConfiguration;

    private ProxyInformation proxyInformation;

    private ZapFullRuleset fullRuleset;

    private DeactivatedRuleReferences deactivatedRuleReferences;

    private List<File> apiDefinitionFiles = new ArrayList<>();

    // Using Set here to avoid duplicates
    private Set<URL> zapURLsIncludeSet = new HashSet<>();
    private Set<URL> zapURLsExcludeSet = new HashSet<>();

    private boolean connectionCheckEnabled;

    private int maxNumberOfConnectionRetries;
    private int retryWaittimeInMilliseconds;

    private ZapProductMessageHelper zapProductMessageHelper;
    private ZapPDSEventHandler zapPDSEventHandler;

    private ZapScanContext() {
    }

    public ZapServerConfiguration getServerConfig() {
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

    public String getTargetUrlAsString() {
        return getTargetUrl().toString();
    }

    public URL getTargetUrl() {
        return targetUrl;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public long getMaxScanDurationInMilliSeconds() {
        return maxScanDurationInMilliSeconds;
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

    public ZapFullRuleset getFullRuleset() {
        return fullRuleset;
    }

    public DeactivatedRuleReferences getDeactivatedRuleReferences() {
        return deactivatedRuleReferences;
    }

    public List<File> getApiDefinitionFiles() {
        if (apiDefinitionFiles == null) {
            return Collections.emptyList();
        }
        return apiDefinitionFiles;
    }

    public Set<URL> getZapURLsIncludeSet() {
        if (zapURLsIncludeSet == null) {
            return Collections.emptySet();
        }
        return zapURLsIncludeSet;
    }

    public Set<URL> getZapURLsExcludeSet() {
        if (zapURLsExcludeSet == null) {
            return Collections.emptySet();
        }
        return zapURLsExcludeSet;
    }

    public boolean connectionCheckEnabled() {
        return connectionCheckEnabled;
    }

    public int getMaxNumberOfConnectionRetries() {
        return maxNumberOfConnectionRetries;
    }

    public int getRetryWaittimeInMilliseconds() {
        return retryWaittimeInMilliseconds;
    }

    public ZapProductMessageHelper getZapProductMessageHelper() {
        return zapProductMessageHelper;
    }

    public ZapPDSEventHandler getZapPDSEventHandler() {
        return zapPDSEventHandler;
    }

    public static ZapBasicScanContextBuilder builder() {
        return new ZapBasicScanContextBuilder();
    }

    public static class ZapBasicScanContextBuilder {
        private ZapServerConfiguration serverConfig;

        private boolean verboseOutput = false;

        private boolean ajaxSpiderEnabled;
        private boolean activeScanEnabled;

        private Path reportFile;

        private String contextName;

        private URL targetUrl;

        private AuthenticationType authenticationType;

        private long maxScanDurationInMilliSeconds;

        private SecHubWebScanConfiguration secHubWebScanConfiguration;

        private ProxyInformation proxyInformation;

        private ZapFullRuleset fullRuleset;

        private DeactivatedRuleReferences deactivatedRuleReferences;

        private List<File> apiDefinitionFiles = new LinkedList<>();

        // Using Set here to avoid duplicates
        private Set<URL> zapURLsIncludeSet = new HashSet<>();
        private Set<URL> zapURLsExcludeSet = new HashSet<>();

        private boolean connectionCheckEnabled;

        private int maxNumberOfConnectionRetries;
        private int setRetryWaittimeInMilliseconds;

        private ZapProductMessageHelper zapProductMessageHelper;

        private ZapPDSEventHandler zapPDSEventHandler;

        public ZapBasicScanContextBuilder setServerConfig(ZapServerConfiguration serverConfig) {
            this.serverConfig = serverConfig;
            return this;
        }

        public ZapBasicScanContextBuilder setVerboseOutput(boolean verboseOutput) {
            this.verboseOutput = verboseOutput;
            return this;
        }

        public ZapBasicScanContextBuilder setAjaxSpiderEnabled(boolean ajaxSpiderEnabled) {
            this.ajaxSpiderEnabled = ajaxSpiderEnabled;
            return this;
        }

        public ZapBasicScanContextBuilder setActiveScanEnabled(boolean activeScanEnabled) {
            this.activeScanEnabled = activeScanEnabled;
            return this;
        }

        public ZapBasicScanContextBuilder setReportFile(Path reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public ZapBasicScanContextBuilder setContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public ZapBasicScanContextBuilder setTargetUrl(URL targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public ZapBasicScanContextBuilder setAuthenticationType(AuthenticationType authenticationType) {
            this.authenticationType = authenticationType;
            return this;
        }

        public ZapBasicScanContextBuilder setMaxScanDurationInMilliSeconds(long maxScanDurationInMilliSeconds) {
            this.maxScanDurationInMilliSeconds = maxScanDurationInMilliSeconds;
            return this;
        }

        public ZapBasicScanContextBuilder setSecHubWebScanConfiguration(SecHubWebScanConfiguration secHubWebScanConfiguration) {
            this.secHubWebScanConfiguration = secHubWebScanConfiguration;
            return this;
        }

        public ZapBasicScanContextBuilder setProxyInformation(ProxyInformation proxyInformation) {
            this.proxyInformation = proxyInformation;
            return this;
        }

        public ZapBasicScanContextBuilder setFullRuleset(ZapFullRuleset fullRuleset) {
            this.fullRuleset = fullRuleset;
            return this;
        }

        public ZapBasicScanContextBuilder setDeactivatedRuleReferences(DeactivatedRuleReferences deactivatedRuleReferences) {
            this.deactivatedRuleReferences = deactivatedRuleReferences;
            return this;
        }

        public ZapBasicScanContextBuilder addApiDefinitionFiles(List<File> apiDefinitionFiles) {
            this.apiDefinitionFiles.addAll(apiDefinitionFiles);
            return this;
        }

        public ZapBasicScanContextBuilder addZapURLsIncludeSet(Set<URL> zapURLsIncludeList) {
            this.zapURLsIncludeSet.addAll(zapURLsIncludeList);
            return this;
        }

        public ZapBasicScanContextBuilder addZapURLsExcludeSet(Set<URL> zapURLsExcludeList) {
            this.zapURLsExcludeSet.addAll(zapURLsExcludeList);
            return this;
        }

        public ZapBasicScanContextBuilder setConnectionCheckEnabled(boolean connectionCheckEnabled) {
            this.connectionCheckEnabled = connectionCheckEnabled;
            return this;
        }

        public ZapBasicScanContextBuilder setMaxNumberOfConnectionRetries(int maxNumberOfConnectionRetries) {
            this.maxNumberOfConnectionRetries = maxNumberOfConnectionRetries;
            return this;
        }

        public ZapBasicScanContextBuilder setRetryWaittimeInMilliseconds(int retryWaittimeInMilliseconds) {
            this.setRetryWaittimeInMilliseconds = retryWaittimeInMilliseconds;
            return this;
        }

        public ZapBasicScanContextBuilder setZapProductMessageHelper(ZapProductMessageHelper zapProductMessageHelper) {
            this.zapProductMessageHelper = zapProductMessageHelper;
            return this;
        }

        public ZapBasicScanContextBuilder setZapPDSEventHandler(ZapPDSEventHandler zapPDSEventHandler) {
            this.zapPDSEventHandler = zapPDSEventHandler;
            return this;
        }

        public ZapScanContext build() {
            ZapScanContext zapBasicScanConfiguration = new ZapScanContext();
            zapBasicScanConfiguration.serverConfig = this.serverConfig;
            zapBasicScanConfiguration.verboseOutput = this.verboseOutput;
            zapBasicScanConfiguration.ajaxSpiderEnabled = this.ajaxSpiderEnabled;
            zapBasicScanConfiguration.activeScanEnabled = this.activeScanEnabled;
            zapBasicScanConfiguration.reportFile = this.reportFile;
            zapBasicScanConfiguration.contextName = this.contextName;
            zapBasicScanConfiguration.targetUrl = this.targetUrl;
            zapBasicScanConfiguration.authenticationType = this.authenticationType;

            zapBasicScanConfiguration.maxScanDurationInMilliSeconds = this.maxScanDurationInMilliSeconds;

            zapBasicScanConfiguration.secHubWebScanConfiguration = this.secHubWebScanConfiguration;

            zapBasicScanConfiguration.proxyInformation = this.proxyInformation;

            zapBasicScanConfiguration.fullRuleset = this.fullRuleset;
            zapBasicScanConfiguration.deactivatedRuleReferences = this.deactivatedRuleReferences;

            zapBasicScanConfiguration.apiDefinitionFiles = this.apiDefinitionFiles;

            zapBasicScanConfiguration.zapURLsIncludeSet.addAll(this.zapURLsIncludeSet);
            zapBasicScanConfiguration.zapURLsExcludeSet.addAll(this.zapURLsExcludeSet);

            zapBasicScanConfiguration.connectionCheckEnabled = this.connectionCheckEnabled;

            zapBasicScanConfiguration.maxNumberOfConnectionRetries = this.maxNumberOfConnectionRetries;
            zapBasicScanConfiguration.retryWaittimeInMilliseconds = this.setRetryWaittimeInMilliseconds;

            zapBasicScanConfiguration.zapProductMessageHelper = this.zapProductMessageHelper;

            zapBasicScanConfiguration.zapPDSEventHandler = this.zapPDSEventHandler;

            return zapBasicScanConfiguration;
        }

    }
}
