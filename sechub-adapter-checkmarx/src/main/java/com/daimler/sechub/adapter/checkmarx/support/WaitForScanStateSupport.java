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

class WaitForScanStateSupport extends WaitForStateSupport<CheckmarxContext, CheckmarxAdapterConfig>{

	private CheckmarxOAuthSupport oauthSupport;

    public WaitForScanStateSupport(CheckmarxOAuthSupport oauthSupport, Adapter<?> adapter) {
		super(adapter);
		this.oauthSupport=oauthSupport;
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
		try {
			RestOperations restTemplate = context.getRestOperations();
			ResponseEntity<String> queueData = restTemplate.getForEntity(
					context.getAPIURL("sast/scans/" + context.getSessionData().getScanId()), String.class);
			String body = queueData.getBody();

			Access status = context.json().fetch("status", body);
			String statusName = status.fetch("name").asText();
			details.statusName =statusName;

		}catch(HttpStatusCodeException e) {
			if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
				/* ok just no longer in queue / or never existed */
				details.notFound=true;
				return;
			}
			throw e; // rethrow
		}

	}
	
}