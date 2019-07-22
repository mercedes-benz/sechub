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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxContext;

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
	 * @param context
	 *            - if scan is started, the corresponding queue id will be set to
	 *            context
	 * @param sessionContext
	 * @throws AdapterException
	 */
	public void startNewScan(CheckmarxContext context) throws AdapterException {
		LOG.info("Start new checkmarx scan for: {}", context.getSessionData().getProjectName());
		triggerNewEntryInQueue(context);
		waitForQueingDone(context);
		checkScanAvailable(context);

	}

	private void checkScanAvailable(CheckmarxContext context) throws AdapterException {
		WaitForScanStateSupport support = new WaitForScanStateSupport(context.getCheckmarxAdapter());
		support.waitForOK(context);

	}

	private void waitForQueingDone(CheckmarxContext context) throws AdapterException {
		WaitForQueueStateSupport support = new WaitForQueueStateSupport(context.getCheckmarxAdapter());
		support.waitForOK(context);

		QueueDetails queueDetails = context.getQueueDetails();
		if (queueDetails.hasNeverRun()) {
			throw context.asAdapterException("The queuing has never been run ?!!?", null);
		}

		if (queueDetails.hasFailed()) {
			String failureText = queueDetails.getFailureText();
			if (failureText == null) {
				failureText = "";
			}
			if (context.isIncrementalScan() && failureText.toLowerCase().contains("full scan")) {
				throw new CheckmarxFullScanNecessaryException(failureText);
			}
			throw context.asAdapterException("The queuing has failed:" + failureText, null);
		}
	}

	// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814121878/Swagger+Examples+v8.8.0+-+v1
	private void triggerNewEntryInQueue(CheckmarxContext context) throws AdapterException {
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
		if (!result.getStatusCode().equals(HttpStatus.CREATED)) {
			throw context.asAdapterException("Response HTTP status not as expected: " + result.getStatusCode(), null);
		}
		String body = result.getBody();

		long scanId = context.json().fetch("id", body).asLong();
		context.getSessionData().setScanId(scanId);
	}

}
