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
import com.daimler.sechub.adapter.checkmarx.CheckmarxContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport.Access;

class WaitForQueueStateSupport extends WaitForStateSupport<CheckmarxContext, CheckmarxAdapterConfig>{

	public WaitForQueueStateSupport(Adapter<?> adapter) {
		super(adapter);
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
		QueueDetails details = context.getQueueDetails();
		try {
			RestOperations restTemplate = context.getRestOperations();
			ResponseEntity<String> queueData = restTemplate.getForEntity(
					context.getAPIURL("sast/scansQueue/" + context.getSessionData().getScanId()), String.class);
			String body = queueData.getBody();
			Access stage = context.json().fetch("stage", body);
			String value = stage.fetch("value").asText();

			details.stageValue =value;

			switch (details.stageValue) {
			case "New":
				if (!details.newQueueEntryFound) {
					details.newQueueEntryFound = true;
				}
				break;
			case "Failed":
				details.failureText=context.json().fetch("stageDetails", body).asText();
				break;
			case "Finished":
				details.done=true;
				break;
			default:
				break;
			}
			details.checkCount++;
		}catch(HttpStatusCodeException e) {
			if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
				/* ok just no longer in queue / or never existed */
				details.done=true;
				return;
			}
			throw e; // rethrow
		}

	}
	
}