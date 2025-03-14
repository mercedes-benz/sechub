// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginVerificationConfiguration;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
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
    private File pacFilePath;
    private boolean noHeadless;
    private int zapContextId;
    public int maximumLoginScriptFailureRetries;

    private ZapScanContext() {
    }

    public int getZapContextId() {
        return zapContextId;
    }

    public void setZapContextId(int zapContextId) {
        this.zapContextId = zapContextId;
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

    public boolean isNoHeadless() {
        return noHeadless;
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
        if (secHubWebScanConfiguration == null) {
            return Collections.emptyMap();
        }
        Optional<WebLoginConfiguration> optLogin = secHubWebScanConfiguration.getLogin();
        if (optLogin.isEmpty()) {
            return Collections.emptyMap();
        }
        WebLoginConfiguration login = optLogin.get();
        TemplateData templateData = login.getTemplateData();
        if (templateData == null) {
            return Collections.emptyMap();
        }
        return templateData.getVariables();
    }

    public File getPacFilePath() {
        return pacFilePath;
    }

    public WebLoginVerificationConfiguration getVerificationFromConfig() {
        SecHubWebScanConfiguration config = this.getSecHubWebScanConfiguration();
        if (config == null) {
            return null;
        }
        Optional<WebLoginConfiguration> login = this.getSecHubWebScanConfiguration().getLogin();
        if (login.isEmpty()) {
            return null;
        }
        WebLoginConfiguration webLoginConfiguration = login.get();
        return webLoginConfiguration.getVerification();
    }

    public int getMaximumLoginScriptFailureRetries() {
        return maximumLoginScriptFailureRetries;
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

        private File pacFilePath;

        private boolean noHeadless;

        private int loginScriptFailureMaximumRetries;

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
            if (zapRuleIDsToDeactivate != null) {
                this.zapRuleIDsToDeactivate = new LinkedList<>(zapRuleIDsToDeactivate);
            }
            return this;
        }

        public ZapScanContextBuilder setApiDefinitionFiles(List<File> apiDefinitionFiles) {
            if (apiDefinitionFiles != null) {
                this.apiDefinitionFiles = new LinkedList<>(apiDefinitionFiles);
            }
            return this;
        }

        public ZapScanContextBuilder setZapURLsIncludeSet(Set<String> zapURLsIncludeSet) {
            if (zapURLsIncludeSet != null) {
                this.zapURLsIncludeSet = new HashSet<>(zapURLsIncludeSet);
            }
            return this;
        }

        public ZapScanContextBuilder setZapURLsExcludeSet(Set<String> zapURLsExcludeSet) {
            if (zapURLsExcludeSet != null) {
                this.zapURLsExcludeSet = new HashSet<>(zapURLsExcludeSet);
            }
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
            if (headerValueFiles != null) {
                this.headerValueFiles = new HashMap<>(headerValueFiles);
            }
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

        public ZapScanContextBuilder setPacFilePath(File pacFilePath) {
            this.pacFilePath = pacFilePath;
            return this;
        }

        public ZapScanContextBuilder setNoHeadless(boolean noHeadless) {
            this.noHeadless = noHeadless;
            return this;
        }

        public ZapScanContextBuilder setMaxGroovyScriptLoginFailureRetries(int loginScriptFailureMaximumRetries) {
            this.loginScriptFailureMaximumRetries = loginScriptFailureMaximumRetries;
            return this;
        }

        public ZapScanContext build() {
            BrowserIdTransformationSupport transformBrowserIdSupport = new BrowserIdTransformationSupport();

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

            zapScanContext.noHeadless = this.noHeadless;
            zapScanContext.ajaxSpiderBrowserId = transformBrowserIdSupport.transformBrowserIdWhenNoHeadless(this.noHeadless, this.ajaxSpiderBrowserId);

            zapScanContext.groovyScriptLoginFile = this.groovyScriptLoginFile;

            zapScanContext.pacFilePath = this.pacFilePath;

            zapScanContext.maximumLoginScriptFailureRetries = loginScriptFailureMaximumRetries;

            return zapScanContext;
        }
    }
}
