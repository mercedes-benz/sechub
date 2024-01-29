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
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterContext;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport.Access;

class WaitForScanReportSupport extends WaitForStateSupport<CheckmarxAdapterContext, CheckmarxAdapterConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForScanReportSupport.class);

    private CheckmarxOAuthSupport oauthSupport;

    public WaitForScanReportSupport(CheckmarxOAuthSupport oauthSupport, Adapter<?> adapter) {
        super(adapter);
        this.oauthSupport = oauthSupport;
    }

    @Override
    protected boolean isWaitingForOKWhenInState(String state, CheckmarxAdapterContext context) throws Exception {
        return context.getReportDetails().isRunning();
    }

    @Override
    protected String getCurrentState(CheckmarxAdapterContext context) throws Exception {
        fetchScanDetails(context);
        return null;
    }

//	https://checkmarx.atlassian.net/wiki/spaces/KC/pages/563806382/Get+Report+Status+by+Id+-+GET+reports+sastScan+id+status+v8.8.0+and+up
//	https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814121878/Swagger+Examples+v8.8.0+-+v1
    private void fetchScanDetails(CheckmarxAdapterContext context) throws AdapterException {

        oauthSupport.refreshBearerTokenWhenNecessary(context);

        ReportDetails details = context.getReportDetails();
        long reportId = context.getReportId();

        try {
            LOG.debug("Fetching scan report status for Checkmarx report Id: {}.", reportId);

            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> queueData = restTemplate.getForEntity(context.getAPIURL("reports/sastScan/" + reportId + "/status"), String.class);
            String body = queueData.getBody();

            Access status = context.json().fetch("status", body);
            String value = status.fetch("value").asText();
            details.status = value;

            LOG.debug("Report status: {}. Checkmarx report Id: {}.", details.status, reportId);

            if (details.status.equalsIgnoreCase("failed")) {
                LOG.warn(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "Scan report status is: failed. Checkmarx report Id: {}.", reportId);
            }

        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                /* ok just no longer in queue / or never existed */
                details.notFound = true;
                LOG.info(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX
                        + "Unable to find Checkmarx report Id: {}. Possible reasons: no longer in queue or never existed.", reportId);

                return;
            }
            throw e; // rethrow
        }

    }

}