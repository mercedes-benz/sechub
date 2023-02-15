// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.AbstractScan;

public abstract class AbstractAuthScan extends AbstractScan implements AuthScan {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthScan.class);

    protected String userId;
    protected String username;

    public AbstractAuthScan(ClientApi clientApi, OwaspZapScanContext scanContext) {
        super(clientApi, scanContext);
    }

    @Override
    public void scan() {
        try {
            scanUnsafe();
        } catch (ClientApiException e) {
            LOG.error("For scan {}: An error occured while scanning! Reason: {}", scanContext.getContextName(), e.getMessage(), e);
            throw new ZapWrapperRuntimeException("An error occurred during the scan execution", e, ZapWrapperExitCode.PRODUCT_EXECUTION_ERROR);
        }
    }

    @Override
    protected void runSpider() throws ClientApiException {
        String url = scanContext.getTargetUrlAsString();
        String maxchildren = null;
        String recurse = "true";
        String subtreeonly = "true";
        LOG.info("For scan {}: Starting authenticated Spider.", scanContext.getContextName());
        /* @formatter:off */
		ApiResponse responseSpider = clientApi.spider.scanAsUser(
		        contextId,
		        userId,
		        url,
				maxchildren,
				recurse,
				subtreeonly);
		/* @formatter:on */
        waitForSpiderResults(responseSpider);
    }

    @Override
    protected void runAjaxSpider() throws ClientApiException {
        String contextname = scanContext.getContextName();
        String url = scanContext.getTargetUrlAsString();
        String subtreeonly = "true";
        LOG.info("For scan {}: Starting authenticated Ajax Spider.", scanContext.getContextName());
        /* @formatter:off */
		ApiResponse responseAjaxSpider = clientApi.ajaxSpider.scanAsUser(
		        contextname,
		        username,
				url,
				subtreeonly);
		/* @formatter:on */

        waitForAjaxSpiderResults(responseAjaxSpider);
    }

    @Override
    protected void runActiveScan() throws ClientApiException {
        // Necessary otherwise the active scanner exits with an exception,
        // if no URLs to scan where detected by the spider/ajaxSpider before
        if (!atLeastOneURLDetected()) {
            LOG.warn("For {} skipping active scan, since no URLs where detected by spider or ajaxSpider!", scanContext.getContextName());
            scanContext.getOwaspZapProductMessagehelper().writeSingleProductMessage(new SecHubMessage(SecHubMessageType.WARNING,
                    "Active scan part of the webscan was skipped, because no URLs where detected by crawling mechanisms!"));
            return;
        }
        String url = scanContext.getTargetUrlAsString();
        String recurse = "true";
        String scanpolicyname = null;
        String method = null;
        String postdata = null;
        LOG.info("For scan {}: Starting authenticated ActiveScan.", scanContext.getContextName());
        /* @formatter:off */
		ApiResponse responseActive = clientApi.ascan.scanAsUser(
		        url,
		        contextId,
		        userId,
		        recurse,
		        scanpolicyname,
		        method,
		        postdata);
		/* @formatter:on */
        waitForActiveScanResults(responseActive);
    }

    protected String urlEncodeUTF8(String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("This should not happen because we always use UTF-8: " + e);
        }
    }

    private void scanUnsafe() throws ClientApiException {
        setupBasicConfiguration();
        deactivateRules();
        setupAdditonalProxyConfiguration();

        createContext();
        addIncludedAndExcludedUrlsToContext();
        init();
        loadApiDefinitions();
        if (scanContext.isAjaxSpiderEnabled()) {
            runAjaxSpider();
        }

        runSpider();

        passiveScan();

        if (scanContext.isActiveScanEnabled()) {
            runActiveScan();
        }
        generateOwaspZapReport();

        cleanUp();
    }

}
