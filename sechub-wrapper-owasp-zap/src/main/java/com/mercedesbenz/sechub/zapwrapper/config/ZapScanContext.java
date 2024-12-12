// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
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

    private SecHubWebScanConfiguration secHubWebScanConfiguration;

    private ProxyInformation proxyInformation;

    private List<String> zapRuleIDsToDeactivate = new LinkedList<>();

    private List<File> apiDefinitionFiles = new LinkedList<>();

    // Using Set here to avoid duplicates
    private Set<String> zapURLsIncludeSet = new HashSet<>();
    private Set<String> zapURLsExcludeSet = new HashSet<>();

    private boolean connectionCheckEnabled;

    private int maxNumberOfConnectionRetries;
    private int retryWaittimeInMilliseconds;

    private ZapProductMessageHelper zapProductMessageHelper;
    private ZapPDSEventHandler zapPDSEventHandler;

    private File clientCertificateFile;
    private Map<String, File> headerValueFiles = new HashMap<>();
    private String ajaxSpiderBrowserId;

    private File groovyScriptLoginFile;
    private Map<String, String> templateVariables = new LinkedHashMap<>();

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

    public List<String> getZapRuleIDsToDeactivate() {
        return Collections.unmodifiableList(zapRuleIDsToDeactivate);
    }

    public List<File> getApiDefinitionFiles() {
        return Collections.unmodifiableList(apiDefinitionFiles);
    }

    public Set<String> getZapURLsIncludeSet() {
        return Collections.unmodifiableSet(zapURLsIncludeSet);
    }

    public Set<String> getZapURLsExcludeSet() {
        return Collections.unmodifiableSet(zapURLsExcludeSet);
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

    public String getAjaxSpiderBrowserId() {
        return ajaxSpiderBrowserId;
    }

    public File getGroovyScriptLoginFile() {
        return groovyScriptLoginFile;
    }

    public Map<String, String> getTemplateVariables() {
        return Collections.unmodifiableMap(templateVariables);
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

        private SecHubWebScanConfiguration secHubWebScanConfiguration;

        private ProxyInformation proxyInformation;

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

        private String ajaxSpiderBrowserId;

        private File groovyScriptLoginFile;

        private List<String> zapRuleIDsToDeactivate = new LinkedList<>();

        private Map<String, String> templateVariables = new LinkedHashMap<>();

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

        public ZapScanContextBuilder setSecHubWebScanConfiguration(SecHubWebScanConfiguration secHubWebScanConfiguration) {
            this.secHubWebScanConfiguration = secHubWebScanConfiguration;
            return this;
        }

        public ZapScanContextBuilder setProxyInformation(ProxyInformation proxyInformation) {
            this.proxyInformation = proxyInformation;
            return this;
        }

        public ZapScanContextBuilder setZapRuleIDsToDeactivate(List<String> zapRuleIDsToDeactivate) {
            this.zapRuleIDsToDeactivate.clear();
            this.zapRuleIDsToDeactivate = zapRuleIDsToDeactivate;
            return this;
        }

        public ZapScanContextBuilder setApiDefinitionFiles(List<File> apiDefinitionFiles) {
            this.apiDefinitionFiles.clear();
            this.apiDefinitionFiles.addAll(apiDefinitionFiles);
            return this;
        }

        public ZapScanContextBuilder setZapURLsIncludeSet(Set<String> zapURLsIncludeList) {
            this.zapURLsExcludeSet.clear();
            this.zapURLsIncludeSet.addAll(zapURLsIncludeList);
            return this;
        }

        public ZapScanContextBuilder setZapURLsExcludeSet(Set<String> zapURLsExcludeList) {
            this.zapURLsExcludeSet.clear();
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

        public ZapScanContextBuilder setHeaderValueFiles(Map<String, File> headerValueFiles) {
            this.headerValueFiles.clear();
            this.headerValueFiles.putAll(headerValueFiles);
            return this;
        }

        public ZapScanContextBuilder setAjaxSpiderBrowserId(String ajaxSpiderBrowserId) {
            this.ajaxSpiderBrowserId = ajaxSpiderBrowserId;
            return this;
        }

        public ZapScanContextBuilder setGroovyScriptLoginFile(File groovyScriptLoginFile) {
            this.groovyScriptLoginFile = groovyScriptLoginFile;
            return this;
        }

        public ZapScanContextBuilder setTemplateVariables(Map<String, String> templateVariables) {
            this.templateVariables.clear();
            this.templateVariables.putAll(templateVariables);
            return this;
        }

        public ZapScanContext build() {
            ZapScanContext zapScanContext = new ZapScanContext();
            zapScanContext.serverConfig = this.serverConfig;
            zapScanContext.verboseOutput = this.verboseOutput;
            zapScanContext.ajaxSpiderEnabled = this.ajaxSpiderEnabled;
            zapScanContext.activeScanEnabled = this.activeScanEnabled;
            zapScanContext.reportFile = this.reportFile;
            zapScanContext.contextName = this.contextName;
            zapScanContext.targetUrl = this.targetUrl;

            zapScanContext.secHubWebScanConfiguration = this.secHubWebScanConfiguration;

            zapScanContext.proxyInformation = this.proxyInformation;

            zapScanContext.zapRuleIDsToDeactivate = this.zapRuleIDsToDeactivate;

            zapScanContext.apiDefinitionFiles = this.apiDefinitionFiles;

            zapScanContext.zapURLsIncludeSet = this.zapURLsIncludeSet;
            zapScanContext.zapURLsExcludeSet = this.zapURLsExcludeSet;

            zapScanContext.connectionCheckEnabled = this.connectionCheckEnabled;

            zapScanContext.maxNumberOfConnectionRetries = this.maxNumberOfConnectionRetries;
            zapScanContext.retryWaittimeInMilliseconds = this.setRetryWaittimeInMilliseconds;

            zapScanContext.zapProductMessageHelper = this.zapProductMessageHelper;

            zapScanContext.zapPDSEventHandler = this.zapPDSEventHandler;

            zapScanContext.clientCertificateFile = this.clientCertificateFile;

            zapScanContext.headerValueFiles = this.headerValueFiles;

            zapScanContext.ajaxSpiderBrowserId = this.ajaxSpiderBrowserId;

            zapScanContext.groovyScriptLoginFile = this.groovyScriptLoginFile;
            zapScanContext.templateVariables = this.templateVariables;

            return zapScanContext;
        }

    }
}
