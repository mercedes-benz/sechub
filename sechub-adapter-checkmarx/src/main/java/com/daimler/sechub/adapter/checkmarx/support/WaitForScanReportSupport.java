// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.Adapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.WaitForStateSupport;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport.Access;

class WaitForScanReportSupport extends WaitForStateSupport<CheckmarxAdapterContext, CheckmarxAdapterConfig> {

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
        try {
            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> queueData = restTemplate.getForEntity(context.getAPIURL("reports/sastScan/" + context.getReportId() + "/status"),
                    String.class);
            String body = queueData.getBody();

            Access status = context.json().fetch("status", body);
            String value = status.fetch("value").asText();
            details.status = value;

        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                /* ok just no longer in queue / or never existed */
                details.notFound = true;
                return;
            }
            throw e; // rethrow
        }

    }

}