// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import com.mercedesbenz.sechub.adapter.Adapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.WaitForStateSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxContext;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport.Access;

class WaitForScanStateSupport extends WaitForStateSupport<CheckmarxContext, CheckmarxAdapterConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(WaitForScanStateSupport.class);

    private CheckmarxOAuthSupport oauthSupport;

    public WaitForScanStateSupport(CheckmarxOAuthSupport oauthSupport, Adapter<?> adapter) {
        super(adapter);
        this.oauthSupport = oauthSupport;
    }

    @Override
    protected boolean isWaitingForOKWhenInState(String state, CheckmarxContext context) throws Exception {
        return context.getScanDetails().isRunning();
    }

    @Override
    protected String getCurrentState(CheckmarxContext context) throws Exception {
        fetchScanDetails(context);
        return null;
    }

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/569442454/Get+SAST+Scan+Details+by+Scan+Id+-+GET+sast+scans+id+v8.8.0+and+up
    private void fetchScanDetails(CheckmarxContext context) throws AdapterException {
        oauthSupport.refreshBearerTokenWhenNecessary(context);

        ScanDetails details = context.getScanDetails();
        long scanId = context.getSessionData().getScanId();
        try {
            LOG.debug("Downloading Checkmarx report for scan Id: {}.", scanId);

            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> queueData = restTemplate.getForEntity(context.getAPIURL("sast/scans/" + scanId), String.class);
            String body = queueData.getBody();

            Access status = context.json().fetch("status", body);
            String statusName = status.fetch("name").asText();
            details.statusName = statusName;

            LOG.debug("Scan status name: {}. Checkmarx scan Id: {}.", details.statusName, scanId);

        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                /* ok just no longer in queue / or never existed */
                details.notFound = true;
                LOG.info(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX
                        + "Unable to find Checkmarx scan Id: {}. Possible reasons: no longer in queue or never existed.", scanId);

                return;
            }
            throw e; // rethrow
        }

    }

}