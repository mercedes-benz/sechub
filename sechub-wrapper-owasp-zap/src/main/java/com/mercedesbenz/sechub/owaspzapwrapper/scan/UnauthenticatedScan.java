// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;

public class UnauthenticatedScan extends AbstractScan {

    private static final Logger LOG = LoggerFactory.getLogger(UnauthenticatedScan.class);

    public UnauthenticatedScan(ClientApi clientApi, OwaspZapScanConfiguration scanConfig) {
        super(clientApi, scanConfig);
    }

    @Override
    protected void runSpider() throws ClientApiException {
        String contextName = scanConfig.getContextName();
        String subtreeonly = "true";
        String recurse = "true";
        String maxChildren = null;
        String targetUrlAsString = scanConfig.getTargetUrlAsString();
        LOG.info("For scan {}: Starting Spider.", contextName);
        /* @formatter:off */
		ApiResponse responseSpider = clientApi.spider.scan(
				targetUrlAsString,
				maxChildren ,
				recurse,
				contextName,
				subtreeonly);
		/* @formatter:on */
        waitForSpiderResults(responseSpider);
    }

    @Override
    protected void runAjaxSpider() throws ClientApiException {
        String inscope = "true";
        String subtreeonly = "true";
        String contextname = scanConfig.getContextName();
        String url = scanConfig.getTargetUrlAsString();
        LOG.info("For scan {}: Starting AjaxSpider.", scanConfig.getContextName());
        /* @formatter:off */
		ApiResponse responseAjaxSpider = clientApi.ajaxSpider.scan(
				url,
				inscope,
				contextname,
				subtreeonly);
		/* @formatter:on */
        waitForAjaxSpiderResults(responseAjaxSpider);
    }

    @Override
    protected void runActiveScan() throws ClientApiException {
        String url = scanConfig.getTargetUrlAsString();
        String inscopeonly = "true";
        String recurse = "true";
        String scanpolicyname = null;
        String method = null;
        String postdata = null;
        LOG.info("For scan {}: Starting ActiveScan.", scanConfig.getContextName());
        /* @formatter:off */
		ApiResponse responseActive = clientApi.ascan.scan(
				url,
				recurse,
				inscopeonly,
				scanpolicyname,
				method,
				postdata);
		/* @formatter:on */
        waitForActiveScanResults(responseActive);
    }
}