// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterContext;
import com.daimler.sechub.adapter.checkmarx.CheckmarxMetaDataID;

public class CheckmarxScanReportSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxScanReportSupport.class);

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223379587/Register+Scan+Report+-+POST+reports+sastScan
    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/563806382/Get+Report+Status+by+Id+-+GET+reports+sastScan+id+status+v8.8.0+and+up
    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/222101925/Get+Report+s+by+Id+-+GET+reports+sastScan+id
    /**
     * Starts new scan - means : Will create an entry inside QUEUE! And wait until
     * processed
     *
     * @param oauthSupport
     *
     * @param context        - if scan is started, the corresponding queue id will
     *                       be set to context
     * @param sessionContext
     * @throws AdapterException
     */
    public void startFetchReport(CheckmarxOAuthSupport oauthSupport, CheckmarxAdapterContext context) throws AdapterException {
        triggerNewReport(oauthSupport, context);
        waitForReport(oauthSupport, context);
        fetchReportResult(oauthSupport, context);

    }

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/222101925/Get+Report+s+by+Id+-+GET+reports+sastScan+id
    void fetchReportResult(CheckmarxOAuthSupport oauthSupport, CheckmarxAdapterContext context) throws AdapterException {
        oauthSupport.refreshBearerTokenWhenNecessary(context);

        ReportDetails details = context.getReportDetails();
        try {
            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> queueData = restTemplate.getForEntity(context.getAPIURL("reports/sastScan/" + context.getReportId()), String.class);
            String body = queueData.getBody();
            if (body == null) { // NOSONAR
                body = "";
            }
            int index = body.indexOf("<?xml");
            if (index > 0) {
                body = body.substring(index);
            }
            context.setResult(body);

        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                /* ok just no longer in queue / or never existed */
                details.notFound = true;
                return;
            }
            throw e; // rethrow
        }
    }

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/563806382/Get+Report+Status+by+Id+-+GET+reports+sastScan+id+status+v8.8.0+and+up
    void waitForReport(CheckmarxOAuthSupport oauthSupport, CheckmarxAdapterContext context) throws AdapterException {

        WaitForScanReportSupport support = new WaitForScanReportSupport(oauthSupport, context.getCheckmarxAdapter());
        support.waitForOK(context);

        ReportDetails reportDetails = context.getReportDetails();
        if (reportDetails.isNotFound()) {
            throw context.asAdapterException("The report cannot be found!", null);
        }

    }

    // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223379587/Register+Scan+Report+-+POST+reports+sastScan
    void triggerNewReport(CheckmarxOAuthSupport oauthSupport, CheckmarxAdapterContext context) throws AdapterException {
        oauthSupport.refreshBearerTokenWhenNecessary(context);

        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        Long reportIdLong = metaData.getValueLong(CheckmarxMetaDataID.KEY_REPORT_ID);
        long reportId = -1;
        if (reportIdLong == null) {
            LOG.info("Trigger new report in queue");
            Map<String, Object> json = new TreeMap<>();
            json.put("reportType", "XML");
            json.put("scanId", context.getScanId());

            String url = context.getAPIURL("reports/sastScan");
            String jsonAsString = context.json().toJSON(json);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (!result.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                throw context.asAdapterException("Response HTTP status not as expected: " + result.getStatusCode(), null);
            }
            String body = result.getBody();

            reportId = context.json().fetch("reportId", body).asLong();
            metaData.setValue(CheckmarxMetaDataID.KEY_REPORT_ID, reportId);

            context.getRuntimeContext().getCallback().persist(metaData);

        } else {
            /* just reuse existing data */
            reportId = reportIdLong.longValue();
            LOG.info("Reuse existing reportId:{} for {}", reportId, context.getTraceID());
        }
        context.setReportId(reportId);
    }

}
