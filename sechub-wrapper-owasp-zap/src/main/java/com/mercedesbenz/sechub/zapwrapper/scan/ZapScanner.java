// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.*;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginVerificationConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.auth.ZapAuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.auth.ZapSessionManagementType;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;
import com.mercedesbenz.sechub.zapwrapper.scan.login.*;
import com.mercedesbenz.sechub.zapwrapper.util.SystemUtil;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

public class ZapScanner implements ZapScan {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScanner.class);

    public static final String X_SECHUB_DAST_HEADER_NAME = "x-sechub-dast";

    private static final int CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS = 5000;
    private static final int DEFAULT_MAX_DEPTH_AJAX_SPIDER = 10;
    private static final int DEFAULT_MAX_DEPTH_SPIDER = 5;

    // all kinds of logout calls that might show up
    private static final String DEFAULT_EXCLUDE = "(?i).*(log[\\s_+-]*out|log[\\s_+-]*off|sign[\\s_+-]*out|sign[\\s_+-]*off|abmelden|ausloggen).*";

    private final ClientApiWrapper clientApiWrapper;
    private final ZapScanContext scanContext;

    private final ZapScriptLogin scriptLogin;
    private final UrlUtil urlUtil;

    private final SystemUtil systemUtil;

    public ZapScanner(ClientApiWrapper clientApiWrapper, ZapScanContext scanContext) {
        this.clientApiWrapper = clientApiWrapper;
        this.scanContext = scanContext;

        this.urlUtil = new UrlUtil();
        this.systemUtil = new SystemUtil();

        this.scriptLogin = new ZapScriptLogin();
    }

    ZapScanner(ClientApiWrapper clientApiWrapper, ZapScanContext scanContext, UrlUtil urlUtil, SystemUtil systemUtil, ZapScriptLogin scriptLogin) {
        this.clientApiWrapper = clientApiWrapper;
        this.scanContext = scanContext;

        this.urlUtil = urlUtil;
        this.systemUtil = systemUtil;

        this.scriptLogin = scriptLogin;
    }

    @Override
    public void scan() throws ZapWrapperRuntimeException {
        try {
            /* ZAP setup on local machine */
            setupStandardConfiguration();
            deactivateRules();
            setupAdditonalProxyConfiguration(scanContext.getProxyInformation());
            int zapContextId = createContext();
            scanContext.setZapContextId(zapContextId);
            addXSecHubDASTHeader();
            addReplacerRulesForHeaders();
            addDefaultExcludes();

            /* ZAP setup with access to target */
            // The order of the following method calls is important. We want to load the
            // client certificate first, because it could be needed to access the included
            // URLs or the URLs from the API definitions.
            importClientCertificate();
            addIncludedAndExcludedUrlsToContext();
            loadApiDefinitions();

            /* ZAP scan */
            executeScan();

            /* After scan */
            generateZapReport();
            cleanUp();
        } catch (ClientApiException | ZapWrapperRuntimeException e) {
            cleanUp();
            throw new ZapWrapperRuntimeException("For scan: " + scanContext.getContextName() + ". An error occured while scanning!", e,
                    ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        }
    }

    void setupStandardConfiguration() throws ClientApiException {
        clientApiWrapper.createNewSession(scanContext.getContextName(), true);
        clientApiWrapper.setMaximumAlertsForEachRuleToUnlimited();
        clientApiWrapper.enableAllPassiveScannerRules();
        clientApiWrapper.enableAllActiveScannerRulesForDefaultPolicy();
        clientApiWrapper.setAjaxSpiderBrowserId(scanContext.getAjaxSpiderBrowserId());
        clientApiWrapper.setSpiderMaxDepth(DEFAULT_MAX_DEPTH_SPIDER);
        clientApiWrapper.setAjaxSpiderMaxDepth(DEFAULT_MAX_DEPTH_AJAX_SPIDER);
    }

    void deactivateRules() throws ClientApiException {
        for (String ruleId : scanContext.getZapRuleIDsToDeactivate()) {
            boolean wasDeactivated = clientApiWrapper.disablePassiveScannerRule(ruleId);
            if (!wasDeactivated) {
                wasDeactivated = clientApiWrapper.disableActiveScannerRuleForDefaultPolicy(ruleId);
            }
            if (!wasDeactivated) {
                LOG.warn("Unable to deactivate ruleId: {}, as it is neither a passive nor an active scanner rule!", ruleId);
            }
        }
    }

    void setupAdditonalProxyConfiguration(ProxyInformation proxyInformation) throws ClientApiException {
        if (proxyInformation != null) {
            String proxyHost = proxyInformation.getHost();
            int proxyPort = proxyInformation.getPort();
            LOG.info("Using proxy {}:{} to reach target.", proxyHost, proxyPort);
            clientApiWrapper.configureHttpProxy(proxyInformation);
            clientApiWrapper.setHttpProxyEnabled(true);
            clientApiWrapper.setHttpProxyAuthEnabled(false);
        } else {
            LOG.info("No proxy was set, continuing without proxy.");
            clientApiWrapper.setHttpProxyEnabled(false);
        }
    }

    /**
     * Creates new context in the current ZAP session.
     *
     * @return the context id returned by the ZAP API
     * @throws ClientApiException
     */
    int createContext() throws ClientApiException {
        LOG.info("Creating context: {}", scanContext.getContextName());
        return clientApiWrapper.createNewContext(scanContext.getContextName());
    }

    void addReplacerRulesForHeaders() throws ClientApiException {
        if (scanContext.getSecHubWebScanConfiguration().getHeaders().isEmpty()) {
            LOG.info("No headers were configured inside the sechub webscan configuration.");
            return;
        }

        // description specifies the rule name, which will be set later in this method
        String description = null;

        boolean enabled = true;
        // "REQ_HEADER" means the header entry will be added to the requests if not
        // existing or replaced if already existing
        String matchtype = "REQ_HEADER";
        boolean matchregex = false;

        // matchstring and replacement will be set to the header name and header value
        String matchstring = null;
        String replacement = null;

        // setting initiators to null means all initiators (ZAP components),
        // this means spider, active scan, etc will send this rule for their requests.
        String initiators = null;
        // default URL is null which means the header would be send on any request to
        // any URL
        String url = null;
        List<HTTPHeaderConfiguration> httpHeaders = scanContext.getSecHubWebScanConfiguration().getHeaders().get();
        LOG.info("For scan {}: Applying header configuration.", scanContext.getContextName());
        for (HTTPHeaderConfiguration httpHeader : httpHeaders) {
            matchstring = httpHeader.getName();
            replacement = httpHeader.getValue();

            if (replacement == null) {
                replacement = readHeaderValueFromFile(httpHeader);
            }

            if (httpHeader.getOnlyForUrls().isEmpty()) {
                // if there are no onlyForUrl patterns, there is only one rule for each header
                description = httpHeader.getName();
                clientApiWrapper.addReplacerRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
            } else {
                for (String onlyForUrl : httpHeader.getOnlyForUrls().get()) {
                    // we need to create a rule for each onlyForUrl pattern on each header
                    description = onlyForUrl;
                    url = urlUtil.replaceWebScanWildCardsWithRegexInString(onlyForUrl);
                    clientApiWrapper.addReplacerRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
                }
            }
        }
    }

    /**
     * Adds all included and excluded URLs into scan context.
     *
     * @throws ClientApiException
     */
    void addIncludedAndExcludedUrlsToContext() throws ClientApiException {
        LOG.info("For scan {}: Adding include parts.", scanContext.getContextName());
        boolean followRedirects = false;
        for (String url : scanContext.getZapURLsIncludeSet()) {
            clientApiWrapper.addIncludeUrlPatternToContext(scanContext.getContextName(), url);
            // Cannot not perform initial connection check to included URL with wildcards
            if (url.contains(".*")) {
                LOG.info("For scan {}: Cannot not perform initial connection check to included URL: {} because it contains wildcards.",
                        scanContext.getContextName(), url);
            } else {
                clientApiWrapper.accessUrlViaZap(url, followRedirects);
            }
        }

        LOG.info("For scan {}: Adding exclude parts.", scanContext.getContextName());
        for (String url : scanContext.getZapURLsExcludeSet()) {
            clientApiWrapper.addExcludeUrlPatternToContext(scanContext.getContextName(), url);
        }
    }

    void loadApiDefinitions() throws ClientApiException {
        Optional<SecHubWebScanApiConfiguration> apiConfig = scanContext.getSecHubWebScanConfiguration().getApi();
        if (!apiConfig.isPresent()) {
            LOG.info("For scan {}: No API definition was found!", scanContext.getContextName());
            return;
        }

        SecHubWebScanApiConfiguration secHubWebScanApiConfiguration = apiConfig.get();
        switch (secHubWebScanApiConfiguration.getType()) {
        case OPEN_API:
            URL apiDefinitionUrl = secHubWebScanApiConfiguration.getApiDefinitionUrl();
            if (apiDefinitionUrl != null) {
                clientApiWrapper.importOpenApiDefintionFromUrl(apiDefinitionUrl, scanContext.getTargetUrlAsString(), scanContext.getZapContextId());
            }
            for (File apiFile : scanContext.getApiDefinitionFiles()) {
                clientApiWrapper.importOpenApiFile(apiFile.toString(), scanContext.getTargetUrlAsString(), scanContext.getZapContextId());
            }
            break;
        default:
            // should never happen since API type is an Enum
            // Failure should happen before getting here
            throw new ZapWrapperRuntimeException("For scan :" + scanContext.getContextName() + ". Unknown API type was definied!",
                    ZapWrapperExitCode.API_DEFINITION_CONFIG_INVALID);
        }
    }

    void importClientCertificate() throws ClientApiException {
        if (scanContext.getClientCertificateFile() == null) {
            LOG.info("For scan {}: No client certificate file was found!", scanContext.getContextName());
            return;
        }
        Optional<ClientCertificateConfiguration> optionalClientCertConfig = scanContext.getSecHubWebScanConfiguration().getClientCertificate();
        if (optionalClientCertConfig.isEmpty()) {
            LOG.info("For scan {}: No client certificate configuration was found!", scanContext.getContextName());
            return;
        }
        // Should never happen at this point, only if the client certificate file was
        // not extracted correctly
        if (!scanContext.getClientCertificateFile().exists()) {
            throw new ZapWrapperRuntimeException("For scan " + scanContext.getContextName()
                    + ": A client certificate section was configured inside the sechub configuration, but the client certificate file was not found on the filesystem inside the extracted sources!",
                    ZapWrapperExitCode.CLIENT_CERTIFICATE_CONFIG_INVALID);
        }

        ClientCertificateConfiguration clientCertificateConfig = optionalClientCertConfig.get();
        File clientCertificateFile = scanContext.getClientCertificateFile();

        String password = null;
        if (clientCertificateConfig.getPassword() != null) {
            password = new String(clientCertificateConfig.getPassword());
        }
        clientApiWrapper.importPkcs12ClientCertificate(clientCertificateFile.getAbsolutePath(), password);
        clientApiWrapper.enableClientCertificate();
    }

    void executeScan() throws ClientApiException {
        UserInformation userInfo = setupLoginInsideZapContext();
        if (userInfo != null) {
            runAndWaitForSpiderAsUser(userInfo.zapuserId);
            runAndWaitForPassiveScan();
            if (scanContext.isAjaxSpiderEnabled()) {
                runAndWaitForAjaxSpiderAsUser(userInfo.userName);
            }
            if (scanContext.isActiveScanEnabled()) {
                runActiveScanAsUser(userInfo.zapuserId);
            }
        } else {
            runAndWaitForSpider();
            runAndWaitForPassiveScan();
            if (scanContext.isAjaxSpiderEnabled()) {
                runAndWaitAjaxSpider();
            }
            if (scanContext.isActiveScanEnabled()) {
                runAndWaitActiveScan();
            }
        }
    }

    /**
     * Configure login according to the sechub webscan config.
     *
     * <p>
     * A future use case with script authentication could be multiple users scan
     * with different sessions. See also
     * <a href="https://github.com/zaproxy/zaproxy/issues/6342">
     * https://github.com/zaproxy/zaproxy/issues/6342</a>
     * </p>
     *
     * <pre>
     * {@code
     * // Example how to set up one user with a specific session
     *
     * String zapAuthSessionName = scriptLogin.login(scanContext, clientApiWrapper);
     * String username = scanContext.getTemplateVariables().get(ZapTemplateDataVariableKeys.USERNAME_KEY);
     * LOG.info("For scan {}: Setup scan user in ZAP to use authenticated session.",
     * scanContext.getContextName()); StringBuilder authCredentialsConfigParams =
     * new StringBuilder();
     * authCredentialsConfigParams.append("username=").append(urlEncodeUTF8(username))
     * .append("&sessionName=").append(urlEncodeUTF8(zapAuthSessionName));
     * clientApiWrapper.addIncludeUrlPatternToContext(scanContext.getContextName(),
     * "^.*"+scanContext.getTargetUrl().getHost()+".*"); UserInformation userInfo =
     * setupScanUserForZapContext(zapContextId, username,
     * authCredentialsConfigParams.toString()); }
     *
     * @return UserInformation containing userName and zapUserId or
     *         <code>null</code> if nothing could be configured.
     * @throws ClientApiException
     */
    UserInformation setupLoginInsideZapContext() throws ClientApiException {
        if (scanContext.getSecHubWebScanConfiguration().getLogin().isEmpty()) {
            LOG.info("For scan {}: No login section detected.", scanContext.getContextName());
            return null;
        }

        WebLoginConfiguration webLoginConfiguration = scanContext.getSecHubWebScanConfiguration().getLogin().get();
        if (webLoginConfiguration.getBasic().isPresent()) {
            LOG.info("For scan {}: Applying basic authentication config.", scanContext.getContextName());
            return initBasicAuthentication(webLoginConfiguration.getBasic().get());
        }

        if (scriptLoginConfigured()) {
            LOG.info("For scan {}: Setting up authentcation and session management method for script authentication.", scanContext.getContextName());
            setupAuthenticationAndSessionManagementMethodForScriptLogin();

            LOG.info("For scan {}: Performing script authentication.", scanContext.getContextName());
            // we only want to scan with one valid session
            // this means it is enough to login and set the session to the active session
            // ZAP will then use this session for all requests, no user setup is needed
            // A user setup with ZAP's manual authentication mode is only necessary if
            // multiple users with different sessions must be used
            // See the JavaDoc for an example if this use case appears.
            scriptLogin.login(scanContext, clientApiWrapper);
            return null;
        }
        return null;
    }

    /**
     * Generates the SARIF report for the current scan, identified using the context
     * name.
     *
     * @throws ClientApiException
     */
    void generateZapReport() throws ClientApiException {
        Path reportFile = scanContext.getReportFile();

        String title = scanContext.getContextName();
        String template = "sarif-json";
        String theme = null;
        String description = null;
        String contexts = scanContext.getContextName();
        String sites = null;
        String sections = null;
        String includedconfidences = null;
        String includedrisks = null;
        String reportfilename = reportFile.getFileName().toString();
        String reportfilenamepattern = null;
        String reportdir = resolveParentDirectoryPath(reportFile);
        String display = null;
        /* @formatter:off */
        // we use the context name as report title
        clientApiWrapper.generateReport(
                                title,
                                template,
                                theme,
                                description,
                                contexts,
                                sites,
                                sections,
                                includedconfidences,
                                includedrisks,
                                reportfilename,
                                reportfilenamepattern,
                                reportdir,
                                display
                                );
		/* @formatter:on */

        // rename is necessary if the file extension is not .json, because Zap
        // adds the file extension .json since we create a SARIF json report.
        renameReportFileToOriginalNameIfNecessary();

        LOG.info("For scan {}: Report can be found at {}", scanContext.getContextName(), reportFile.toFile().getAbsolutePath());
    }

    void cleanUp() {
        // to ensure parts from previous scan are deleted
        LOG.info("Starting cleanup.");
        try {
            clientApiWrapper.createNewSession("Cleaned after scan", true);

            cleanUpReplacerRules();
            // Remove x-sechub-dast header replacer rule
            clientApiWrapper.removeReplacerRule(X_SECHUB_DAST_HEADER_NAME);

            clientApiWrapper.disableClientCertificate();

            scriptLogin.cleanUpScriptLoginData(scanContext.getTargetUrlAsString(), clientApiWrapper);

            LOG.info("Cleanup successful.");
        } catch (ClientApiException e) {
            LOG.error("For scan: {}. An error occurred during the clean up, because: {}", scanContext.getContextName(), e.getMessage());
        }
    }

    void runAndWaitForSpider() throws ClientApiException {
        String contextName = scanContext.getContextName();
        boolean subTreeOnly = true;
        boolean recurse = true;
        String maxChildren = null;
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        LOG.info("For scan {}: Starting Spider.", contextName);
        /* @formatter:off */
        int scanId =
                clientApiWrapper.startSpiderScan(
                                        targetUrlAsString,
                                        maxChildren,
                                        recurse,
                                        contextName,
                                        subTreeOnly);
		/* @formatter:on */
        waitForSpiderResults(scanId);
    }

    void runAndWaitAjaxSpider() throws ClientApiException {
        boolean inScope = true;
        boolean subTreeOnly = true;
        String contextName = scanContext.getContextName();
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        LOG.info("For scan {}: Starting AjaxSpider.", scanContext.getContextName());
        /* @formatter:off */
		clientApiWrapper.startAjaxSpiderScan(
                                    targetUrlAsString,
                                    inScope,
                                    contextName,
                                    subTreeOnly);
		/* @formatter:on */
        waitForAjaxSpiderResults();
    }

    /**
     * Runs the active scanner with the given user for the given context and waits
     * for the scan to be completed or cancelled.
     *
     * @throws ClientApiException
     */
    void runAndWaitActiveScan() throws ClientApiException {
        // Necessary otherwise the active scanner exits with an exception,
        // if no URLs to scan where detected by the spider/ajaxSpider before
        if (!clientApiWrapper.atLeastOneURLDetected()) {
            LOG.warn("For {} skipping active scan, since no URLs where detected by spider or ajaxSpider!", scanContext.getContextName());
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(
                    new SecHubMessage(SecHubMessageType.WARNING, "Skipped the active scan, because no URLs were detected by the crawler! "
                            + "Please check if the URL you specified or any of the includes are accessible."));
            return;
        }
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        boolean inScopeOnly = true;
        boolean recurse = true;
        String scanPolicyName = null;
        String method = null;
        String postData = null;
        LOG.info("For scan {}: Starting ActiveScan.", scanContext.getContextName());
        /* @formatter:off */
		int scanId =
		        clientApiWrapper.startActiveScan(
                                        targetUrlAsString,
                                        recurse,
                                        inScopeOnly,
                                        scanPolicyName,
                                        method,
                                        postData,
                                        scanContext.getZapContextId());
		/* @formatter:on */
        waitForActiveScanResults(scanId);
    }

    /**
     * Runs the spider with the given user for the given context and waits for the
     * scan to be completed or cancelled.
     *
     * @param userId
     * @throws ClientApiException
     */
    void runAndWaitForSpiderAsUser(int userId) throws ClientApiException {
        String url = scanContext.getTargetUrlAsString();
        String maxchildren = null;
        boolean recurse = true;
        boolean subtreeonly = true;
        LOG.info("For scan {}: Starting authenticated Spider.", scanContext.getContextName());
        /* @formatter:off */
        int scanId =
                clientApiWrapper.startSpiderScanAsUser(
                                            scanContext.getZapContextId(),
                                            userId,
                                            url,
                                            maxchildren,
                                            recurse,
                                            subtreeonly);
		/* @formatter:on */
        waitForSpiderResults(scanId);
    }

    /**
     * Runs the ajax spider with the given user for the given context and waits for
     * the scan to be completed or cancelled.
     *
     * @param username
     * @throws ClientApiException
     */
    void runAndWaitForAjaxSpiderAsUser(String username) throws ClientApiException {
        String contextname = scanContext.getContextName();
        String url = scanContext.getTargetUrlAsString();
        boolean subtreeonly = true;
        LOG.info("For scan {}: Starting authenticated Ajax Spider.", contextname);
        /* @formatter:off */
		clientApiWrapper.startAjaxSpiderScanAsUser(
                                            contextname,
                                            username,
                                            url,
                                            subtreeonly);
		/* @formatter:on */
        waitForAjaxSpiderResults();
    }

    /**
     * Runs the active scanner with the given user for the given context and waits
     * for the scan to be completed or cancelled.
     *
     * @param userId
     * @throws ClientApiException
     */
    void runActiveScanAsUser(int userId) throws ClientApiException {
        // Necessary otherwise the active scanner exits with an exception,
        // if no URLs to scan where detected by the spider/ajaxSpider before
        if (!clientApiWrapper.atLeastOneURLDetected()) {
            LOG.warn("For {} skipping active scan, since no URLs where detected by spider or ajaxSpider!", scanContext.getContextName());
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(
                    new SecHubMessage(SecHubMessageType.WARNING, "Skipped the active scan, because no URLs were detected by the crawler! "
                            + "Please check if the URL you specified or any of the includes are accessible."));
            return;
        }
        String url = scanContext.getTargetUrlAsString();
        boolean recurse = true;
        String scanpolicyname = null;
        String method = null;
        String postdata = null;
        LOG.info("For scan {}: Starting authenticated ActiveScan.", scanContext.getContextName());
        /* @formatter:off */
        int scanId =
                clientApiWrapper.startActiveScanAsUser(
                                                url,
                                                scanContext.getZapContextId(),
                                                userId,
                                                recurse,
                                                scanpolicyname,
                                                method,
                                                postdata);
		/* @formatter:on */
        waitForActiveScanResults(scanId);
    }

    /**
     * Wait for the results of the ajax spider. Periodically checks the progress of
     * the ajax spider.
     *
     * @throws ClientApiException
     */
    void waitForAjaxSpiderResults() throws ClientApiException {
        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();

        String ajaxSpiderStatus = "Running";
        while (!isAjaxSpiderStopped(ajaxSpiderStatus)) {
            if (zapPDSEventHandler.isScanCancelled()) {
                clientApiWrapper.stopAjaxSpider();
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            ajaxSpiderStatus = clientApiWrapper.getAjaxSpiderStatus();
            LOG.info("For scan {}: AjaxSpider status {}", scanContext.getContextName(), ajaxSpiderStatus);
        }
        /* stop spider - otherwise running in background */
        clientApiWrapper.stopAjaxSpider();
        LOG.info("For scan {}: AjaxSpider completed.", scanContext.getContextName());
    }

    /**
     * Wait for the results of the spider. Periodically checks the progress of the
     * spider.
     *
     * @param scanId
     * @throws ClientApiException
     */
    void waitForSpiderResults(int scanId) throws ClientApiException {
        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();
        WebLoginVerificationConfiguration verification = scanContext.getVerificationFromConfig();

        int progressSpider = 0;
        while (progressSpider < 100) {
            if (zapPDSEventHandler.isScanCancelled()) {
                clientApiWrapper.stopSpiderScan(scanId);
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            progressSpider = clientApiWrapper.getSpiderStatusForScan(scanId);

            // if verification is set, check if the scan is still logged in
            if (verification != null) {
                if (!clientApiWrapper.isZapLoggedIn(verification)) {
                    LOG.info("For scan {}: Performing a re-login.", scanContext.getContextName());
                    clientApiWrapper.pauseSpiderScan(scanId);
                    setupLoginInsideZapContext();
                    clientApiWrapper.resumeSpiderScan(scanId);
                }
            }
            LOG.info("For scan {}: Spider progress {}%", scanContext.getContextName(), progressSpider);
        }
        /* stop spider - otherwise running in background */
        clientApiWrapper.stopSpiderScan(scanId);

        long numberOfSpiderResults = clientApiWrapper.logFullSpiderResults(scanId);
        scanContext.getZapProductMessageHelper()
                .writeSingleProductMessage(new SecHubMessage(SecHubMessageType.INFO, "Scanned %s URLs during the scan.".formatted(numberOfSpiderResults)));
        LOG.info("For scan {}: Spider completed.", scanContext.getContextName());
    }

    /**
     * Wait for the results of the passive scan. Periodically checks the progress of
     * the passive scan.
     *
     * @throws ClientApiException
     */
    void runAndWaitForPassiveScan() throws ClientApiException {
        LOG.info("For scan {}: Starting passive scan.", scanContext.getContextName());
        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();

        int numberOfRecords = clientApiWrapper.getNumberOfPassiveScannerRecordsToScan();
        while (numberOfRecords > 0) {
            if (zapPDSEventHandler.isScanCancelled()) {
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            numberOfRecords = clientApiWrapper.getNumberOfPassiveScannerRecordsToScan();
            LOG.info("For scan {}: Passive scan number of records left for scanning: {}", scanContext.getContextName(), numberOfRecords);
        }
        LOG.info("For scan {}: Passive scan completed.", scanContext.getContextName());
    }

    /**
     * Wait for the results of the active scan. Periodically checks the progress of
     * the active scan.
     *
     * @param scanId the scan id of the active scan
     * @throws ClientApiException
     */
    void waitForActiveScanResults(int scanId) throws ClientApiException {
        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();
        WebLoginVerificationConfiguration verification = scanContext.getVerificationFromConfig();

        int progressActive = 0;
        while (progressActive < 100) {
            if (zapPDSEventHandler.isScanCancelled()) {
                clientApiWrapper.stopActiveScan(scanId);
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            progressActive = clientApiWrapper.getActiveScannerStatusForScan(scanId);

            // if verification is set, check if the scan is still logged in
            if (verification != null) {
                if (!clientApiWrapper.isZapLoggedIn(verification)) {
                    LOG.info("For scan {}: Performing a re-login.", scanContext.getContextName());
                    clientApiWrapper.pauseActiveScan(scanId);
                    setupLoginInsideZapContext();
                    clientApiWrapper.resumeActiveScan(scanId);
                }
            }
            LOG.info("For scan {}: Active scan progress {}%", scanContext.getContextName(), progressActive);

        }
        clientApiWrapper.stopActiveScan(scanId);
        LOG.info("For scan {}: Active scan completed.", scanContext.getContextName());
    }

    private UserInformation initBasicAuthentication(BasicLoginConfiguration basicLoginConfiguration) throws ClientApiException {
        String realm = "";
        if (basicLoginConfiguration.getRealm().isPresent()) {
            realm = basicLoginConfiguration.getRealm().get();
        }
        String port = Integer.toString(scanContext.getTargetUrl().getPort());
        /* @formatter:off */
		StringBuilder authMethodConfigParams = new StringBuilder();
		authMethodConfigParams.append("hostname=").append(urlEncodeUTF8(scanContext.getTargetUrl().getHost()))
							  .append("&realm=").append(urlEncodeUTF8(realm))
							  .append("&port=").append(urlEncodeUTF8(port));
		/* @formatter:on */
        LOG.info("For scan {}: Setting basic authentication.", scanContext.getContextName());
        String authMethodName = ZapAuthenticationType.HTTP_BASIC_AUTHENTICATION.getZapAuthenticationMethod();
        clientApiWrapper.setAuthenticationMethod(scanContext.getZapContextId(), authMethodName, authMethodConfigParams.toString());

        String methodName = ZapSessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getZapSessionManagementMethod();

        // methodconfigparams in case of http basic auth is null, because it is
        // configured automatically
        String methodconfigparams = null;
        clientApiWrapper.setSessionManagementMethod(scanContext.getZapContextId(), methodName, methodconfigparams);

        /* @formatter:off */
        String username = new String(basicLoginConfiguration.getUser());
        String password = new String(basicLoginConfiguration.getPassword());
        StringBuilder authCredentialsConfigParams = new StringBuilder();
        authCredentialsConfigParams.append("username=").append(urlEncodeUTF8(username))
                                   .append("&password=").append(urlEncodeUTF8(password));
        /* @formatter:on */

        return setupScanUserForZapContext(username, authCredentialsConfigParams.toString());
    }

    private UserInformation setupScanUserForZapContext(String username, String authCredentialsConfigParams) throws ClientApiException {
        int userId = clientApiWrapper.createNewUser(scanContext.getZapContextId(), username);

        LOG.info("For scan {}: Setting up user.", scanContext.getContextName());
        clientApiWrapper.configureAuthenticationCredentials(scanContext.getZapContextId(), userId, authCredentialsConfigParams.toString());
        boolean enabled = true;
        clientApiWrapper.setUserEnabled(scanContext.getZapContextId(), userId, enabled);

        clientApiWrapper.setForcedUser(scanContext.getZapContextId(), userId);
        clientApiWrapper.setForcedUserModeEnabled(true);

        UserInformation userInfo = new UserInformation(username, userId);
        return userInfo;
    }

    private void setupAuthenticationAndSessionManagementMethodForScriptLogin() throws ClientApiException {
        clientApiWrapper.setManualAuthenticationMethod(scanContext.getZapContextId());
        clientApiWrapper.setCookieBasedSessionManagementMethod(scanContext.getZapContextId());
    }

    private boolean isAjaxSpiderStopped(String status) {
        return "stopped".equals(status);
    }

    private String resolveParentDirectoryPath(Path reportFile) {
        if (reportFile == null) {
            throw new ZapWrapperRuntimeException("For scan: " + scanContext.getContextName() + ". Report file not set.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (Files.isDirectory(reportFile)) {
            throw new ZapWrapperRuntimeException("For scan: " + scanContext.getContextName() + ". Report file cannot be a directory!",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }

        Path parent = reportFile.getParent();
        Path absolutePath = parent.toAbsolutePath();

        return absolutePath.toString();
    }

    /**
     * This method is used to rename the file back to the specified name in case the
     * file did not end with .json.
     *
     * The reason for this method is that the Zap appends ".json" to the result file
     * if we generate a report in json format. The PDS result.txt will then be
     * called result.txt.json. Because of this behaviour the file will be renamed.
     */
    private void renameReportFileToOriginalNameIfNecessary() {
        String specifiedReportFile = scanContext.getReportFile().toAbsolutePath().toFile().getAbsolutePath();
        // If the Zap creates the file below, it will be renamed to the originally
        // specified name
        File zapCreatedFile = new File(specifiedReportFile + ".json");
        if (zapCreatedFile.exists()) {
            try {
                Path zapReport = Paths.get(specifiedReportFile + ".json");
                Files.move(zapReport, zapReport.resolveSibling(scanContext.getReportFile().toAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ZapWrapperRuntimeException("For scan: " + scanContext.getContextName() + ". An error occurred renaming the report file", e,
                        ZapWrapperExitCode.IO_ERROR);
            }
        }
    }

    private void cleanUpReplacerRules() throws ClientApiException {
        if (scanContext.getSecHubWebScanConfiguration().getHeaders().isEmpty()) {
            return;
        }

        // Replacer rules are persistent even after restarting ZAP
        // This means we need to cleanUp after every scan.
        LOG.info("Start cleaning up replacer rules.");
        List<HTTPHeaderConfiguration> httpHeaders = scanContext.getSecHubWebScanConfiguration().getHeaders().get();
        for (HTTPHeaderConfiguration httpHeader : httpHeaders) {
            if (httpHeader.getOnlyForUrls().isEmpty()) {
                String description = httpHeader.getName();
                clientApiWrapper.removeReplacerRule(description);
            } else {
                for (String onlyForUrl : httpHeader.getOnlyForUrls().get()) {
                    String description = onlyForUrl;
                    clientApiWrapper.removeReplacerRule(description);
                }
            }
        }
    }

    private String readHeaderValueFromFile(HTTPHeaderConfiguration httpHeader) {
        File headerFile = null;
        headerFile = scanContext.getHeaderValueFiles().getOrDefault(httpHeader.getName(), null);
        try {
            if (headerFile != null) {
                return Files.readString(headerFile.getAbsoluteFile().toPath());
            }
        } catch (IOException e) {
            SecHubMessage message = new SecHubMessage(SecHubMessageType.ERROR, "Could not read header value from file: " + headerFile);
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(message);
            throw new ZapWrapperRuntimeException(message.getText(), e, ZapWrapperExitCode.IO_ERROR);
        }
        return null;
    }

    private String urlEncodeUTF8(String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("This should not happen because we always use UTF-8: " + e);
        }
    }

    private void addXSecHubDASTHeader() throws ClientApiException {
        // description specifies the rule name, which will be set later in this method
        String description = X_SECHUB_DAST_HEADER_NAME;

        boolean enabled = true;
        // "REQ_HEADER" means the header entry will be added to the requests if not
        // existing or replaced if already existing
        String matchtype = "REQ_HEADER";
        boolean matchregex = false;

        // matchstring and replacement will be set to the header name and header value
        String matchstring = X_SECHUB_DAST_HEADER_NAME;
        String replacement = "SecHub DAST job: %s".formatted(scanContext.getContextName());

        // setting initiators to null means all initiators (ZAP components),
        // this means spider, active scan, etc will send this rule for their requests.
        String initiators = null;
        // default URL is null which means the header would be send on any request to
        // any URL
        String url = null;

        LOG.info("Add '{}' replacer rule.", X_SECHUB_DAST_HEADER_NAME);
        clientApiWrapper.addReplacerRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
    }

    private boolean scriptLoginConfigured() {
        return scanContext.getGroovyScriptLoginFile() != null && !scanContext.getTemplateVariables().isEmpty();
    }

    private void addDefaultExcludes() throws ClientApiException {
        clientApiWrapper.addExcludeUrlPatternToContext(scanContext.getContextName(), DEFAULT_EXCLUDE);
    }

    record UserInformation(String userName, int zapuserId) {
    }

}
