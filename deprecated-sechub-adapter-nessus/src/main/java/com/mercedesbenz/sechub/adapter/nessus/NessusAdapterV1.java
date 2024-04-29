// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import static org.springframework.http.HttpStatus.*;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.adapter.AbstractAdapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterLogId;
import com.mercedesbenz.sechub.adapter.AdapterProfiles;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.WaitForStateSupport;

/**
 * This component is able to handle NESSUS API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class NessusAdapterV1 extends AbstractAdapter<NessusAdapterContext, NessusAdapterConfig> implements NessusAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NessusAdapterV1.class);
    private static final String APICALL_LOGIN = "/session";
    private static final String APICALL_LOGOUT = APICALL_LOGIN;
    private static final String APICALL_GET_POLICIES = "/editor/policy/templates";
    private static final String APICALL_ADD_NEW_SCAN = "/scans";
    private static final String MSG_APICALL_GET_HISTORY_IDS = "/scans/{0}";
    private static final String MSG_APICALL_LAUNCH_SCAN = "/scans/{0}/launch";
    private static final String MSG_APICALL_EXPORT_SCAN = "/scans/{0}/export";
    private static final String MSG_APICALL_EXPORT_SCAN_STATUS = "/scans/{0}/export/{1}/status";
    private static final String MSG_APICALL_EXPORT_SCAN_DOWNLOAD = "/scans/{0}/export/{1}/download";

    @Override
    public AdapterExecutionResult execute(NessusAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        try {
            NessusContext context = new NessusContext(config, this, runtimeContext);
            NessusWaitForScanStateSupport waitForScanDoneSupport = new NessusWaitForScanStateSupport();
            WaitForExportStatusSupport waitForExportDoneSupport = new WaitForExportStatusSupport();

            loginAndFetchToken(context);
            updateContextWithNessusPolicyUUID(context);
            addNewScan(context);
            launchScan(context);

            waitForScanDoneSupport.waitForOK(context);

            startExport(context);
            waitForExportDoneSupport.waitForOK(context);

            logout(context);

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

    private void updateContextWithNessusPolicyUUID(NessusContext context) throws AdapterException {
        String nessusPolicyUID = resolvePolicyUID(context);
        if (nessusPolicyUID == null) {
            NessusAdapterConfig config = context.getConfig();
            throw asAdapterException("There exists no nessus policy UUID for title:" + config.getPolicyId(), config);
        }
        context.setNessusPolicyId(nessusPolicyUID);
    }

    private void startExport(NessusContext context) throws AdapterException {
        NessusAdapterConfig config = context.getConfig();
        AdapterLogId adapterLogId = getAdapterLogId(config);
        LOG.debug("{} started scan result export", adapterLogId);

        String apiUrl = createScanExportApiURL(context);
        String json = "{\n" + "	\"history_id\": " + context.getHistoryId() + ",\n" + "	\"format\":\"nessus\"\n" + "}\n" + "";

        MultiValueMap<String, String> headers = createHeader(config);

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
        if (!OK.equals(response.getStatusCode())) {
            throw new NessusRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
        }
        String fileId = context.json().fetch("file", response).asText();
        context.setExportFileId(fileId);
        LOG.debug("{} fetched export data, fileId={}", adapterLogId, fileId);

    }

    protected String ensureHistoryIdInContext(NessusAdapterContext context) throws AdapterException {
        String historyId = context.getHistoryId();
        if (historyId != null) {
            return historyId;
        }
        context.setHistoryId(resolveHistoryId(context));
        return context.getHistoryId();
    }

    private String resolveHistoryId(NessusAdapterContext context) throws AdapterException {
        String traceID = context.getConfig().getTraceID();
        LOG.debug("{} try to fetch history id", traceID);

        String apiUrl = createGetHistoryIdsApiURL(context);
        ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
        if (!OK.equals(response.getStatusCode())) {

            throw new NessusRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
        }
        String content = response.getBody();
        String historyId = resolveHistoryIdByUUID(content, context);
        LOG.debug("{} found history id {}", traceID, historyId);

        return historyId;
    }

    private HttpStatus converToHttpStatus(HttpStatusCode code) {
        return HttpStatus.valueOf(code.value());
    }

    private void launchScan(NessusAdapterContext context) throws AdapterException {
        NessusAdapterConfig config = context.getConfig();

        MultiValueMap<String, String> headers = createHeader(config);

        HttpEntity<String> request = new HttpEntity<>(headers);

        String apiUrl = createLaunchApiURL(context);

        try {
            ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
            /* resolve token from response */
            String body = response.getBody();
            LOG.debug("{} resulted response body was '{}'", getAdapterLogId(context), body);
            String scanUUID = extractScanUUID(context, body);
            context.setProductContextId(scanUUID);

        } catch (HttpClientErrorException e) {
            throw asAdapterException("Problems with url:" + apiUrl + ":" + e.getResponseBodyAsString(), context);
        }
    }

    String extractScanUUID(NessusAdapterContext context, String body) throws AdapterException {
        String scanUUID = context.json().fetch("scan_uuid", body).asText();
        LOG.debug("{} resulted scanId uuid '{}'", getAdapterLogId(context), scanUUID);
        return scanUUID;
    }

    String createGetHistoryInfoApiURL(NessusAdapterContext context) {
        return createGetHistoryIdsApiURL(context);// same url
    }

    String createGetHistoryIdsApiURL(NessusAdapterContext context) {
        String part = MessageFormat.format(MSG_APICALL_GET_HISTORY_IDS, Long.toString(context.getNessusScanId()));
        return createAPIURL(part, context);
    }

    String createLaunchApiURL(NessusAdapterContext context) {
        String part = MessageFormat.format(MSG_APICALL_LAUNCH_SCAN, Long.toString(context.getNessusScanId()));
        return createAPIURL(part, context);
    }

    String createScanExportApiURL(NessusAdapterContext context) {
        String part = MessageFormat.format(MSG_APICALL_EXPORT_SCAN, Long.toString(context.getNessusScanId()));
        return createAPIURL(part, context);
    }

    String createScanExportStatusApiURL(NessusAdapterContext context) {
        String part = MessageFormat.format(MSG_APICALL_EXPORT_SCAN_STATUS, Long.toString(context.getNessusScanId()), context.getExportFileId());
        return createAPIURL(part, context);
    }

    String createScanExportDownloadApiURL(NessusAdapterContext context) {
        String part = MessageFormat.format(MSG_APICALL_EXPORT_SCAN_DOWNLOAD, Long.toString(context.getNessusScanId()), context.getExportFileId());
        return createAPIURL(part, context);
    }

    void addNewScan(NessusAdapterContext context) throws AdapterException {
        NessusAdapterConfig config = context.getConfig();
        String jsonAsString = createNewScanJSON(context);

        MultiValueMap<String, String> headers = createHeader(config);

        HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

        String apiUrl = createAPIURL(APICALL_ADD_NEW_SCAN, config);

        try {
            ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
            /* resolve token from response */
            String body = response.getBody();
            LOG.debug("{} resulted response body was '{}'", config.getTraceID(), body);
            long scanId = context.json().fetch("scan", body).fetch("id").asLong();
            LOG.debug("{} resulted scanId is '{}'", config.getTraceID(), scanId);
            context.setNessusScanId(scanId);

        } catch (HttpClientErrorException e) {
            throw asAdapterException("Was not able to add new scan", e, config);
        }

    }

    String createNewScanJSON(NessusAdapterContext context) {
        NessusAdapterConfig config = context.getConfig();
        /* @formatter:off */
		return createNewScanJSONBuilder().
				uuid(context.getNessusPolicyUID()).
				name(config.getTraceID()+"_"+config.getTargetType()).
				description("SecHub scan "+config.getTraceID()+" for target type "+config.getTargetType()).
				targetsURIs(config.getTargetURIs()).
				targetIPs(config.getTargetIPs()).build();
		/* @formatter:on */

    }

    NessusAdapterV1NewScanJSONBuilder createNewScanJSONBuilder() {
        return new NessusAdapterV1NewScanJSONBuilder();
    }

    private String resolvePolicyUID(NessusAdapterContext context) throws AdapterException {
        String content = fetchPoliciesBody(context);
        String searchedPolicyTitle = context.getConfig().getPolicyId();
        return resolvePolicyUIDByTitle(content, searchedPolicyTitle, context);
    }

    String resolvePolicyUIDByTitle(String content, String searchedPolicyTitle, NessusAdapterContext context) throws AdapterException {
        try {

            ArrayNode templatesArray = context.json().fetch("templates", content).asArray();
            for (Iterator<JsonNode> elements = templatesArray.elements(); elements.hasNext();) {
                JsonNode node = elements.next();
                String title = context.json().fetch("title", node).asText();
                if (searchedPolicyTitle.equals(title)) {
                    return context.json().fetch("uuid", node).asText();
                }
            }
            return null;
        } catch (Exception e) {
            // JSON errors are marked as adapter exceptions and all others too...
            throw asAdapterException("Was not able to resolve policy uid", e, context);
        }
    }

    String resolveHistoryIdByUUID(String content, NessusAdapterContext context) throws AdapterException {
        String uuid = context.getProductContextId();
        if (uuid == null) {
            throw new IllegalStateException("No nessus uuid available!");
        }
        return context.json().fetch("history", content).fetchArrayElementHaving("history_id", Collections.singletonMap("uuid", uuid)).asText();
    }

    /**
     * Try to login with user credentials and fetch session token from NESSUS. The
     * token is stored in context.
     *
     * @param context
     * @throws AdapterException
     */
    void loginAndFetchToken(NessusAdapterContext context) throws AdapterException {
        NessusAdapterConfig config = context.getConfig();
        String traceID = config.getTraceID();

        LOG.debug("{} start login at {}", traceID, config.getProductBaseURL());

        MultiValueMap<String, String> headers = createHeader(config);

        Map<String, String> json = new TreeMap<>();
        json.put("username", config.getUser());
        json.put("password", config.getPasswordOrAPIToken());

        String jsonAsString = context.json().toJSON(json);

        HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);

        String apiUrl = createAPIURL(APICALL_LOGIN, config);
        try {
            ResponseEntity<String> response = context.getRestOperations().postForEntity(apiUrl, request, String.class);
            /* resolve token from response */
            String token = context.json().fetch("token", response).asText();
            LOG.debug("{} resulted token is '{}'", traceID, token);

            if (token == null || token.isEmpty()) {
                throw asAdapterException("Login failed, no token returned!", config);
            }
            context.setNessusSessionToken(token);

        } catch (HttpClientErrorException e) {
            throw asAdapterException("Was not able to login by url:" + apiUrl, e, context);
        }

    }

    String fetchPoliciesBody(NessusAdapterContext context) throws AdapterException {
        NessusAdapterConfig config = context.getConfig();
        String traceID = config.getTraceID();

        LOG.debug("{} start scanning for list of policies at {}", traceID, config.getProductBaseURL());

        String apiUrl = createAPIURL(APICALL_GET_POLICIES, config);
        try {
            RestOperations restTemplate = context.getRestOperations();
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            /* resolve token from response */
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw asAdapterException("Was not able to fetch policies body by url:" + apiUrl, e, context);
        }

    }

    void logout(NessusAdapterContext context) {
        String apiURL = createAPIURL(APICALL_LOGOUT, context.getConfig());
        context.getRestOperations().delete(apiURL);
        context.setNessusSessionToken(null);

    }

    private MultiValueMap<String, String> createHeader(NessusAdapterConfig config) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        return headers;
    }

    @Override
    protected String getAPIPrefix() {
        return null;
    }

    private class NessusWaitForScanStateSupport extends WaitForStateSupport<NessusAdapterContext, NessusAdapterConfig> {

        public NessusWaitForScanStateSupport() {
            super(NessusAdapterV1.this);
        }

        @Override
        protected boolean isWaitingForOKWhenInState(String state, NessusAdapterContext context) {
            return !NessusState.isWellknown(state);
        }

        @Override
        protected void handleNoLongerWaitingState(String state, NessusAdapterContext context) throws AdapterException {
            NessusAdapterConfig config = context.getConfig();
            if (NessusState.COMPLETE.isRepresentedBy(state)) {
                LOG.debug("{}  completed", getAdapterLogId(config));
                return;
            }
            if (NessusState.CANCELED.isRepresentedBy(state)) {
                LOG.debug("{} canceled", getAdapterLogId(config));
                throw asAdapterCanceledByUserException(config);
            }
            throw asAdapterException(state + " is wellknown but not handled by adapter!", config);

        }

        @Override
        protected String getCurrentState(NessusAdapterContext context) throws Exception {
            String historyID = ensureHistoryIdInContext(context);

            String body = "{\"history_id\":\"" + historyID + "\"}";
            AdapterLogId adapterLogId = getAdapterLogId(context.getConfig());
            LOG.debug("{} try to fetch history information for '{}'", adapterLogId, historyID);
            String apiUrl = createGetHistoryInfoApiURL(context);

            /* strange but necessary for NESSUS: a HTTP GET with a body... */
            MultiValueMap<String, String> headers = createHeader(context.getConfig());
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = context.getRestOperations().exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (!OK.equals(response.getStatusCode())) {
                throw new NessusRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
            }
            String status = context.json().fetch("info", response).fetch("status").asText();
            LOG.debug("{} found status {}", adapterLogId, status);
            return status;
        }
    }

    private class WaitForExportStatusSupport extends WaitForStateSupport<NessusAdapterContext, NessusAdapterConfig> {

        public WaitForExportStatusSupport() {
            super(NessusAdapterV1.this);
        }

        @Override
        protected boolean isWaitingForOKWhenInState(String state, NessusAdapterContext context) throws AdapterException {
            return !"ready".equalsIgnoreCase(state);
        }

        @Override
        protected void handleNoLongerWaitingState(String state, NessusAdapterContext context) throws Exception {
            String apiUrl = createScanExportDownloadApiURL(context);

            ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
            if (!OK.equals(response.getStatusCode())) {
                throw new NessusRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
            }

            String result = response.getBody();
            context.setResult(result);
            LOG.debug("{} fetched export status, result={}", getAdapterLogId(context), context.getResult());

        }

        @Override
        protected String getCurrentState(NessusAdapterContext context) throws Exception {
            String apiUrl = createScanExportStatusApiURL(context);

            ResponseEntity<String> response = context.getRestOperations().getForEntity(apiUrl, String.class);
            if (!OK.equals(response.getStatusCode())) {
                throw new NessusRESTFailureException(converToHttpStatus(response.getStatusCode()), response.getBody());
            }

            String state = context.json().fetch("status", response).asText();

            LOG.debug("{} fetched export status, fileId={}, state={}", getAdapterLogId(context), context.getExportFileId(), state);

            return state;
        }

    }

}
