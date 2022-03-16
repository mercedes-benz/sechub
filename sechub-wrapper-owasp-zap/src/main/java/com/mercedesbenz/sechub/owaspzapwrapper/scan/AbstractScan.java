// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapApiResponseHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.ScanDurationHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.SecHubIncludeExcludeToOwaspZapURIHelper;

public abstract class AbstractScan implements OwaspZapScan {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractScan.class);

    private static final int CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS = 3000;

    protected ClientApi clientApi;
    protected OwaspZapScanConfiguration scanConfig;

    protected String contextId;

    private ScanDurationHelper scanDurationHelper;
    private long remainingScanTime;

    private SecHubIncludeExcludeToOwaspZapURIHelper includeExcludeConverter;

    public AbstractScan(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) {
        this.clientApi = clientApi;
        this.scanConfig = scanConfig;
        this.scanDurationHelper = new ScanDurationHelper();
        this.remainingScanTime = scanConfig.getMaxScanDurationInMillis();
        this.includeExcludeConverter = new SecHubIncludeExcludeToOwaspZapURIHelper();
    }

    @Override
    public void scan() {
        try {
            createContext();
            addIncludedAndExcludedUrlsToContext();
            if (scanConfig.isAjaxSpiderEnabled()) {
                runAjaxSpider();
            }

            runSpider();

            passiveScan();

            if (scanConfig.isActiveScanEnabled()) {
                runActiveScan();
            }
            generateOwaspZapReport();
        } catch (ClientApiException e) {
            LOG.error("For scan {}: An error occured while scanning! Reason: {}", scanConfig.getContextName(), e.getMessage(), e);
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
        this.contextId = OwaspZapApiResponseHelper.getIdOfApiRepsonse(createContextRepsonse);
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
            LOG.info("For scan {}: Passive scan number of records left for scanning: " + numberOfRecords, scanConfig.getContextName());
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
     */
    protected void generateOwaspZapReport() {
        LOG.info("For scan {}: Writing results to report...", scanConfig.getContextName());
        Path reportFile = scanConfig.getReportFile();

        try {
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

            // clean up after scan only if report was done correctly
            // to investigate errors we keep the session otherwise, because a new scan
            // always starts a new session, if it worked here or not does not matter
            cleanUp();
        } catch (ClientApiException e) {
            LOG.error("For scan {}: Error writing report file", scanConfig.getContextName(), e);
        }
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
            throw new IllegalArgumentException("Report file not set");
        }
        if (Files.isDirectory(reportFile)) {
            throw new IllegalArgumentException("Report file may not be a directory!");
        }

        Path parent = reportFile.getParent();
        Path absolutePath = parent.toAbsolutePath();

        return absolutePath.toString();
    }

    private void renameReportFileIfFileExtensionIsNotJSON() {
        String reportFile = scanConfig.getReportFile().toAbsolutePath().toFile().getAbsolutePath();
        if (!reportFile.endsWith(".json")) {
            try {
                Path owaspzapReport = Paths.get(reportFile + ".json");
                Files.move(owaspzapReport, owaspzapReport.resolveSibling(scanConfig.getReportFile().toAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("For scan {}: Could not rename report file.", scanConfig.getContextName(), e);
            }
        }
    }

    private void cleanUp() throws ClientApiException {
        // to ensure parts from previous scan are deleted
        LOG.info("Cleaning up by starting new and empty session...", scanConfig.getContextName());
        clientApi.core.newSession("Cleaned after scan", "true");
        LOG.info("Cleanup successful.");
    }

    private void registerUrlsIncludedInContext() throws ClientApiException {
        clientApi.context.includeInContext(scanConfig.getContextName(), scanConfig.getTargetUrlAsString() + ".*");

        if (scanConfig.getSecHubWebScanConfiguration().getIncludes().isPresent()) {
            List<String> includedUrls = includeExcludeConverter.createListOfUrls(scanConfig.getTargetUrlAsString(),
                    scanConfig.getSecHubWebScanConfiguration().getIncludes().get());

            for (String url : includedUrls) {
                clientApi.context.includeInContext(scanConfig.getContextName(), url);
            }
        }
    }

    private void registerUrlsExcludedFromContext() throws ClientApiException {
        if (scanConfig.getSecHubWebScanConfiguration().getExcludes().isPresent()) {
            List<String> excludedUrls = includeExcludeConverter.createListOfUrls(scanConfig.getTargetUrlAsString(),
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
     * Runs webdriver oriented spider (suitable for single page applications)- just
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
