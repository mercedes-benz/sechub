// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import static org.springframework.http.HttpStatus.*;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.mercedesbenz.sechub.adapter.AbstractAdapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterProfiles;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.WaitForStateSupport;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;

/**
 * This component is able to handle Netsparker API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class NetsparkerAdapterV1 extends AbstractAdapter<NetsparkerAdapterContext, NetsparkerAdapterConfig> implements NetsparkerAdapter {

    private static final String POLICY_ID = "PolicyId";
    private static final String TARGET_URI = "TargetUri";
    private static final String AGENT_NAME = "AgentName";
    private static final String AGENT_GROUP_NAME = "AgentGroupName";
    private static final String PROPERTY_SCAN_ID = "Id";
    private static final String EXCLUDE_AUTHENTICATION_PAGES = "ExcludeAuthenticationPages";
    private static final String APICALL_GET_WEBSITE = "websites/get?query=";
    private static final String APICALL_CREATE_NEW_WEBSITE = "websites/new";
    private static final String APICALL_CREATE_NEW_SCAN = "scans/new";
    private static final String APICALL_GET_SCAN_STATUS = "scans/status/";
    private static final String APICALL_GET_SCAN_REPORT = "scans/report/";
    private static final String MAX_SCAN_DURATION = "MaxScanDuration";

    private static final Logger LOG = LoggerFactory.getLogger(NetsparkerAdapterV1.class);

    private NetsparkerAdapterWebLoginSupportV1 webLoginSupport = new NetsparkerAdapterWebLoginSupportV1();

    @Override
    public AdapterExecutionResult execute(NetsparkerAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        try {
            NetsparkerContext context = new NetsparkerContext(config, this, runtimeContext);
            NetsparkerWaitForStateSupport waitSupport = new NetsparkerWaitForStateSupport();
            ensureNetsparkerWebsiteConfigurationExists(context);

            createNewScanAndFetchId(context);
            waitSupport.waitForOK(context);

            fetchReport(context);

            return new AdapterExecutionResult(context.getResult());
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

    String extractIDFromScanResult(String body, NetsparkerAdapterContext context) throws AdapterException {
        return context.json().fetchRootNode(body).fetchArrayElement(0).fetch(PROPERTY_SCAN_ID).asText();
    }

    void createWebsite(NetsparkerContext context) throws AdapterException {
        NetsparkerAdapterConfig config = context.getConfig();
        String jsonAsString = buildJsonForCreateWebsite(context.json(), config);

        MultiValueMap<String, String> headers = createHeader(config);
        HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

        String apiUrl = createAPIURL(APICALL_CREATE_NEW_WEBSITE, config);
        try {
            ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
            if (!CREATED.equals(response.getStatusCode())) {
                throw new NetsparkerRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
            }
        } catch (HttpClientErrorException e) {
            LOG.error(e.getResponseBodyAsString());
            throw e;
        }
    }

    void fetchReport(NetsparkerAdapterContext context) {
        String traceID = context.getConfig().getTraceID();
        LOG.debug("{} try to fetch report", context.getConfig().getTraceID());

        String apiUrl = createAPIURL(APICALL_GET_SCAN_REPORT + context.getProductContextId() + "?Type=Vulnerabilities&Format=Xml", context.getConfig());
        ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
        if (!OK.equals(response.getStatusCode())) {
            throw new NetsparkerRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
        }
        String body = response.getBody();
        context.setResult(body);
        LOG.debug("{} calling fetch report with '{}'", traceID, apiUrl);
    }

    String buildJsonForCreateWebsite(JSONAdapterSupport jsonAdapterSupport, NetsparkerAdapterConfig config) throws AdapterException {
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

    String buildJsonForCreateNewScan(JSONAdapterSupport jsonAdapterSupport, NetsparkerAdapterConfig config) throws AdapterException {
        Map<String, Object> map = new TreeMap<>();

        map.put(TARGET_URI, config.getTargetAsString());
        map.put(POLICY_ID, config.getPolicyId());

        if (config.hasAgentGroup()) {
            map.put(AGENT_GROUP_NAME, config.getAgentGroupName());
        } else {
            map.put(AGENT_NAME, config.getAgentName());
        }

        if (config.hasMaxScanDuration()) {
            long maxScanDurationInHours = config.getMaxScanDuration().getTimeInHours();

            // the minimum for the maxScanDuration in Netsparker is 1
            if (maxScanDurationInHours < 1) {
                maxScanDurationInHours = 1;
            }

            map.put(MAX_SCAN_DURATION, maxScanDurationInHours);
        }

        // This parameter is not configurable, because without it the
        // form authentication and script authentication does not work.
        // In addition, the basic authentication takes twice as much time.
        // This problem exists in Netsparker version: 1.9.0.x, 1.9.1.x and 1.9.2.x
        map.put(EXCLUDE_AUTHENTICATION_PAGES, "true");

        webLoginSupport.addAuthorizationInfo(config, map);

        String jsonAsString = jsonAdapterSupport.toJSON(map);
        return jsonAsString;
    }

    private void createNewScanAndFetchId(NetsparkerContext context) throws AdapterException {
        NetsparkerAdapterConfig config = context.getConfig();
        String traceID = config.getTraceID();
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        metaData.setValue(NetsparkerMetaDataID.KEY_TARGET_URI, "" + context.getConfig().getTargetURI());

        String jsonAsString = buildJsonForCreateNewScan(context.json(), config);

        LOG.debug("{} request body will contain json:'{}'", traceID, jsonAsString);
        HttpEntity<String> request = new HttpEntity<>(jsonAsString);

        String apiUrl = createAPIURL(APICALL_CREATE_NEW_SCAN, config);
        try {
            LOG.debug("{} calling api url '{}'", traceID, apiUrl);
            ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
            if (!CREATED.equals(response.getStatusCode())) {
                throw new NetsparkerRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
            }
            context.setProductContextId(extractIDFromScanResult(response.getBody(), context));
            LOG.debug("{} created new scan and got netsparker ID '{}'", traceID, context.getProductContextId());

        } catch (HttpClientErrorException e) {
            throw new NetsparkerRESTFailureException(converToHttpStatus(e.getStatusCode()), e.getResponseBodyAsString());
        }

    }

    private void ensureNetsparkerWebsiteConfigurationExists(NetsparkerContext context) throws AdapterException {
        if (existsWebsiteInNetsparker(context)) {
            return;
        }
        /* create the web site */
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
                LOG.debug("{} Website:{} exists already with name:{}", traceID, config.getTargetAsString(), websiteName);
                return true;
            }
        } catch (HttpClientErrorException e) {
            if (NOT_FOUND.equals(e.getStatusCode())) {
                LOG.debug("{} Website:{} does not exists with name:{}", traceID, config.getTargetAsString(), websiteName);
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

    private class NetsparkerWaitForStateSupport extends WaitForStateSupport<NetsparkerAdapterContext, NetsparkerAdapterConfig> {

        public NetsparkerWaitForStateSupport() {
            super(NetsparkerAdapterV1.this);
        }

        @Override
        protected boolean isWaitingForOKWhenInState(String state, NetsparkerAdapterContext context) {
            return !NetsparkerState.isWellknown(state);
        }

        @Override
        protected void handleNoLongerWaitingState(String state, NetsparkerAdapterContext context) throws Exception {
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
                throw new NetsparkerRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
            }
            String state = context.json().fetch("State", response).asText();
            LOG.debug("{} state is '{}'", traceID, state);
            return state;
        }
    }

    private HttpStatus converToHttpStatus(HttpStatusCode code) {
        return HttpStatus.valueOf(code.value());
    }
}
