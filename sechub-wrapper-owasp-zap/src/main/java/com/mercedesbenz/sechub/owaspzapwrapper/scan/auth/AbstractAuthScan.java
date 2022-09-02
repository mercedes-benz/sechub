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

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.AbstractScan;

public abstract class AbstractAuthScan extends AbstractScan implements AuthScan {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthScan.class);

    protected String userId;
    protected String username;

    public AbstractAuthScan(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) {
        super(clientApi, scanConfig);
    }

    @Override
    public void scan() {
        try {
            scanUnsafe();
        } catch (ClientApiException e) {
            LOG.error("For scan {}: An error occured while scanning! Reason: {}", scanConfig.getContextName(), e.getMessage(), e);
            throw new ZapWrapperRuntimeException("An error occurred during the scan execution", e, ZapWrapperExitCode.EXECUTION_FAILED);
        }
    }

    @Override
    protected void runSpider() throws ClientApiException {
        String url = scanConfig.getTargetUriAsString();
        String maxchildren = null;
        String recurse = "true";
        String subtreeonly = "true";
        LOG.info("For scan {}: Starting authenticated Spider.", scanConfig.getContextName());
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
        String contextname = scanConfig.getContextName();
        String url = scanConfig.getTargetUriAsString();
        String subtreeonly = "true";
        LOG.info("For scan {}: Starting authenticated Ajax Spider.", scanConfig.getContextName());
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
        String url = scanConfig.getTargetUriAsString();
        String recurse = "true";
        String scanpolicyname = null;
        String method = null;
        String postdata = null;
        LOG.info("For scan {}: Starting authenticated ActiveScan.", scanConfig.getContextName());
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

}
