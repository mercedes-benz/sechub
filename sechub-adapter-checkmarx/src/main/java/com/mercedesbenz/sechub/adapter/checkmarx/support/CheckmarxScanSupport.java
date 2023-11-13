// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxContext;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxMetaDataID;

public class CheckmarxScanSupport {

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/563806540/Create+New+Scan+POST+sast+scans+v8.8.0+and+up
    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/563806540/Create+New+Scan+POST+sast+scans+v8.8.0+and+up
    // next: important: v1 -because only there are SAST scans!!!!
    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814121878/Swagger+Examples+v8.8.0+-+v1

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxScanSupport.class);

    /**
     * Starts new scan - means : Will create an entry inside QUEUE! And wait until
     * processed. If checkmarx queuing fails because of full scan is necessary a
     * automatic retry will be done. If another failure occurs the scan will fail.
     *
     * @param oauthSupport
     *
     * @param context        - if scan is started, the corresponding queue id will
     *                       be set to context
     * @param sessionContext
     * @throws AdapterException
     */
    public void startNewScan(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        LOG.info("Start new checkmarx scan for: {}", context.getSessionData().getProjectName());

        triggerNewEntryInQueue(oauthSupport, context);
        waitForQueingDone(oauthSupport, context);
        checkScanAvailable(oauthSupport, context);

    }

    private void checkScanAvailable(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        WaitForScanStateSupport support = new WaitForScanStateSupport(oauthSupport, context.getCheckmarxAdapter());
        support.waitForOK(context);

    }

    private void waitForQueingDone(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        WaitForQueueStateSupport queueSupport = new WaitForQueueStateSupport(oauthSupport, context.getCheckmarxAdapter());
        queueSupport.waitForOK(context);

        QueueDetails queueDetails = context.getQueueDetails();
        if (queueDetails.hasNeverRun()) {
            throw context.asAdapterException(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "Queuing has never been run", null);
        }

        if (queueDetails.hasFailed()) {
            String failureText = queueDetails.getFailureText();
            if (failureText == null) {
                failureText = "";
            }
            if (context.isIncrementalScan() && failureText.toLowerCase().contains("full scan")) {
                throw new CheckmarxFullScanNecessaryException(failureText);
            }
            if (failureText.contains("unsupported language or file format")) {
                throw new CheckmarxOnlyUnsupportedFilesException(failureText);
            }

            throw context.asAdapterException(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "Queuing has failed. Details: " + failureText, null);
        }
    }

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814121878/Swagger+Examples+v8.8.0+-+v1
    private void triggerNewEntryInQueue(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        oauthSupport.refreshBearerTokenWhenNecessary(context);

        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        Long scanIdLong = metaData.getValueAsLongOrNull(CheckmarxMetaDataID.KEY_SCAN_ID);
        long scanId = -1;
        if (scanIdLong == null) {
            LOG.info("Trigger new scan entry in checkmarx queue");
            CheckmarxAdapterConfig config = context.getConfig();
            long projectId = context.getSessionData().getProjectId();

            Map<String, Object> json = new TreeMap<>();
            json.put("projectId", projectId);
            json.put("isIncremental", context.isIncrementalScan());
            json.put("isPublic", false);
            json.put("forceScan", false);
            json.put("comment", "sechub job:" + config.getTraceID());

            String url = context.getAPIURL("sast/scans");
            String jsonAsString = context.json().toJSON(json);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json;v=1.0");
            HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            HttpStatus expectedHttpStatus = HttpStatus.CREATED;
            if (!result.getStatusCode().equals(expectedHttpStatus)) {
                throw context.asAdapterException(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "HTTP status=" + result.getStatusCode()
                        + " (but expected was HTTP status=" + expectedHttpStatus + ")", null);
            }
            String body = result.getBody();

            scanId = context.json().fetch("id", body).asLong();

            metaData.setValue(CheckmarxMetaDataID.KEY_SCAN_ID, scanId);
            context.getRuntimeContext().getCallback().persist(metaData);
        } else {
            /* just reuse existing data */
            scanId = scanIdLong.longValue();
            LOG.info("Reuse existing scanId:{}, for :{}", scanId, context.getTraceID());
        }

        context.getSessionData().setScanId(scanId);
    }

}
