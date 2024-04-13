// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private Set<String> zapURLsIncludeSet = new HashSet<>();
    private Set<String> zapURLsExcludeSet = new HashSet<>();

    private boolean connectionCheckEnabled;

    private int maxNumberOfConnectionRetries;
    private int retryWaittimeInMilliseconds;

    private ZapProductMessageHelper zapProductMessageHelper;
    private ZapPDSEventHandler zapPDSEventHandler;

    private File clientCertificateFile;
    private Map<String, File> headerValueFiles;

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

    public Set<String> getZapURLsIncludeSet() {
        if (zapURLsIncludeSet == null) {
            return Collections.emptySet();
        }
        return zapURLsIncludeSet;
    }

    public Set<String> getZapURLsExcludeSet() {
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

    public File getClientCertificateFile() {
        return clientCertificateFile;
    }

    public Map<String, File> getHeaderValueFiles() {
        return Collections.unmodifiableMap(headerValueFiles);
    }

    public static ZapScanContextBuilder builder() {
        return new ZapScanContextBuilder();
    }

    public static class ZapScanContextBuilder {
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
        private Set<String> zapURLsIncludeSet = new HashSet<>();
        private Set<String> zapURLsExcludeSet = new HashSet<>();

        private boolean connectionCheckEnabled;

        private int maxNumberOfConnectionRetries;
        private int setRetryWaittimeInMilliseconds;

        private ZapProductMessageHelper zapProductMessageHelper;

        private ZapPDSEventHandler zapPDSEventHandler;

        private File clientCertificateFile;

        private Map<String, File> headerValueFiles = new HashMap<>();

        public ZapScanContextBuilder setServerConfig(ZapServerConfiguration serverConfig) {
            this.serverConfig = serverConfig;
            return this;
        }

        public ZapScanContextBuilder setVerboseOutput(boolean verboseOutput) {
            this.verboseOutput = verboseOutput;
            return this;
        }

        public ZapScanContextBuilder setAjaxSpiderEnabled(boolean ajaxSpiderEnabled) {
            this.ajaxSpiderEnabled = ajaxSpiderEnabled;
            return this;
        }

        public ZapScanContextBuilder setActiveScanEnabled(boolean activeScanEnabled) {
            this.activeScanEnabled = activeScanEnabled;
            return this;
        }

        public ZapScanContextBuilder setReportFile(Path reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public ZapScanContextBuilder setContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public ZapScanContextBuilder setTargetUrl(URL targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public ZapScanContextBuilder setAuthenticationType(AuthenticationType authenticationType) {
            this.authenticationType = authenticationType;
            return this;
        }

        public ZapScanContextBuilder setMaxScanDurationInMilliSeconds(long maxScanDurationInMilliSeconds) {
            this.maxScanDurationInMilliSeconds = maxScanDurationInMilliSeconds;
            return this;
        }

        public ZapScanContextBuilder setSecHubWebScanConfiguration(SecHubWebScanConfiguration secHubWebScanConfiguration) {
            this.secHubWebScanConfiguration = secHubWebScanConfiguration;
            return this;
        }

        public ZapScanContextBuilder setProxyInformation(ProxyInformation proxyInformation) {
            this.proxyInformation = proxyInformation;
            return this;
        }

        public ZapScanContextBuilder setFullRuleset(ZapFullRuleset fullRuleset) {
            this.fullRuleset = fullRuleset;
            return this;
        }

        public ZapScanContextBuilder setDeactivatedRuleReferences(DeactivatedRuleReferences deactivatedRuleReferences) {
            this.deactivatedRuleReferences = deactivatedRuleReferences;
            return this;
        }

        public ZapScanContextBuilder addApiDefinitionFiles(List<File> apiDefinitionFiles) {
            this.apiDefinitionFiles.addAll(apiDefinitionFiles);
            return this;
        }

        public ZapScanContextBuilder addZapURLsIncludeSet(Set<String> zapURLsIncludeList) {
            this.zapURLsIncludeSet.addAll(zapURLsIncludeList);
            return this;
        }

        public ZapScanContextBuilder addZapURLsExcludeSet(Set<String> zapURLsExcludeList) {
            this.zapURLsExcludeSet.addAll(zapURLsExcludeList);
            return this;
        }

        public ZapScanContextBuilder setConnectionCheckEnabled(boolean connectionCheckEnabled) {
            this.connectionCheckEnabled = connectionCheckEnabled;
            return this;
        }

        public ZapScanContextBuilder setMaxNumberOfConnectionRetries(int maxNumberOfConnectionRetries) {
            this.maxNumberOfConnectionRetries = maxNumberOfConnectionRetries;
            return this;
        }

        public ZapScanContextBuilder setRetryWaittimeInMilliseconds(int retryWaittimeInMilliseconds) {
            this.setRetryWaittimeInMilliseconds = retryWaittimeInMilliseconds;
            return this;
        }

        public ZapScanContextBuilder setZapProductMessageHelper(ZapProductMessageHelper zapProductMessageHelper) {
            this.zapProductMessageHelper = zapProductMessageHelper;
            return this;
        }

        public ZapScanContextBuilder setZapPDSEventHandler(ZapPDSEventHandler zapPDSEventHandler) {
            this.zapPDSEventHandler = zapPDSEventHandler;
            return this;
        }

        public ZapScanContextBuilder setClientCertificateFile(File clientCertificateFile) {
            this.clientCertificateFile = clientCertificateFile;
            return this;
        }

        public ZapScanContextBuilder addHeaderValueFiles(Map<String, File> headerValueFiles) {
            this.headerValueFiles.putAll(headerValueFiles);
            return this;
        }

        public ZapScanContext build() {
            ZapScanContext zapScanConfiguration = new ZapScanContext();
            zapScanConfiguration.serverConfig = this.serverConfig;
            zapScanConfiguration.verboseOutput = this.verboseOutput;
            zapScanConfiguration.ajaxSpiderEnabled = this.ajaxSpiderEnabled;
            zapScanConfiguration.activeScanEnabled = this.activeScanEnabled;
            zapScanConfiguration.reportFile = this.reportFile;
            zapScanConfiguration.contextName = this.contextName;
            zapScanConfiguration.targetUrl = this.targetUrl;
            zapScanConfiguration.authenticationType = this.authenticationType;

            zapScanConfiguration.maxScanDurationInMilliSeconds = this.maxScanDurationInMilliSeconds;

            zapScanConfiguration.secHubWebScanConfiguration = this.secHubWebScanConfiguration;

            zapScanConfiguration.proxyInformation = this.proxyInformation;

            zapScanConfiguration.fullRuleset = this.fullRuleset;
            zapScanConfiguration.deactivatedRuleReferences = this.deactivatedRuleReferences;

            zapScanConfiguration.apiDefinitionFiles = this.apiDefinitionFiles;

            zapScanConfiguration.zapURLsIncludeSet.addAll(this.zapURLsIncludeSet);
            zapScanConfiguration.zapURLsExcludeSet.addAll(this.zapURLsExcludeSet);

            zapScanConfiguration.connectionCheckEnabled = this.connectionCheckEnabled;

            zapScanConfiguration.maxNumberOfConnectionRetries = this.maxNumberOfConnectionRetries;
            zapScanConfiguration.retryWaittimeInMilliseconds = this.setRetryWaittimeInMilliseconds;

            zapScanConfiguration.zapProductMessageHelper = this.zapProductMessageHelper;

            zapScanConfiguration.zapPDSEventHandler = this.zapPDSEventHandler;

            zapScanConfiguration.clientCertificateFile = this.clientCertificateFile;

            zapScanConfiguration.headerValueFiles = this.headerValueFiles;

            return zapScanConfiguration;
        }

    }
}
