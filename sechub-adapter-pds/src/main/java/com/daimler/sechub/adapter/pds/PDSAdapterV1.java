// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

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
import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.AdapterRuntimeContext;
import com.daimler.sechub.adapter.WaitForStateSupport;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;

/**
 * This component is able to handle PDS API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class PDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig>
		implements PDSAdapter {
    /* FIXME Albert Tregnaghi, 2020-06-17:implement this correctl - currently more or less a copy from netsparker adapter */
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

	private static final Logger LOG = LoggerFactory.getLogger(PDSAdapterV1.class);


	@Override
	public String execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
		try {
			PDSContext context = new PDSContext(config, this,runtimeContext);
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
	public int getAdapterVersion() {
		return 1;
	}

	@Override
	protected String getAPIPrefix() {
		return "api/1.0";
	}

	String extractIDFromScanResult(String body, PDSAdapterContext context)
			throws AdapterException {
		return context.json().fetchRootNode(body).fetchArrayElement(0).fetch(PROPERTY_SCAN_ID).asText();
	}

	void createWebsite(PDSContext context) throws AdapterException {
		PDSAdapterConfig config = context.getConfig();
		String jsonAsString = buildJsonForCreateWebsite(context.json(), config);

		MultiValueMap<String, String> headers = createHeader(config);
		HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

		String apiUrl = createAPIURL(APICALL_CREATE_NEW_WEBSITE, config);
		try {
			ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
			if (!CREATED.equals(response.getStatusCode())) {
				throw new PSDRESTFailureException(response.getStatusCode(), response.getBody());
			}
		} catch (HttpClientErrorException e) {
			LOG.error(e.getResponseBodyAsString());
			throw e;
		}
	}

	void fetchReport(PDSAdapterContext context) {
		String traceID = context.getConfig().getTraceID();
		LOG.debug("{} try to fetch report", context.getConfig().getTraceID());

		String apiUrl = createAPIURL(
				APICALL_GET_SCAN_REPORT + context.getProductContextId() + "?Type=Vulnerabilities&Format=Xml",
				context.getConfig());
		ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
		if (!OK.equals(response.getStatusCode())) {
			throw new PSDRESTFailureException(response.getStatusCode(), response.getBody());
		}
		String body = response.getBody();
		context.setResult(body);
		LOG.debug("{} calling fetch report with '{}'", traceID, apiUrl);
	}

	String buildJsonForCreateWebsite(JSONAdapterSupport jsonAdapterSupport, PDSAdapterConfig config) throws AdapterException {
		String targetURL = config.getTargetAsString();
		String name = config.getWebsiteName();
		String traceID = config.getTraceID();

		LOG.debug("{} try to create website with targetURL '{}' and name '{}'", traceID, targetURL, name);

		Map<String, String> rootMap = new TreeMap<>();
		rootMap.put("RootUrl", targetURL);
		rootMap.put("Name", name);
		rootMap.put("LicenseType", "Subscription");
		rootMap.put("SubscriptionBasedProductLicenseId", config.getLicenseID());

		String jsonAsString = jsonAdapterSupport.toJSON(rootMap);
		return jsonAsString;
	}

	String buildJsonForCreateNewScan(JSONAdapterSupport jsonAdapterSupport, PDSAdapterConfig config) throws AdapterException {
		Map<String, Object> map = new TreeMap<>();
		map.put(TARGET_URI, config.getTargetAsString());
		if (config.hasAgentGroup()) {
			map.put(AGENT_GROUP_NAME, config.getAgentGroupName());
		} else {
			map.put(AGENT_NAME, config.getAgentName());
		}
		map.put(POLICY_ID, config.getPolicyId());

//		webLoginSupport.addAuthorizationInfo(config, map);

		String jsonAsString = jsonAdapterSupport.toJSON(map);
		return jsonAsString;
	}

	private void createNewScanAndFetchId(PDSContext context) throws AdapterException {
		PDSAdapterConfig config = context.getConfig();
		String traceID = config.getTraceID();
		AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        metaData.setValue(PDSMetaDataID.KEY_TARGET_URI, ""+context.getConfig().getTargetURI());

		String jsonAsString = buildJsonForCreateNewScan(context.json(), config);

		LOG.debug("{} request body will contain json:'{}'", traceID, jsonAsString);
		HttpEntity<String> request = new HttpEntity<>(jsonAsString);

		String apiUrl = createAPIURL(APICALL_CREATE_NEW_SCAN, config);
		try {
			LOG.debug("{} calling api url '{}'", traceID, apiUrl);
			ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
			if (!CREATED.equals(response.getStatusCode())) {
				throw new PSDRESTFailureException(response.getStatusCode(), response.getBody());
			}
			context.setProductContextId(extractIDFromScanResult(response.getBody(), context));
			LOG.debug("{} created new scan and got netsparker ID '{}'", traceID, context.getProductContextId());

		} catch (HttpClientErrorException e) {
			throw new PSDRESTFailureException(e.getStatusCode(), e.getResponseBodyAsString());
		}

	}

	private void ensureNetsparkerWebsiteConfigurationExists(PDSContext context) throws AdapterException {
		if (existsWebsiteInNetsparker(context)) {
			return;
		}
		/* create the web site */
		createWebsite(context);

	}

	private boolean existsWebsiteInNetsparker(PDSContext context) {
		PDSAdapterConfig config = context.getConfig();
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

	private MultiValueMap<String, String> createHeader(PDSAdapterConfig config) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		return headers;
	}

	private class NetsparkerWaitForStateSupport
			extends WaitForStateSupport<PDSAdapterContext, PDSAdapterConfig> {

		public NetsparkerWaitForStateSupport() {
			super(PDSAdapterV1.this);
		}

		@Override
		protected boolean isWaitingForOKWhenInState(String state, PDSAdapterContext context) {
			return !PDSState.isWellknown(state);
		}

		@Override
		protected void handleNoLongerWaitingState(String state, PDSAdapterContext context)
				throws Exception {
			PDSAdapterConfig config = context.getConfig();
			if (PDSState.COMPLETE.isRepresentedBy(state)) {
				LOG.debug("{}  completed", getAdapterLogId(config));
				return;
			}
			if (PDSState.CANCELED.isRepresentedBy(state)) {
				LOG.debug("{} canceled", getAdapterLogId(config));
				throw asAdapterCanceledByUserException(config);
			}
			if (PDSState.FAILED.isRepresentedBy(state)) {
				LOG.debug("{} failed", getAdapterLogId(config));
				throw asAdapterException("Execution failed, see log files in netsparker for details", config);
			}
			throw asAdapterException(state + " is wellknown but not handled by adapter!", config);

		}

		protected String getCurrentState(PDSAdapterContext context) throws AdapterException {
			String traceID = context.getConfig().getTraceID();
			String apiUrl = createAPIURL(APICALL_GET_SCAN_STATUS + context.getProductContextId(), context.getConfig());
			LOG.debug("{} calling get state with '{}'", traceID, apiUrl);

			ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
			if (!OK.equals(response.getStatusCode())) {
				throw new PSDRESTFailureException(response.getStatusCode(), response.getBody());
			}
			String state = context.json().fetch("State", response).asText();
			LOG.debug("{} state is '{}'", traceID, state);
			return state;
		}
	}
}
