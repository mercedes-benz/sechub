// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

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
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxContext;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxMetaDataID;

public class CheckmarxCancelSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxCancelSupport.class);

    /**
     * Cancel a scan in the Checkmarx queue: @see <a href=
     * "https://checkmarx.stoplight.io/docs/checkmarx-sast-api-reference-guide/9ad912c03f478-update-status-of-a-scan-in-queue">Checkmarx
     * API reference</a>.
     *
     * @param oauthSupport - to ensure the authentication is set up correctly
     * @param context      - to provide necessary data like Checkmarx scan ID
     * @throws AdapterException - an unexpected response from Checkmarx occurs
     */
    public void cancelScanInQueue(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        String checkmarxScanId = metaData.getValueAsStringOrNull(CheckmarxMetaDataID.KEY_SCAN_ID);
        if (checkmarxScanId == null) {
            LOG.info("No Checkmarx scan ID in meta data found, means no cancel operation necessary.");
            return;
        }

        oauthSupport.refreshBearerTokenWhenNecessary(context);

        LOG.info("Trigger cancel scan in checkmarx queue.");
        Map<String, Object> json = new TreeMap<>();
        json.put("status", "canceled");

        String url = context.getAPIURL("sast/scansQueue/" + checkmarxScanId);
        String jsonAsString = context.json().toJSON(json);

        HttpHeaders headers = new HttpHeaders();
        headers.set(ACCEPT, "application/json;v=1.2");
        headers.set(CONTENT_TYPE, "application/json;v=1.2");
        HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

        RestOperations restTemplate = context.getRestOperations();
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);

        if (!HttpStatus.OK.equals(result.getStatusCode())) {
            throw new CheckmarxCancelException(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "Was not able to cancel scan in Checkmarx queue.");
        }
    }

}
