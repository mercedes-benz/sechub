// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.OwaspZapFullRuleset;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.Rule;
import com.mercedesbenz.sechub.owaspzapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapApiResponseHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.ScanDurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubIncludeExcludeToOwaspZapURIHelper;

public abstract class AbstractScan implements OwaspZapScan {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractScan.class);

    private static final int CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS = 3000;

    private ScanDurationHelper scanDurationHelper;
    private long remainingScanTime;

    private SecHubIncludeExcludeToOwaspZapURIHelper includeExcludeConverter;

    protected ClientApi clientApi;
    protected OwaspZapScanConfiguration scanConfig;

    protected String contextId;
    protected OwaspZapApiResponseHelper apiResponseHelper;

    public AbstractScan(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) {
        this.clientApi = clientApi;
        this.scanConfig = scanConfig;
        this.scanDurationHelper = new ScanDurationHelper();
        this.remainingScanTime = scanConfig.getMaxScanDurationInMillis();
        this.includeExcludeConverter = new SecHubIncludeExcludeToOwaspZapURIHelper();
        this.apiResponseHelper = new OwaspZapApiResponseHelper();
    }

    @Override
    public void scan() {
        try {
            scanUnsafe();
        } catch (ClientApiException e) {
            throw new ZapWrapperRuntimeException("For scan: " + scanConfig.getContextName() + ". An error occured while scanning!", e,
                    ZapWrapperExitCode.EXECUTION_FAILED);
        }
    }

    /**
     * Creates a new scan context.
     *
     * @throws ClientApiException
     */
    protected void createContext() throws ClientApiException {
        LOG.info("Creating context: {}", scanConfig.getContextName());
        ApiResponse createContextRepsonse = clientApi.context.newContext(scanConfig.getContextName());
        this.contextId = apiResponseHelper.getIdOfApiRepsonse(createContextRepsonse);
    }

    /**
     * Adds all included and excluded URL into scan context.
     *
     * @throws ClientApiException
     */
    protected void addIncludedAndExcludedUrlsToContext() throws ClientApiException {
        LOG.info("For scan {}: Adding include and exclude parts.", scanConfig.getContextName());
        registerUrlsIncludedInContext();
        registerUrlsExcludedFromContext();
    }

    /**
     * Wait for the results of the spider. Periodically checks the progress of the
     * spider.
     *
     * @param response
     * @throws ClientApiException
     */
    protected void waitForSpiderResults(ApiResponse response) throws ClientApiException {
        String scanId = ((ApiResponseElement) response).getValue();
        int progressSpider = 0;

        long startTime = System.currentTimeMillis();
        long maxDuration = scanDurationHelper.computeSpiderMaxScanDuration(scanConfig.isActiveScanEnabled(), scanConfig.isAjaxSpiderEnabled(),
                remainingScanTime);

        boolean timeOut = false;

        while (progressSpider < 100 && !timeOut) {
            waitForNextCheck();
            progressSpider = Integer.parseInt(((ApiResponseElement) clientApi.spider.status(scanId)).getValue());
            LOG.info("For scan {}: Spider progress {}%", scanConfig.getContextName(), progressSpider);

            if (scanConfig.isVerboseOutput()) {
                List<ApiResponse> spiderResults = ((ApiResponseList) clientApi.spider.results(scanId)).getItems();
                for (ApiResponse result : spiderResults) {
                    LOG.info("For scan {}: Result: {}", scanConfig.getContextName(), result.toString());
                }
            }
            timeOut = System.currentTimeMillis() - startTime > maxDuration;
        }
        /* stop spider - otherwise running in background */
        clientApi.spider.stop(scanId);

        LOG.info("For scan {}: Spider completed.", scanConfig.getContextName());
        remainingScanTime = remainingScanTime - (System.currentTimeMillis() - startTime);
    }

    /**
     * Wait for the results of the ajax spider. Periodically checks the progress of
     * the ajax spider.
     *
     * @param response
     * @throws ClientApiException
     */
    protected void waitForAjaxSpiderResults(ApiResponse response) throws ClientApiException {
        String ajaxSpiderStatus = null;

        long startTime = System.currentTimeMillis();
        long maxDuration = scanDurationHelper.computeAjaxSpiderMaxScanDuration(scanConfig.isActiveScanEnabled(), remainingScanTime);

        boolean timeOut = false;

        while (!isAjaxSpiderStopped(ajaxSpiderStatus) && !timeOut) {
            waitForNextCheck();
            ajaxSpiderStatus = ((ApiResponseElement) clientApi.ajaxSpider.status()).getValue();
            LOG.info("For scan {}: AjaxSpider status {}", scanConfig.getContextName(), ajaxSpiderStatus);

            if (scanConfig.isVerboseOutput()) {
                String numberOfResults = ((ApiResponseElement) clientApi.ajaxSpider.numberOfResults()).getValue();
                // Every iteration that checks the progress of the ajax spider logs the
                // currently available results if verbose output is enabled.
                // The results are fetched from the first index "0" (string instead of integer
                // in this API call).
                List<ApiResponse> spiderResults = ((ApiResponseList) clientApi.ajaxSpider.results("0", numberOfResults)).getItems();
                for (ApiResponse result : spiderResults) {
                    LOG.info("For scan {}: Result: {}", scanConfig.getContextName(), result.toString(0));
                }
            }
            timeOut = (System.currentTimeMillis() - startTime) > maxDuration;
        }
        /* stop spider - otherwise running in background */
        clientApi.ajaxSpider.stop();
        LOG.info("For scan {}: AjaxSpider completed.", scanConfig.getContextName());
        remainingScanTime = remainingScanTime - (System.currentTimeMillis() - startTime);
    }

    /**
     * Wait for the results of the passive scan. Periodically checks the progress of
     * the passive scan.
     *
     * @throws ClientApiException
     */
    protected void passiveScan() throws ClientApiException {
        LOG.info("For scan {}: Starting passive scan.", scanConfig.getContextName());
        long startTime = System.currentTimeMillis();
        long maxDuration = scanDurationHelper.computePassiveScanMaxScanDuration(scanConfig.isActiveScanEnabled(), scanConfig.isAjaxSpiderEnabled(),
                remainingScanTime);

        int numberOfRecords = Integer.parseInt(((ApiResponseElement) clientApi.pscan.recordsToScan()).getValue());

        while (numberOfRecords > 0 || (System.currentTimeMillis() - startTime) > maxDuration) {
            waitForNextCheck();
            clientApi.pscan.recordsToScan();
            numberOfRecords = Integer.parseInt(((ApiResponseElement) clientApi.pscan.recordsToScan()).getValue());
            LOG.info("For scan {}: Passive scan number of records left for scanning: {}", scanConfig.getContextName(), numberOfRecords);
        }
        LOG.info("For scan {}: Passive scan completed.", scanConfig.getContextName());
        remainingScanTime = remainingScanTime - (System.currentTimeMillis() - startTime);
    }

    /**
     * Wait for the results of the active scan. Periodically checks the progress of
     * the active scan.
     *
     * @param response
     * @throws ClientApiException
     */
    protected void waitForActiveScanResults(ApiResponse response) throws ClientApiException {
        String scanId = ((ApiResponseElement) response).getValue();
        int progressActive = 0;

        long startTime = System.currentTimeMillis();
        long maxDuration = remainingScanTime;
        boolean timeOut = false;
        while (progressActive < 100 && !timeOut) {
            waitForNextCheck();

            progressActive = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanId)).getValue());
            LOG.info("For scan {}: Active scan progress {}%", scanConfig.getContextName(), progressActive);

            timeOut = (System.currentTimeMillis() - startTime) > maxDuration;
        }
        clientApi.ascan.stop(scanId);
        LOG.info("For scan {}: Active scan completed.", scanConfig.getContextName());
    }

    /**
     * Generates the SARIF report for the current scan, identified using the context
     * name.
     *
     * @throws ClientApiException
     */
    protected void generateOwaspZapReport() throws ClientApiException {
        LOG.info("For scan {}: Writing results to report...", scanConfig.getContextName());
        Path reportFile = scanConfig.getReportFile();

        String title = scanConfig.getContextName();
        String template = "sarif-json";
        String theme = null;
        String description = null;
        String contexts = scanConfig.getContextName();
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
			clientApi.reports.generate(
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
					display);
			/* @formatter:on */

        // rename is necessary if the file extension is not .json, because Owasp Zap
        // adds the file extension .json since we create a json report. Might not be
        // necessary anymore if we have the sarif support
        renameReportFileIfFileExtensionIsNotJSON();

        LOG.info("For scan {}: Report can be found at {}", scanConfig.getContextName(), reportFile.toFile().getAbsolutePath());

    }

    protected void cleanUp() throws ClientApiException {
        // to ensure parts from previous scan are deleted
        LOG.info("Cleaning up by starting new and empty session...", scanConfig.getContextName());
        clientApi.core.newSession("Cleaned after scan", "true");
        LOG.info("Cleanup successful.");
    }

    protected void setupBasicConfiguration() throws ClientApiException {
        LOG.info("Creating new session inside the Owasp Zap");
        // to ensure parts from previous scan are deleted
        clientApi.core.newSession(scanConfig.getContextName(), "true");
        LOG.info("Setting default of how many alerts of the same rule will be inside the report to unlimited.");
        // setting this value to zero means unlimited
        clientApi.core.setOptionMaximumAlertInstances("0");

        // enable all passive scanner rules by default
        clientApi.pscan.enableAllScanners();
        // enable all passive scanner rules by default
        // null specifies the default scan policy
        clientApi.ascan.enableAllScanners(null);

        // use firefox in headless mode by default
        clientApi.ajaxSpider.setOptionBrowserId("firefox-headless");
    }

    protected void setupAdditonalProxyConfiguration() throws ClientApiException {
        ProxyInformation proxyInformation = scanConfig.getProxyInformation();
        if (proxyInformation != null) {
            String proxyHost = proxyInformation.getHost();
            int proxyPort = proxyInformation.getPort();
            LOG.info("Using proxy {}:{} to reach target.", proxyHost, proxyPort);
            clientApi.network.setHttpProxy(proxyHost, ""+proxyPort, null, null, null);
            clientApi.network.setHttpProxyEnabled("true");
            clientApi.network.setHttpProxyAuthEnabled("false");
        } else {
            LOG.info("No proxy was set, continuing without proxy.");
            clientApi.network.setHttpProxyEnabled("false");
        }
    }

    protected void deactivateRules() throws ClientApiException {
        OwaspZapFullRuleset fullRuleset = scanConfig.getFullRuleset();
        DeactivatedRuleReferences deactivatedRuleReferences = scanConfig.getDeactivatedRuleReferences();
        if (fullRuleset == null && deactivatedRuleReferences == null) {
            return;
        }
        List<RuleReference> rulesReferences = deactivatedRuleReferences.getDeactivatedRuleReferences();
        if (rulesReferences == null) {
            return;
        }

        for (RuleReference ruleRef : rulesReferences) {
            Rule ruleToDeactivate = fullRuleset.findRuleByReference(ruleRef.getReference());
            if (isPassiveRule(ruleToDeactivate.getType())) {
                clientApi.pscan.disableScanners(ruleToDeactivate.getId());
            } else if (isActiveRule(ruleToDeactivate.getType())) {
                // null specifies the default scan policy
                clientApi.ascan.disableScanners(ruleToDeactivate.getId(), null);
            }
        }
    }

    protected void loadApiDefinitions() throws ClientApiException {
        if (scanConfig.getApiDefinitionFile() == null) {
            LOG.info("For scan {}: No file with API definition found!", scanConfig.getContextName());
            return;
        }
        Optional<SecHubWebScanApiConfiguration> apiConfig = scanConfig.getSecHubWebScanConfiguration().getApi();
        if (!apiConfig.isPresent()) {
            throw new ZapWrapperRuntimeException("For scan :" + scanConfig.getContextName() + " No API type was definied!",
                    ZapWrapperExitCode.SECHUB_CONFIGURATION_INVALID);
        }

        switch (apiConfig.get().getType()) {
        case OPEN_API:
            clientApi.openapi.importFile(scanConfig.getApiDefinitionFile().toString(), scanConfig.getTargetUriAsString(), contextId);
            break;
        default:
            // should never happen since API type is an Enum
            // Failure should happen before getting here
            throw new ZapWrapperRuntimeException("For scan :" + scanConfig.getContextName() + " Unknown API type was definied!",
                    ZapWrapperExitCode.SECHUB_CONFIGURATION_INVALID);
        }
    }

    private boolean isPassiveRule(String type) {
        return "passive".equals(type.toLowerCase());
    }

    private boolean isActiveRule(String type) {
        return "active".equals(type.toLowerCase());
    }

    private void scanUnsafe() throws ClientApiException {
        setupBasicConfiguration();
        deactivateRules();
        setupAdditonalProxyConfiguration();

        createContext();
        addIncludedAndExcludedUrlsToContext();
        loadApiDefinitions();
        if (scanConfig.isAjaxSpiderEnabled()) {
            runAjaxSpider();
        }

        runSpider();

        passiveScan();

        if (scanConfig.isActiveScanEnabled()) {
            runActiveScan();
        }
        generateOwaspZapReport();

        cleanUp();
    }

    private void waitForNextCheck() {
        try {
            Thread.sleep(CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isAjaxSpiderStopped(String status) {
        return "stopped".equals(status);
    }

    private String resolveParentDirectoryPath(Path reportFile) {
        if (reportFile == null) {
            throw new ZapWrapperRuntimeException("For scan: " + scanConfig.getContextName() + ". Report file not set.", ZapWrapperExitCode.REPORT_FILE_ERROR);
        }
        if (Files.isDirectory(reportFile)) {
            throw new ZapWrapperRuntimeException("For scan: " + scanConfig.getContextName() + ". Report file must not be a directory!",
                    ZapWrapperExitCode.REPORT_FILE_ERROR);
        }

        Path parent = reportFile.getParent();
        Path absolutePath = parent.toAbsolutePath();

        return absolutePath.toString();
    }

    /**
     * This method is used to rename the file back to the specified name in case the
     * file did not end with .json.
     *
     * The reason for this method is that the Owasp Zap appends ".json" to the
     * result file if we generate a report in json format. The PDS result.txt will
     * then be called result.txt.json. Because of this behaviour the file will be
     * renamed.
     */
    private void renameReportFileIfFileExtensionIsNotJSON() {
        String specifiedReportFile = scanConfig.getReportFile().toAbsolutePath().toFile().getAbsolutePath();
        // If the Owasp Zap creates the file below, it will be renamed to the originally
        // specified name
        File owaspZapCreatedFile = new File(specifiedReportFile + ".json");
        if (owaspZapCreatedFile.exists()) {
            try {
                Path owaspzapReport = Paths.get(specifiedReportFile + ".json");
                Files.move(owaspzapReport, owaspzapReport.resolveSibling(scanConfig.getReportFile().toAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {

                throw new ZapWrapperRuntimeException("For scan: " + scanConfig.getContextName() + ". An error occurred renaming the report file", e,
                        ZapWrapperExitCode.REPORT_FILE_ERROR);
            }
        }
    }

    private void registerUrlsIncludedInContext() throws ClientApiException {
        clientApi.context.includeInContext(scanConfig.getContextName(), scanConfig.getTargetUriAsString() + ".*");

        if (scanConfig.getSecHubWebScanConfiguration().getIncludes().isPresent()) {
            List<String> includedUrls = includeExcludeConverter.createListOfUrls(scanConfig.getTargetUriAsString(),
                    scanConfig.getSecHubWebScanConfiguration().getIncludes().get());

            for (String url : includedUrls) {
                clientApi.context.includeInContext(scanConfig.getContextName(), url);
            }
        }
    }

    private void registerUrlsExcludedFromContext() throws ClientApiException {
        if (scanConfig.getSecHubWebScanConfiguration().getExcludes().isPresent()) {
            List<String> excludedUrls = includeExcludeConverter.createListOfUrls(scanConfig.getTargetUriAsString(),
                    scanConfig.getSecHubWebScanConfiguration().getExcludes().get());

            for (String url : excludedUrls) {
                clientApi.context.excludeFromContext(scanConfig.getContextName(), url);
            }
        }
    }

    /**
     * Runs classical spider (suitable for web applications) - just parsing....
     * creates tree
     *
     * @throws ClientApiException
     */
    protected abstract void runSpider() throws ClientApiException;

    /**
     * Runs web driver oriented spider (suitable for single page applications)- just
     * clicking.... creates tree
     *
     * @throws ClientApiException
     */
    protected abstract void runAjaxSpider() throws ClientApiException;

    /**
     * Attacks the target
     *
     * @throws ClientApiException
     */
    protected abstract void runActiveScan() throws ClientApiException;

}
