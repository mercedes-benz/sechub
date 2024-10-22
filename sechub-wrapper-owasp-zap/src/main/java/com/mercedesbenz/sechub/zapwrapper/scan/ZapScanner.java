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
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.auth.SessionManagementType;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.Rule;
import com.mercedesbenz.sechub.zapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.ScanDurationHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiFacade;
import com.mercedesbenz.sechub.zapwrapper.util.SystemUtil;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

public class ZapScanner implements ZapScan {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScanner.class);
    static final int CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS = 5000;

    public static final String X_SECHUB_DAST_HEADER_NAME = "x-sechub-dast";

    ClientApiFacade clientApiFacade;
    ZapScanContext scanContext;

    ScanDurationHelper scanDurationHelper;
    UrlUtil urlUtil;
    SystemUtil systemUtil;

    long remainingScanTime;

    TextFileReader fileReader;

    public static ZapScanner from(ClientApiFacade clientApiFacade, ZapScanContext scanContext) {
        if (clientApiFacade == null) {
            throw new ZapWrapperRuntimeException("Cannot create Zap Scanner because ClientApiFacade is null!", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }

        if (scanContext == null) {
            throw new ZapWrapperRuntimeException("Cannot create Zap Scanner because ZapScanContext is null!", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }

        ScanDurationHelper scanDurationHelper = new ScanDurationHelper();
        UrlUtil urlUtil = new UrlUtil();
        SystemUtil systemUtil = new SystemUtil();

        return new ZapScanner(clientApiFacade, scanContext, scanDurationHelper, urlUtil, systemUtil);
    }

    private ZapScanner(ClientApiFacade clientApiFacade, ZapScanContext scanContext, ScanDurationHelper scanDurationHelper, UrlUtil urlUtil,
            SystemUtil systemUtil) {
        this.clientApiFacade = clientApiFacade;
        this.scanContext = scanContext;

        this.scanDurationHelper = scanDurationHelper;
        this.urlUtil = urlUtil;
        this.systemUtil = systemUtil;

        this.remainingScanTime = scanContext.getMaxScanDurationInMilliSeconds();

        this.fileReader = new TextFileReader();
    }

    @Override
    public void scan() throws ZapWrapperRuntimeException {
        try {
            /* ZAP setup on local machine */
            setupStandardConfiguration();
            deactivateRules(scanContext.getFullRuleset(), scanContext.getDeactivatedRuleReferences());
            setupAdditonalProxyConfiguration(scanContext.getProxyInformation());
            String zapContextId = createContext();
            addXSecHubDASTHeader();
            addReplacerRulesForHeaders();

            /* ZAP setup with access to target */
            // The order of the following method calls is important. We want to load the
            // client certificate first, because it could be needed to access the included
            // URLs or the URLs from the API definitions.
            importClientCertificate();
            addIncludedAndExcludedUrlsToContext();
            loadApiDefinitions(zapContextId);

            /* ZAP scan */
            executeScan(zapContextId);

            /* After scan */
            generateZapReport();
            cleanUp();
        } catch (ClientApiException e) {
            cleanUp();
            throw new ZapWrapperRuntimeException("For scan: " + scanContext.getContextName() + ". An error occured while scanning!", e,
                    ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        }
    }

    void setupStandardConfiguration() throws ClientApiException {
        LOG.info("Creating new session inside the Zap");
        // to ensure parts from previous scan are deleted
        clientApiFacade.createNewSession(scanContext.getContextName(), "true");

        LOG.info("Setting default maximum number of alerts for each rule.");
        // setting this value to zero means unlimited
        clientApiFacade.configureMaximumAlertsForEachRule("0");

        LOG.info("Enable all passive scan rules before configuration begins.");
        // enable all passive scanner rules by default
        clientApiFacade.enableAllPassiveScannerRules();

        LOG.info("Enable all active scan rules before configuration begins.");
        // enable all passive scanner rules by default
        // null specifies the default scan policy
        clientApiFacade.enableAllActiveScannerRulesForPolicy(null);

        LOG.info("Set browser for ajaxSpider.");
        // use firefox in headless mode by default
        clientApiFacade.configureAjaxSpiderBrowserId(scanContext.getAjaxSpiderBrowserId());
    }

    void deactivateRules(ZapFullRuleset fullRuleset, DeactivatedRuleReferences deactivatedRuleReferences) throws ClientApiException {
        if (fullRuleset == null || deactivatedRuleReferences == null) {
            return;
        }
        List<RuleReference> rulesReferences = deactivatedRuleReferences.getDeactivatedRuleReferences();
        if (rulesReferences == null) {
            return;
        }

        for (RuleReference ruleRef : rulesReferences) {
            Rule ruleToDeactivate = fullRuleset.findRuleByReference(ruleRef.getReference());
            if (isPassiveRule(ruleToDeactivate.getType())) {
                LOG.info("Deactivate passive scanner rule: {} ", ruleRef.getReference());
                clientApiFacade.disablePassiveScannerRule(ruleToDeactivate.getId());
            } else if (isActiveRule(ruleToDeactivate.getType())) {
                LOG.info("Deactivate active scanner rule: {} ", ruleRef.getReference());
                // null specifies the default scan policy
                clientApiFacade.disableActiveScannerRuleForPolicy(ruleToDeactivate.getId(), null);
            }
        }
    }

    void setupAdditonalProxyConfiguration(ProxyInformation proxyInformation) throws ClientApiException {
        if (proxyInformation != null) {
            String proxyHost = proxyInformation.getHost();
            int proxyPort = proxyInformation.getPort();
            LOG.info("Using proxy {}:{} to reach target.", proxyHost, proxyPort);
            clientApiFacade.configureHttpProxy(proxyHost, "" + proxyPort, null, null, null);
            clientApiFacade.setHttpProxyEnabled("true");
            clientApiFacade.setHttpProxyAuthEnabled("false");
        } else {
            LOG.info("No proxy was set, continuing without proxy.");
            clientApiFacade.setHttpProxyEnabled("false");
        }
    }

    /**
     * Creates new context in the current ZAP session.
     *
     * @return the context id returned by the ZAP API
     * @throws ClientApiException
     */
    String createContext() throws ClientApiException {
        LOG.info("Creating context: {}", scanContext.getContextName());
        return clientApiFacade.createNewContext(scanContext.getContextName());
    }

    void addReplacerRulesForHeaders() throws ClientApiException {
        if (scanContext.getSecHubWebScanConfiguration().getHeaders().isEmpty()) {
            LOG.info("No headers were configured inside the sechub webscan configuration.");
            return;
        }

        // description specifies the rule name, which will be set later in this method
        String description = null;

        String enabled = "true";
        // "REQ_HEADER" means the header entry will be added to the requests if not
        // existing or replaced if already existing
        String matchtype = "REQ_HEADER";
        String matchregex = "false";

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
                clientApiFacade.addReplacerRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
            } else {
                for (String onlyForUrl : httpHeader.getOnlyForUrls().get()) {
                    // we need to create a rule for each onlyForUrl pattern on each header
                    description = onlyForUrl;
                    url = urlUtil.replaceWebScanWildCardsWithRegexInString(onlyForUrl);
                    clientApiFacade.addReplacerRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
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
        String followRedirects = "false";
        for (String url : scanContext.getZapURLsIncludeSet()) {
            clientApiFacade.addIncludeUrlPatternToContext(scanContext.getContextName(), url);
            // Cannot not perform initial connection check to included URL with wildcards
            if (url.contains(".*")) {
                LOG.info("For scan {}: Cannot not perform initial connection check to included URL: {} because it contains wildcards.",
                        scanContext.getContextName(), url);
            } else {
                clientApiFacade.accessUrlViaZap(url, followRedirects);
            }
        }

        LOG.info("For scan {}: Adding exclude parts.", scanContext.getContextName());
        for (String url : scanContext.getZapURLsExcludeSet()) {
            clientApiFacade.addExcludeUrlPatternToContext(scanContext.getContextName(), url);
        }
    }

    void loadApiDefinitions(String zapContextId) throws ClientApiException {
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
                LOG.info("For scan {}: Loading openAPI definition from : {}", scanContext.getContextName(), apiDefinitionUrl.toString());
                clientApiFacade.importOpenApiDefintionFromUrl(apiDefinitionUrl, scanContext.getTargetUrlAsString(), zapContextId);
            }
            for (File apiFile : scanContext.getApiDefinitionFiles()) {
                LOG.info("For scan {}: Loading openAPI file: {}", scanContext.getContextName(), apiFile.toString());
                clientApiFacade.importOpenApiFile(apiFile.toString(), scanContext.getTargetUrlAsString(), zapContextId);
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
        LOG.info("For scan {}: Loading client certificate file: {}", scanContext.getContextName(), clientCertificateFile.getAbsolutePath());
        clientApiFacade.importPkcs12ClientCertificate(clientCertificateFile.getAbsolutePath(), password);
        clientApiFacade.enableClientCertificate();
    }

    void executeScan(String zapContextId) throws ClientApiException {
        UserInformation userInfo = configureLoginInsideZapContext(zapContextId);
        if (userInfo != null) {
            runSpiderAsUser(zapContextId, userInfo.zapuserId);
            passiveScan();
            if (scanContext.isAjaxSpiderEnabled()) {
                runAjaxSpiderAsUser(userInfo.userName);
            }
            if (scanContext.isActiveScanEnabled()) {
                runActiveScanAsUser(zapContextId, userInfo.zapuserId);
            }
        } else {
            runSpider();
            passiveScan();
            if (scanContext.isAjaxSpiderEnabled()) {
                runAjaxSpider();
            }
            if (scanContext.isActiveScanEnabled()) {
                runActiveScan();
            }
        }
    }

    /**
     * Configure login according to the sechub webscan config.
     *
     * @param zapContextId
     * @return UserInformation containing userName and zapUserId or
     *         <code>null</code> if nothing could be configured.
     * @throws ClientApiException
     */
    UserInformation configureLoginInsideZapContext(String zapContextId) throws ClientApiException {
        if (scanContext.getSecHubWebScanConfiguration().getLogin().isEmpty()) {
            LOG.info("For scan {}: No login section detected.", scanContext.getContextName());
            return null;
        }

        WebLoginConfiguration webLoginConfiguration = scanContext.getSecHubWebScanConfiguration().getLogin().get();
        if (webLoginConfiguration.getBasic().isPresent()) {
            LOG.info("For scan {}: Applying basic authentication config.", scanContext.getContextName());
            return initBasicAuthentication(zapContextId, webLoginConfiguration.getBasic().get());
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
        LOG.info("For scan {}: Writing results to report...", scanContext.getContextName());
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
        clientApiFacade.generateReport(
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
        try {
            LOG.info("Cleaning up by starting new and empty session...", scanContext.getContextName());
            clientApiFacade.createNewSession("Cleaned after scan", "true");
            LOG.info("New and empty session inside Zap created.");

            // Replacer rules are persistent even after restarting ZAP
            // This means we need to cleanUp after every scan.
            LOG.info("Start cleaning up replacer rules.");
            cleanUpReplacerRules();

            // Remove x-sechub-dast header replacer rule
            LOG.info("Remove '{}' replacer rule.", X_SECHUB_DAST_HEADER_NAME);
            clientApiFacade.removeReplacerRule(X_SECHUB_DAST_HEADER_NAME);

            // disable client certificate here, the imported client certificate will be
            // removed on ZAP shutdown automatically anyway
            LOG.info("Disable client certificate if one was used for the scan.");
            clientApiFacade.disableClientCertificate();

            LOG.info("Cleanup successful.");
        } catch (ClientApiException e) {
            LOG.error("For scan: {}. An error occurred during the clean up, because: {}", scanContext.getContextName(), e.getMessage());
        }
    }

    void runSpider() throws ClientApiException {
        String contextName = scanContext.getContextName();
        String subTreeOnly = "true";
        String recurse = "true";
        String maxChildren = null;
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        LOG.info("For scan {}: Starting Spider.", contextName);
        /* @formatter:off */
        String scanId =
                clientApiFacade.startSpiderScan(
                                        targetUrlAsString,
                                        maxChildren,
                                        recurse,
                                        contextName,
                                        subTreeOnly);
		/* @formatter:on */
        waitForSpiderResults(scanId);
    }

    void runAjaxSpider() throws ClientApiException {
        String inScope = "true";
        String subTreeOnly = "true";
        String contextName = scanContext.getContextName();
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        LOG.info("For scan {}: Starting AjaxSpider.", scanContext.getContextName());
        /* @formatter:off */
		clientApiFacade.startAjaxSpiderScan(
                                    targetUrlAsString,
                                    inScope,
                                    contextName,
                                    subTreeOnly);
		/* @formatter:on */
        waitForAjaxSpiderResults();
    }

    void runActiveScan() throws ClientApiException {
        // Necessary otherwise the active scanner exits with an exception,
        // if no URLs to scan where detected by the spider/ajaxSpider before
        if (!clientApiFacade.atLeastOneURLDetected()) {
            LOG.warn("For {} skipping active scan, since no URLs where detected by spider or ajaxSpider!", scanContext.getContextName());
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(
                    new SecHubMessage(SecHubMessageType.WARNING, "Skipped the active scan, because no URLs were detected by the crawler! "
                            + "Please check if the URL you specified or any of the includes are accessible."));
            return;
        }
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        String inScopeOnly = "true";
        String recurse = "true";
        String scanPolicyName = null;
        String method = null;
        String postData = null;
        LOG.info("For scan {}: Starting ActiveScan.", scanContext.getContextName());
        /* @formatter:off */
		String scanId =
		        clientApiFacade.startActiveScan(
                                        targetUrlAsString,
                                        recurse,
                                        inScopeOnly,
                                        scanPolicyName,
                                        method,
                                        postData);
		/* @formatter:on */
        waitForActiveScanResults(scanId);
    }

    void runSpiderAsUser(String contextId, String userId) throws ClientApiException {
        String url = scanContext.getTargetUrlAsString();
        String maxchildren = null;
        String recurse = "true";
        String subtreeonly = "true";
        LOG.info("For scan {}: Starting authenticated Spider.", scanContext.getContextName());
        /* @formatter:off */
        String scanId =
                clientApiFacade.startSpiderScanAsUser(
                                            contextId,
                                            userId,
                                            url,
                                            maxchildren,
                                            recurse,
                                            subtreeonly);
		/* @formatter:on */
        waitForSpiderResults(scanId);
    }

    void runAjaxSpiderAsUser(String username) throws ClientApiException {
        String contextname = scanContext.getContextName();
        String url = scanContext.getTargetUrlAsString();
        String subtreeonly = "true";
        LOG.info("For scan {}: Starting authenticated Ajax Spider.", scanContext.getContextName());
        /* @formatter:off */
		clientApiFacade.startAjaxSpiderScanAsUser(
                                            contextname,
                                            username,
                                            url,
                                            subtreeonly);
		/* @formatter:on */
        waitForAjaxSpiderResults();
    }

    void runActiveScanAsUser(String contextId, String userId) throws ClientApiException {
        // Necessary otherwise the active scanner exits with an exception,
        // if no URLs to scan where detected by the spider/ajaxSpider before
        if (!clientApiFacade.atLeastOneURLDetected()) {
            LOG.warn("For {} skipping active scan, since no URLs where detected by spider or ajaxSpider!", scanContext.getContextName());
            scanContext.getZapProductMessageHelper().writeSingleProductMessage(
                    new SecHubMessage(SecHubMessageType.WARNING, "Skipped the active scan, because no URLs were detected by the crawler! "
                            + "Please check if the URL you specified or any of the includes are accessible."));
            return;
        }
        String url = scanContext.getTargetUrlAsString();
        String recurse = "true";
        String scanpolicyname = null;
        String method = null;
        String postdata = null;
        LOG.info("For scan {}: Starting authenticated ActiveScan.", scanContext.getContextName());
        /* @formatter:off */
        String scanId =
                clientApiFacade.startActiveScanAsUser(
                                                url,
                                                contextId,
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
        String ajaxSpiderStatus = null;

        long startTime = systemUtil.getCurrentTimeInMilliseconds();
        long maxDuration = scanDurationHelper.computeAjaxSpiderMaxScanDuration(scanContext.isActiveScanEnabled(), remainingScanTime);

        boolean timeOut = false;

        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();

        while (!isAjaxSpiderStopped(ajaxSpiderStatus) && !timeOut) {
            if (zapPDSEventHandler.isScanCancelled()) {
                clientApiFacade.stopAjaxSpider();
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            ajaxSpiderStatus = clientApiFacade.getAjaxSpiderStatus();
            LOG.info("For scan {}: AjaxSpider status {}", scanContext.getContextName(), ajaxSpiderStatus);
            timeOut = (systemUtil.getCurrentTimeInMilliseconds() - startTime) > maxDuration;
        }
        /* stop spider - otherwise running in background */
        clientApiFacade.stopAjaxSpider();
        LOG.info("For scan {}: AjaxSpider completed.", scanContext.getContextName());
        remainingScanTime = remainingScanTime - (systemUtil.getCurrentTimeInMilliseconds() - startTime);
    }

    /**
     * Wait for the results of the spider. Periodically checks the progress of the
     * spider.
     *
     * @param response
     * @throws ClientApiException
     */
    void waitForSpiderResults(String scanId) throws ClientApiException {
        int progressSpider = 0;

        long startTime = systemUtil.getCurrentTimeInMilliseconds();
        long maxDuration = scanDurationHelper.computeSpiderMaxScanDuration(scanContext.isActiveScanEnabled(), scanContext.isAjaxSpiderEnabled(),
                remainingScanTime);

        boolean timeOut = false;
        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();

        while (progressSpider < 100 && !timeOut) {
            if (zapPDSEventHandler.isScanCancelled()) {
                clientApiFacade.stopSpiderScan(scanId);
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            progressSpider = clientApiFacade.getSpiderStatusForScan(scanId);
            LOG.info("For scan {}: Spider progress {}%", scanContext.getContextName(), progressSpider);
            timeOut = systemUtil.getCurrentTimeInMilliseconds() - startTime > maxDuration;
        }
        /* stop spider - otherwise running in background */
        clientApiFacade.stopSpiderScan(scanId);

        long numberOfSpiderResults = clientApiFacade.logFullSpiderResults(scanId);
        scanContext.getZapProductMessageHelper()
                .writeSingleProductMessage(new SecHubMessage(SecHubMessageType.INFO, "Scanned %s URLs during the scan.".formatted(numberOfSpiderResults)));
        LOG.info("For scan {}: Spider completed.", scanContext.getContextName());
        remainingScanTime = remainingScanTime - (systemUtil.getCurrentTimeInMilliseconds() - startTime);
    }

    /**
     * Wait for the results of the passive scan. Periodically checks the progress of
     * the passive scan.
     *
     * @throws ClientApiException
     */
    void passiveScan() throws ClientApiException {
        LOG.info("For scan {}: Starting passive scan.", scanContext.getContextName());
        long startTime = systemUtil.getCurrentTimeInMilliseconds();
        long maxDuration = scanDurationHelper.computePassiveScanMaxScanDuration(scanContext.isActiveScanEnabled(), scanContext.isAjaxSpiderEnabled(),
                remainingScanTime);

        int numberOfRecords = clientApiFacade.getNumberOfPassiveScannerRecordsToScan();
        boolean timeOut = false;
        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();

        while (numberOfRecords > 0 && !timeOut) {
            if (zapPDSEventHandler.isScanCancelled()) {
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            numberOfRecords = clientApiFacade.getNumberOfPassiveScannerRecordsToScan();
            LOG.info("For scan {}: Passive scan number of records left for scanning: {}", scanContext.getContextName(), numberOfRecords);
            timeOut = systemUtil.getCurrentTimeInMilliseconds() - startTime > maxDuration;
        }
        LOG.info("For scan {}: Passive scan completed.", scanContext.getContextName());
        remainingScanTime = remainingScanTime - (systemUtil.getCurrentTimeInMilliseconds() - startTime);
    }

    /**
     * Wait for the results of the active scan. Periodically checks the progress of
     * the active scan.
     *
     * @param response
     * @throws ClientApiException
     */
    void waitForActiveScanResults(String scanId) throws ClientApiException {
        int progressActive = 0;

        long startTime = systemUtil.getCurrentTimeInMilliseconds();
        long maxDuration = remainingScanTime;
        boolean timeOut = false;

        ZapPDSEventHandler zapPDSEventHandler = scanContext.getZapPDSEventHandler();

        while (progressActive < 100 && !timeOut) {
            if (zapPDSEventHandler.isScanCancelled()) {
                clientApiFacade.stopActiveScan(scanId);
                zapPDSEventHandler.cancelScan(scanContext.getContextName());
            }
            systemUtil.waitForMilliseconds(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
            progressActive = clientApiFacade.getActiveScannerStatusForScan(scanId);
            LOG.info("For scan {}: Active scan progress {}%", scanContext.getContextName(), progressActive);

            timeOut = (systemUtil.getCurrentTimeInMilliseconds() - startTime) > maxDuration;
        }
        clientApiFacade.stopActiveScan(scanId);
        LOG.info("For scan {}: Active scan completed.", scanContext.getContextName());
    }

    private boolean isPassiveRule(String type) {
        return "passive".equals(type.toLowerCase());
    }

    private boolean isActiveRule(String type) {
        return "active".equals(type.toLowerCase());
    }

    private UserInformation initBasicAuthentication(String zapContextId, BasicLoginConfiguration basicLoginConfiguration) throws ClientApiException {
        String realm = "";
        if (basicLoginConfiguration.getRealm().isPresent()) {
            realm = basicLoginConfiguration.getRealm().get();
        }
        String port = "" + scanContext.getTargetUrl().getPort();
        /* @formatter:off */
		StringBuilder authMethodConfigParams = new StringBuilder();
		authMethodConfigParams.append("hostname=").append(urlEncodeUTF8(scanContext.getTargetUrl().getHost()))
							  .append("&realm=").append(urlEncodeUTF8(realm))
							  .append("&port=").append(urlEncodeUTF8(port));
		/* @formatter:on */
        LOG.info("For scan {}: Setting basic authentication.", scanContext.getContextName());
        String authMethodName = scanContext.getAuthenticationType().getZapAuthenticationMethod();
        clientApiFacade.configureAuthenticationMethod(zapContextId, authMethodName, authMethodConfigParams.toString());

        String methodName = SessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getZapSessionManagementMethod();

        // methodconfigparams in case of http basic auth is null, because it is
        // configured automatically
        String methodconfigparams = null;
        clientApiFacade.setSessionManagementMethod(zapContextId, methodName, methodconfigparams);

        return initBasicAuthScanUser(zapContextId, basicLoginConfiguration);
    }

    private UserInformation initBasicAuthScanUser(String zapContextId, BasicLoginConfiguration basicLoginConfiguration) throws ClientApiException {
        String username = new String(basicLoginConfiguration.getUser());
        String password = new String(basicLoginConfiguration.getPassword());

        String userId = clientApiFacade.createNewUser(zapContextId, username);

        /* @formatter:off */
		StringBuilder authCredentialsConfigParams = new StringBuilder();
		authCredentialsConfigParams.append("username=").append(urlEncodeUTF8(username))
								   .append("&password=").append(urlEncodeUTF8(password));
		/* @formatter:on */

        LOG.info("For scan {}: Setting up user.", scanContext.getContextName());
        clientApiFacade.configureAuthenticationCredentials(zapContextId, userId, authCredentialsConfigParams.toString());
        String enabled = "true";
        clientApiFacade.setUserEnabled(zapContextId, userId, enabled);

        clientApiFacade.setForcedUser(zapContextId, userId);
        clientApiFacade.setForcedUserModeEnabled(true);

        UserInformation userInfo = new UserInformation(username, userId);
        return userInfo;
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

        List<HTTPHeaderConfiguration> httpHeaders = scanContext.getSecHubWebScanConfiguration().getHeaders().get();
        for (HTTPHeaderConfiguration httpHeader : httpHeaders) {
            if (httpHeader.getOnlyForUrls().isEmpty()) {
                String description = httpHeader.getName();
                clientApiFacade.removeReplacerRule(description);
            } else {
                for (String onlyForUrl : httpHeader.getOnlyForUrls().get()) {
                    String description = onlyForUrl;
                    clientApiFacade.removeReplacerRule(description);
                }
            }
        }
    }

    private String readHeaderValueFromFile(HTTPHeaderConfiguration httpHeader) {
        File headerFile = null;
        headerFile = scanContext.getHeaderValueFiles().getOrDefault(httpHeader.getName(), null);
        try {
            if (headerFile != null) {
                return fileReader.readTextFromFile(headerFile.getAbsoluteFile());
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

        String enabled = "true";
        // "REQ_HEADER" means the header entry will be added to the requests if not
        // existing or replaced if already existing
        String matchtype = "REQ_HEADER";
        String matchregex = "false";

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
        clientApiFacade.addReplacerRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
    }

    record UserInformation(String userName, String zapuserId) {
    }
}
