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

class WaitForQueueStateSupport extends WaitForStateSupport<CheckmarxContext, CheckmarxAdapterConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForQueueStateSupport.class);

    private CheckmarxOAuthSupport oauthSupport;

    public WaitForQueueStateSupport(CheckmarxOAuthSupport oauthSupport, Adapter<?> adapter) {
        super(adapter);
        this.oauthSupport = oauthSupport;
    }

    @Override
    protected boolean isWaitingForOKWhenInState(String state, CheckmarxContext context) throws Exception {
        return context.getQueueDetails().isRunning();
    }

    @Override
    protected String getCurrentState(CheckmarxContext context) throws Exception {
        fetchScanQueueDetails(context);
        return null;
    }

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/334332174/Get+Scan+Queue+Details+by+Scan+Id+-+GET+sast+scansQueue+id+8.7.0+and+up
    private void fetchScanQueueDetails(CheckmarxContext context) throws AdapterException {
        oauthSupport.refreshBearerTokenWhenNecessary(context);

        QueueDetails details = context.getQueueDetails();
        long scanId = context.getSessionData().getScanId();

        try {
            LOG.debug("Fetching scan queue details for Checkmarx scan Id: {}.", scanId);

            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> queueData = restTemplate.getForEntity(context.getAPIURL("sast/scansQueue/" + scanId), String.class);
            String body = queueData.getBody();
            Access stage = context.json().fetch("stage", body);
            String value = stage.fetch("value").asText();

            details.stageValue = value;
            LOG.debug("Scan queue stage: {}. Checkmarx scan Id: {}.", details.stageValue, scanId);

            switch (details.stageValue) {
            case "New":
                if (!details.newQueueEntryFound) {
                    details.newQueueEntryFound = true;
                }
                break;
            case "Failed":
                if (!details.newQueueEntryFound) {
                    details.newQueueEntryFound = true;
                }
                details.failureText = context.json().fetch("stageDetails", body).asText();
                LOG.info(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "Scan queue stage failed. Details: {}", details.failureText);
                break;
            case "Finished":
                if (!details.newQueueEntryFound) {
                    details.newQueueEntryFound = true;
                }
                details.done = true;
                break;
            default:
                break;
            }
            details.checkCount++;
        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                /* ok just no longer in queue / or never existed */
                details.done = true;
                return;
            }
            throw e; // rethrow
        }

    }

}