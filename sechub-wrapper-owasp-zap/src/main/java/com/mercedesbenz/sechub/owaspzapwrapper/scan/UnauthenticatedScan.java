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
        String subTreeOnly = "true";
        String recurse = "true";
        String maxChildren = null;
        String targetUrlAsString = scanConfig.getTargetUriAsString();
        LOG.info("For scan {}: Starting Spider.", contextName);
        /* @formatter:off */
		ApiResponse responseSpider = clientApi.spider.scan(
				targetUrlAsString,
				maxChildren,
				recurse,
				contextName,
				subTreeOnly);
		/* @formatter:on */
        waitForSpiderResults(responseSpider);
    }

    @Override
    protected void runAjaxSpider() throws ClientApiException {
        String inScope = "true";
        String subTreeOnly = "true";
        String contextName = scanConfig.getContextName();
        String targetUrlAsString = scanConfig.getTargetUriAsString();
        LOG.info("For scan {}: Starting AjaxSpider.", scanConfig.getContextName());
        /* @formatter:off */
		ApiResponse responseAjaxSpider = clientApi.ajaxSpider.scan(
				targetUrlAsString,
				inScope,
				contextName,
				subTreeOnly);
		/* @formatter:on */
        waitForAjaxSpiderResults(responseAjaxSpider);
    }

    @Override
    protected void runActiveScan() throws ClientApiException {
        String targetUrlAsString = scanConfig.getTargetUriAsString();
        String inScopeOnly = "true";
        String recurse = "true";
        String scanPolicyName = null;
        String method = null;
        String postData = null;
        LOG.info("For scan {}: Starting ActiveScan.", scanConfig.getContextName());
        /* @formatter:off */
		ApiResponse responseActive = clientApi.ascan.scan(
				targetUrlAsString,
				recurse,
				inScopeOnly,
				scanPolicyName,
				method,
				postData);
		/* @formatter:on */
        waitForActiveScanResults(responseActive);
    }
}