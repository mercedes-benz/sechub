// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;

public class UnauthenticatedScan extends AbstractScan {

    private static final Logger LOG = LoggerFactory.getLogger(UnauthenticatedScan.class);

    public UnauthenticatedScan(ClientApi clientApi, OwaspZapScanContext scanContext) {
        super(clientApi, scanContext);
    }

    @Override
    protected void runSpider() throws ClientApiException {
        String contextName = scanContext.getContextName();
        String subTreeOnly = "true";
        String recurse = "true";
        String maxChildren = null;
        String targetUrlAsString = scanContext.getTargetUrlAsString();
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
        String contextName = scanContext.getContextName();
        String targetUrlAsString = scanContext.getTargetUrlAsString();
        LOG.info("For scan {}: Starting AjaxSpider.", scanContext.getContextName());
        /* @formatter:off */
		clientApi.ajaxSpider.scan(
				targetUrlAsString,
				inScope,
				contextName,
				subTreeOnly);
		/* @formatter:on */
        waitForAjaxSpiderResults();
    }

    @Override
    protected void runActiveScan() throws ClientApiException {
        // Necessary otherwise the active scanner exits with an exception,
        // if no URLs to scan where detected by the spider/ajaxSpider before
        if (!atLeastOneURLDetected()) {
            LOG.warn("For {} skipping active scan, since no URLs where detected by spider or ajaxSpider!", scanContext.getContextName());
            scanContext.getOwaspZapProductMessageHelper().writeSingleProductMessage(
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