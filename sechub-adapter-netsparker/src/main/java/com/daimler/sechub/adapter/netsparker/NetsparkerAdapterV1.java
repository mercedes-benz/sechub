// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.springframework.http.HttpStatus.*;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.WaitForStateSupport;

/**
 * This component is able to handle Netsparker API V1
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class NetsparkerAdapterV1 extends AbstractAdapter<NetsparkerAdapterContext, NetsparkerAdapterConfig>
		implements NetsparkerAdapter {

	private static final String POLICY_ID = "PolicyId";
	private static final String TARGET_URI = "TargetUri";
	private static final String AGENT_NAME = "AgentName";
	private static final String AGENT_GROUP_NAME = "AgentGroupName";
	private static final String PROPERTY_SCAN_ID = "Id";
	private static final String APICALL_GET_WEBSITE = "websites/get?query=";
	private static final String APICALL_CREATE_NEW_WEBSITE = "websites/new";
	private static final String APICALL_CREATE_NEW_SCAN = "scans/new";
	private static final String APICALL_GET_SCAN_STATUS = "scans/status/";
	private static final String APICALL_GET_SCAN_REPORT = "scans/report/";

	private static final Logger LOG = LoggerFactory.getLogger(NetsparkerAdapterV1.class);

	@Override
	public String start(NetsparkerAdapterConfig config) throws AdapterException {
		try {
			NetsparkerContext context = new NetsparkerContext(config, this);
			NetsparkerWaitForStateSupport waitSupport = new NetsparkerWaitForStateSupport();
			ensureNetsparkerWebsiteConfigurationExists(context);

			createNewScanAndFetchId(context);
			waitSupport.waitForOK(context);

			fetchReport(context);

			return context.getResult();
		} catch (AdapterException e) {
			throw e;
		} catch (Exception e) {
			throw asAdapterException("Was not able to perform scan!", e, config);
		}

	}

	@Override
	protected String getAPIPrefix() {
		return "api/1.0";
	}

	String extractIDFromScanResult(String body, NetsparkerAdapterContext context)
			throws AdapterException {
		return context.json().fetchRootNode(body).fetchArrayElement(0).fetch(PROPERTY_SCAN_ID).asText();
	}

	void createWebsite(NetsparkerContext context) throws AdapterException {
		NetsparkerAdapterConfig config = context.getConfig();
		String targetURL = config.getTargetAsString();
		String name = config.getWebsiteName();
		String traceID = config.getTraceID();

		LOG.debug("{} try to create website with targetURL '{}' and name '{}'", traceID, targetURL, name);
		MultiValueMap<String, String> headers = createHeader(config);

		Map<String, String> json = new TreeMap<>();
		json.put("RootUrl", targetURL);
		json.put("Name", name);
		json.put("LicenseType", "Subscription");
		json.put("SubscriptionBasedProductLicenseId", config.getLicenseID());

		String jsonAsString = context.json().toJSON(json);

		HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

		String apiUrl = createAPIURL(APICALL_CREATE_NEW_WEBSITE, config);
		try {
			ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
			if (!CREATED.equals(response.getStatusCode())) {
				throw new NetsparkerRESTFailureException(response.getStatusCode(), response.getBody());
			}
		} catch (HttpClientErrorException e) {
			LOG.error(e.getResponseBodyAsString());
			throw e;
		}
	}

	void fetchReport(NetsparkerAdapterContext context) {
		String traceID = context.getConfig().getTraceID();
		LOG.debug("{} try to fetch report", context.getConfig().getTraceID());

		String apiUrl = createAPIURL(
				APICALL_GET_SCAN_REPORT + context.getProductContextId() + "?Type=Vulnerabilities&Format=Xml",
				context.getConfig());
		ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
		if (!OK.equals(response.getStatusCode())) {
			throw new NetsparkerRESTFailureException(response.getStatusCode(), response.getBody());
		}
		String body = response.getBody();
		context.setResult(body);
		LOG.debug("{} calling fetch report with '{}'", traceID, apiUrl);
	}

	private void createNewScanAndFetchId(NetsparkerContext context) throws AdapterException {
		NetsparkerAdapterConfig config = context.getConfig();
		String traceID = config.getTraceID();

		Map<String, String> json = new TreeMap<>();
		json.put(TARGET_URI, config.getTargetAsString());
		if (config.hasAgentGroup()) {
			json.put(AGENT_GROUP_NAME, config.getAgentGroupName());
		} else {
			json.put(AGENT_NAME, config.getAgentName());
		}
		json.put(POLICY_ID, config.getPolicyId());

		String jsonAsString = context.json().toJSON(json);

		LOG.debug("{} request body will contain json:'{}'", traceID, jsonAsString);
		HttpEntity<String> request = new HttpEntity<>(jsonAsString);

		String apiUrl = createAPIURL(APICALL_CREATE_NEW_SCAN, config);
		try {
			LOG.debug("{} calling api url '{}'", traceID, apiUrl);
			ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
			if (!CREATED.equals(response.getStatusCode())) {
				throw new NetsparkerRESTFailureException(response.getStatusCode(), response.getBody());
			}
			context.setProductContextId(extractIDFromScanResult(response.getBody(), context));
			LOG.debug("{} created new scan and got netsparker ID '{}'", traceID, context.getProductContextId());

		} catch (HttpClientErrorException e) {
			throw new NetsparkerRESTFailureException(e.getStatusCode(), e.getResponseBodyAsString());
		}

	}

	private void ensureNetsparkerWebsiteConfigurationExists(NetsparkerContext context) throws AdapterException {
		if (existsWebsiteInNetsparker(context)) {
			return;
		}
		/* create the website */
		createWebsite(context);

	}

	private boolean existsWebsiteInNetsparker(NetsparkerContext context) {
		NetsparkerAdapterConfig config = context.getConfig();
		String traceID = config.getTraceID();

		String websiteName = config.getWebsiteName();
		String apiUrl = createAPIURL(APICALL_GET_WEBSITE + websiteName, config);
		LOG.debug("{} check website existswith '{}'", traceID, apiUrl);
		try {
			ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
			if (OK.equals(response.getStatusCode())) {
				LOG.debug("{} Website:{} exists already with name:{}", traceID, config.getTargetAsString(),
						websiteName);
				return true;
			}
		} catch (HttpClientErrorException e) {
			if (NOT_FOUND.equals(e.getStatusCode())) {
				LOG.debug("{} Website:{} does not exists with name:{}", traceID, config.getTargetAsString(),
						websiteName);
				return false;
			}
			LOG.error(e.getResponseBodyAsString());
			throw e;
		}
		return false;
	}

	private MultiValueMap<String, String> createHeader(NetsparkerAdapterConfig config) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		return headers;
	}

	private class NetsparkerWaitForStateSupport
			extends WaitForStateSupport<NetsparkerAdapterContext, NetsparkerAdapterConfig> {

		public NetsparkerWaitForStateSupport() {
			super(NetsparkerAdapterV1.this);
		}

		@Override
		protected boolean isWaitingForOKWhenInState(String state, NetsparkerAdapterContext context) {
			return !NetsparkerState.isWellknown(state);
		}

		@Override
		protected void handleNoLongerWaitingState(String state, NetsparkerAdapterContext context)
				throws Exception {
			NetsparkerAdapterConfig config = context.getConfig();
			if (NetsparkerState.COMPLETE.isRepresentedBy(state)) {
				LOG.debug("{}  completed", getAdapterLogId(config));
				return;
			}
			if (NetsparkerState.CANCELED.isRepresentedBy(state)) {
				LOG.debug("{} canceled", getAdapterLogId(config));
				throw asAdapterCanceledByUserException(config);
			}
			if (NetsparkerState.FAILED.isRepresentedBy(state)) {
				LOG.debug("{} failed", getAdapterLogId(config));
				throw asAdapterException("Execution failed, see log files in netsparker for details", config);
			}
			throw asAdapterException(state + " is wellknown but not handled by adapter!", config);

		}

		protected String getCurrentState(NetsparkerAdapterContext context) throws AdapterException {
			String traceID = context.getConfig().getTraceID();
			String apiUrl = createAPIURL(APICALL_GET_SCAN_STATUS + context.getProductContextId(), context.getConfig());
			LOG.debug("{} calling get state with '{}'", traceID, apiUrl);

			ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
			if (!OK.equals(response.getStatusCode())) {
				throw new NetsparkerRESTFailureException(response.getStatusCode(), response.getBody());
			}
			String state = context.json().fetch("State", response).asText();
			LOG.debug("{} state is '{}'", traceID, state);
			return state;
		}
	}
}
