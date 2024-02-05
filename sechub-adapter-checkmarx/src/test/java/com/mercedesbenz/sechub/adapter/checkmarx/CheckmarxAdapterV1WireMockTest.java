// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.IcrementalAdditionalPrefixAPIURLSupport;
import com.mercedesbenz.sechub.adapter.support.APIURLSupport;
import com.mercedesbenz.sechub.test.JSONTestUtil;
import com.mercedesbenz.sechub.test.TestPortProvider;
import com.mercedesbenz.sechub.test.WireMockUtil;
import com.mercedesbenz.sechub.test.WiremockUrlHistory;

public class CheckmarxAdapterV1WireMockTest {

    private static final String CONTENT_FROM_CHECKMARX = "content-from-checkmarx";
    private static final String SECHUB_TRACE_ID = "sechub-trace-id";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_FORM_URL_ENCODED_UTF_8 = "application/x-www-form-urlencoded;charset=UTF-8";

    private static final int HTTPS_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPSPort();

    private static final int HTTP_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPPort();
    private static final String PASSWORD = "12345BASE64_PWD";

    private static final String CHECKMARX_BASE_URL = "http://localhost:" + HTTP_PORT;

    private static final String POLICY_ID = "12345POLICY_ID";

    private static final String USERNAME = "sechub-user";
    private static final String PROJECT_NAME = "testproject";

    private static final long CHECKMARX_PROJECT_ID = 10666;
    private static final long CHECKMARX_CONFIGURATION_ID = 20666;
    private static final String CHECKMARX_ENGINE_CONFIGURATION_NAME = "Multi-language Scan";
    private static final long CHECKMARX_SECHUB_DEFAULT_PRESET_ID = 4711;
    private static final long CHECKMARX_SCAN_ID = 28331;
    private static final long CHECKMARX_REPORT_ID = 12345;

    private AdapterMetaDataCallback callback;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(HTTP_PORT).httpsPort(HTTPS_PORT));

    private CheckmarxAdapterV1 adapterToTest;

    private CheckmarxAdapterConfig config;
    private IcrementalAdditionalPrefixAPIURLSupport apiURLSupport;
    private WiremockUrlHistory history;

    @Before
    public void before() {

        apiURLSupport = new IcrementalAdditionalPrefixAPIURLSupport("checkmarxtest");
        history = new WiremockUrlHistory();

        adapterToTest = new CheckmarxAdapterV1() {
            @Override
            protected APIURLSupport createAPIURLSupport() {
                return apiURLSupport;
            }
        };

        callback = mock(AdapterMetaDataCallback.class);
        config = mock(CheckmarxAdapterConfig.class);

        when(config.getTraceID()).thenReturn(SECHUB_TRACE_ID);
        when(config.getPresetIdForNewProjectsOrNull()).thenReturn(CHECKMARX_SECHUB_DEFAULT_PRESET_ID);
        when(config.getClientSecret()).thenReturn(CheckmarxConfig.DEFAULT_CLIENT_SECRET);
        when(config.getEngineConfigurationName()).thenReturn(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME);
        when(config.getUser()).thenReturn(USERNAME);
        when(config.getPasswordOrAPIToken()).thenReturn(PASSWORD);
        when(config.getTimeOutInMilliseconds()).thenReturn(1000 * 5);
        when(config.getProjectId()).thenReturn(PROJECT_NAME);

        when(config.getProductBaseURL()).thenReturn(CHECKMARX_BASE_URL);
        when(config.getPolicyId()).thenReturn(POLICY_ID);
        when(config.getSourceCodeZipFileInputStream()).thenReturn(new ByteArrayInputStream("pseudo-zip-content".getBytes()));
    }

    @Test
    public void when_checkmarx_has_only_unsupported_files_the_result_is_canceled() throws Exception {
        when(config.getTimeOutInMilliseconds()).thenReturn(1000 * 1000* 5);

        /* prepare */
        LinkedHashMap<String, String> loginResponse = login(3600);
        /* project creation */
        simulateCheckProjectExistsReturnsFalse(loginResponse);
        simulateCreateProjectWasSuccessful();

        /* update scan settings */
        LinkedHashMap<String, Object> fetchScanSettingsResultMap = simulateFetchScanSettingsForProject();
        List<Map<String, Object>> fetchEngineConfigurationsMap = simulateFetchEngineConfigurations();
        Map<String, Object> multiLanguageEngineConfiguration = findMapByValue(CHECKMARX_ENGINE_CONFIGURATION_NAME, fetchEngineConfigurationsMap);
        long serverReturnedEngineConfigurationId = Long.valueOf(multiLanguageEngineConfiguration.get("id").toString());

        simulateUpdateScanSettingsForProjectWereSuccessful(fetchScanSettingsResultMap, serverReturnedEngineConfigurationId);

        /* upload */
        simulateUploadZipFileWasSuccesful();

        /* scan start */
        simulateStartScanAccepted();

        simulateWaitForQueingDoneReturns("New");
        simulateWaitForQueingDoneReturnsFailureWithText("source folder is empty, all source files are of an unsupported language or file format");

        /* execute */
        AdapterExecutionResult adapterResult = executeAndLogHistoryOnFailure(()->adapterToTest.start(config, callback));

        /* @formatter:on */
        /* test */
        assertEquals("", adapterResult.getProductResult()); // empty result because canceled
        assertEquals(true,adapterResult.hasBeenCanceled());
        history.assertAllRememberedUrlsWereRequested();
    }

    @Test
    public void simulate_start__create_new_project_and_scan_token_expires_very_often() throws Exception {
        /* prepare */

        LinkedHashMap<String, String> loginResponse = login(0);
        /* project creation */
        simulateCheckProjectExistsReturnsFalse(loginResponse);
        simulateCreateProjectWasSuccessful();

        /* update scan settings */
        LinkedHashMap<String, Object> fetchScanSettingsResultMap = simulateFetchScanSettingsForProject();
        List<Map<String, Object>> fetchEngineConfigurationsMap = simulateFetchEngineConfigurations();
        Map<String, Object> multiLanguageEngineConfiguration = findMapByValue(CHECKMARX_ENGINE_CONFIGURATION_NAME, fetchEngineConfigurationsMap);
        long serverReturnedEngineConfigurationId = Long.valueOf(multiLanguageEngineConfiguration.get("id").toString());

        simulateUpdateScanSettingsForProjectWereSuccessful(fetchScanSettingsResultMap, serverReturnedEngineConfigurationId);

        /* upload */
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateUploadZipFileWasSuccesful();

        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateStartScanAccepted();

        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateWaitForQueingDoneReturns("New");
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateWaitForQueingDoneReturns("New");
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateWaitForQueingDoneReturns("Finished");

        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateCheckScanAvailableReturns("Running");
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateCheckScanAvailableReturns("Running");
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateCheckScanAvailableReturns("Finished");

        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateStartReportCreationWasSuccesful();

        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateWaitForReportResultsReturns("Something");
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateWaitForReportResultsReturns("Something");
        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateWaitForReportResultsReturns("Created");

        simulateExpiredBearerTokenLeadsToLoginRequest();
        simulateDownloadReportSuccesful();

        /* execute */
        AdapterExecutionResult adapterResult = executeAndLogHistoryOnFailure(() -> adapterToTest.start(config, callback));

        /* test */
        assertEquals(CONTENT_FROM_CHECKMARX, adapterResult.getProductResult());
        assertEquals(false, adapterResult.hasBeenCanceled());
        history.assertAllRememberedUrlsWereRequested();

    }

    private void simulateExpiredBearerTokenLeadsToLoginRequest() throws JSONException {
        login(0);
    }

    @Test
    public void simulate_restart_upload_done_but_no_scan() throws Exception {
        /* prepare */

        AdapterMetaData metadata = new AdapterMetaData();
        metadata.setValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true);
        when(callback.getMetaDataOrNull()).thenReturn(metadata);

        LinkedHashMap<String, String> loginResponse = login(3600);
        simulateCheckProjectExistsReturnsTrue(loginResponse);
        /* no project creation */
        /* no upload */

        /* scan start */
        simulateStartScanAccepted();

        simulateWaitForQueingDoneReturns("New");
        simulateWaitForQueingDoneReturns("Finished");

        simulateCheckScanAvailableReturns("Running");
        simulateCheckScanAvailableReturns("Finished");

        /* report start */
        simulateStartReportCreationWasSuccesful();

        simulateWaitForReportResultsReturns("Something");
        simulateWaitForReportResultsReturns("Something");
        simulateWaitForReportResultsReturns("Created");

        /* download report */
        simulateDownloadReportSuccesful();

    }

    @Test
    public void simulate_restart_scan_already_exists_and_has_finished_but_no_report() throws Exception {
        /* prepare */

        AdapterMetaData metadata = new AdapterMetaData();
        metadata.setValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true);
        metadata.setValue(CheckmarxMetaDataID.KEY_SCAN_ID, CHECKMARX_SCAN_ID);
        when(callback.getMetaDataOrNull()).thenReturn(metadata);

        LinkedHashMap<String, String> loginResponse = login(3600);
        simulateCheckProjectExistsReturnsTrue(loginResponse);
        /* no project creation */
        /* no upload */

        /* no scan start - because reused */
        simulateWaitForQueingDoneReturns("Finished");
        simulateCheckScanAvailableReturns("Finished");

        /* report start */
        /* report start */
        simulateStartReportCreationWasSuccesful();

        simulateWaitForReportResultsReturns("Something");
        simulateWaitForReportResultsReturns("Created");

        /* download report */
        simulateDownloadReportSuccesful();

        /* execute */
        AdapterExecutionResult adapterResult = executeAndLogHistoryOnFailure(() -> adapterToTest.start(config, callback));

        /* @formatter:on */
        /* test */
        assertEquals(CONTENT_FROM_CHECKMARX, adapterResult.getProductResult());
        assertEquals(false, adapterResult.hasBeenCanceled());
        history.assertAllRememberedUrlsWereRequested();

    }

    @Test
    public void simulate_restart_and_scan_already_exists_and_also_report_exists_and_both_have_finished() throws Exception {
        /* prepare */

        AdapterMetaData metadata = new AdapterMetaData();
        metadata.setValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true);
        metadata.setValue(CheckmarxMetaDataID.KEY_SCAN_ID, CHECKMARX_SCAN_ID);
        metadata.setValue(CheckmarxMetaDataID.KEY_REPORT_ID, CHECKMARX_REPORT_ID);
        when(callback.getMetaDataOrNull()).thenReturn(metadata);

        LinkedHashMap<String, String> loginResponse = login(3600);
        simulateCheckProjectExistsReturnsTrue(loginResponse);
        /* no project creation */
        /* no upload */
        /* no scan start */
        simulateWaitForQueingDoneReturns("Finished");
        simulateCheckScanAvailableReturns("Finished");

        /* no report start */
        simulateWaitForReportResultsReturns("Created");

        /* download report */
        simulateDownloadReportSuccesful();

        /* execute */
        AdapterExecutionResult adapterResult = executeAndLogHistoryOnFailure(() -> adapterToTest.start(config, callback));

        /* @formatter:on */
        /* test */
        assertEquals(CONTENT_FROM_CHECKMARX, adapterResult.getProductResult());
        assertEquals(false, adapterResult.hasBeenCanceled());
        history.assertAllRememberedUrlsWereRequested();

    }

    @Test
    public void simulate_start__create_new_project_and_scan_token_expires_one_hour_as_usual() throws Exception {
        /* prepare */

        LinkedHashMap<String, String> loginResponse = login(3600);
        /* project creation */
        simulateCheckProjectExistsReturnsFalse(loginResponse);
        simulateCreateProjectWasSuccessful();

        /* update scan settings */
        LinkedHashMap<String, Object> fetchScanSettingsResultMap = simulateFetchScanSettingsForProject();
        List<Map<String, Object>> fetchEngineConfigurationsMap = simulateFetchEngineConfigurations();
        Map<String, Object> multiLanguageEngineConfiguration = findMapByValue(CHECKMARX_ENGINE_CONFIGURATION_NAME, fetchEngineConfigurationsMap);
        long serverReturnedEngineConfigurationId = Long.valueOf(multiLanguageEngineConfiguration.get("id").toString());

        simulateUpdateScanSettingsForProjectWereSuccessful(fetchScanSettingsResultMap, serverReturnedEngineConfigurationId);

        /* upload */
        simulateUploadZipFileWasSuccesful();

        /* scan start */
        simulateStartScanAccepted();

        simulateWaitForQueingDoneReturns("New");
        simulateWaitForQueingDoneReturns("New");
        simulateWaitForQueingDoneReturns("Finished");

        simulateCheckScanAvailableReturns("Running");
        simulateCheckScanAvailableReturns("Running");
        simulateCheckScanAvailableReturns("Finished");

        /* report start */
        simulateStartReportCreationWasSuccesful();

        simulateWaitForReportResultsReturns("Something");
        simulateWaitForReportResultsReturns("Something");
        simulateWaitForReportResultsReturns("Created");

        /* download report */
        simulateDownloadReportSuccesful();

        /* execute */
        AdapterExecutionResult adapterResult = executeAndLogHistoryOnFailure(() -> adapterToTest.start(config, callback));

        /* @formatter:on */
        /* test */
        assertEquals(CONTENT_FROM_CHECKMARX, adapterResult.getProductResult());
        assertEquals(false, adapterResult.hasBeenCanceled());
        history.assertAllRememberedUrlsWereRequested();

    }

    private void simulateDownloadReportSuccesful() {
        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/reports/sastScan/" + CHECKMARX_REPORT_ID))))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON).withBody(CONTENT_FROM_CHECKMARX)));
    }

    private void simulateStartReportCreationWasSuccesful() {
        LinkedHashMap<String, Object> scanReportCreation = new LinkedHashMap<>();
        scanReportCreation.put("reportType", "XML");
        scanReportCreation.put("scanId", CHECKMARX_SCAN_ID);

        LinkedHashMap<String, Object> scanReportAnswert = new LinkedHashMap<>();
        scanReportAnswert.put("reportId", CHECKMARX_REPORT_ID);

        stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/reports/sastScan")))).
        // .inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
                withHeader("content-type", equalTo(APPLICATION_JSON)).withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(scanReportCreation)))
                .willReturn(aResponse().withStatus(HttpStatus.ACCEPTED.value()).withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(scanReportAnswert))));
    }

    private void simulateStartScanAccepted() {
        LinkedHashMap<String, Object> startScanMap = new LinkedHashMap<>();
        startScanMap.put("comment", "sechub job:sechub-trace-id");
        startScanMap.put("forceScan", false);
        startScanMap.put("isIncremental", true);
        startScanMap.put("isPublic", false);
        startScanMap.put("projectId", CHECKMARX_PROJECT_ID);

        LinkedHashMap<String, Object> startScanResult = new LinkedHashMap<>();
        startScanResult.put("id", CHECKMARX_SCAN_ID);

        stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/sast/scans")))).
        // .inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
                withHeader("content-type", equalTo(APPLICATION_JSON + ";v=1.0"))
                .withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(startScanMap)))
                .willReturn(aResponse().withStatus(HttpStatus.CREATED.value()).withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(startScanResult))));
    }

    private void simulateUploadZipFileWasSuccesful() {
        stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/projects/" + CHECKMARX_PROJECT_ID + "/sourceCode/attachments"))))
                .withHeader("content-type", containing("multipart/form-data;boundary="))
                .withMultipartRequestBody(aMultipart().withBody(equalTo("pseudo-zip-content")).withName("zippedSource"))
                .willReturn(aResponse().withStatus(HttpStatus.NO_CONTENT.value())));
    }

    private void simulateUpdateScanSettingsForProjectWereSuccessful(LinkedHashMap<String, Object> fetchScanSettingsResultMap, long engineConfigurationId) {
        LinkedHashMap<String, Object> updateScanSettingsParamMap = new LinkedHashMap<>();
        updateScanSettingsParamMap.put("engineConfigurationId", engineConfigurationId);
        updateScanSettingsParamMap.put("presetId", CHECKMARX_SECHUB_DEFAULT_PRESET_ID);
        updateScanSettingsParamMap.put("projectId", CHECKMARX_PROJECT_ID);

        stubFor(put(urlEqualTo(history.rememberPUT(apiURLSupport.nextURL("/cxrestapi/sast/scanSettings"))))
                .withHeader("content-type", equalTo(APPLICATION_JSON + ";v=1.1"))
                .withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(updateScanSettingsParamMap)))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(fetchScanSettingsResultMap))));
    }

    private LinkedHashMap<String, Object> simulateFetchScanSettingsForProject() {
        LinkedHashMap<String, Object> fetchScanSettingsResultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> projectMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> presetMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> engineConfigurationMap = new LinkedHashMap<>();
        fetchScanSettingsResultMap.put("project", projectMap);
        fetchScanSettingsResultMap.put("preset", presetMap);
        fetchScanSettingsResultMap.put("engineConfiguration", engineConfigurationMap);
        projectMap.put("id", CHECKMARX_PROJECT_ID);
        presetMap.put("id", CHECKMARX_SECHUB_DEFAULT_PRESET_ID);
        engineConfigurationMap.put("id", CHECKMARX_CONFIGURATION_ID);

        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/sast/scanSettings/" + CHECKMARX_PROJECT_ID))))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(fetchScanSettingsResultMap))));
        return fetchScanSettingsResultMap;
    }

    private List<Map<String, Object>> simulateFetchEngineConfigurations() {
        List<Map<String, Object>> fetchEngineConfigurations = new LinkedList<>();
        LinkedHashMap<String, Object> defaultConfigurationMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> multiLanguageScanMap = new LinkedHashMap<>();

        defaultConfigurationMap.put("id", 1);
        defaultConfigurationMap.put("name", "Default Configuration");
        multiLanguageScanMap.put("id", 5);
        multiLanguageScanMap.put("name", CHECKMARX_ENGINE_CONFIGURATION_NAME);
        fetchEngineConfigurations.add(defaultConfigurationMap);
        fetchEngineConfigurations.add(multiLanguageScanMap);

        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/sast/engineConfigurations"))))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON + ";v=1.1")
                        .withBody(JSONTestUtil.toJSONContainingNullValues(fetchEngineConfigurations))));

        return fetchEngineConfigurations;
    }

    private void simulateCreateProjectWasSuccessful() {
        /* +-----------------------------------------------------------------------+ */
        /* +............................ create new project........................+ */
        /* +-----------------------------------------------------------------------+ */
        LinkedHashMap<String, Object> createProjectParam = new LinkedHashMap<>();
        createProjectParam.put("isPublic", "false");
        createProjectParam.put("name", PROJECT_NAME);
        createProjectParam.put("owningTeam", null);

        LinkedHashMap<String, Object> createProjectResult = new LinkedHashMap<>();
        createProjectResult.put("id", CHECKMARX_PROJECT_ID);
        createProjectResult.put("name", PROJECT_NAME);
        createProjectResult.put("owningTeam", null);

        stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/projects")))).
        // .inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
                withHeader("content-type", equalTo(APPLICATION_JSON + ";v=2.0"))
                .withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(createProjectParam)))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(createProjectResult))));
    }

    private void simulateCheckProjectExistsReturnsFalse(LinkedHashMap<String, String> checkProjectExistingMap) {
        /* +-----------------------------------------------------------------------+ */
        /* +............................ check for project.........................+ */
        /* +-----------------------------------------------------------------------+ */
        /* we send 404, because project not found */
        stubFor(get(
                urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/projects?" + WireMockUtil.toFormUrlEncoded(checkProjectExistingMap, true)))))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
    }

    private void simulateCheckProjectExistsReturnsTrue(LinkedHashMap<String, String> checkProjectExistingMap) {
        /* @formatter:off */
        LinkedHashMap<String,Object> createProjectResult = new LinkedHashMap<>();
        createProjectResult.put("id", CHECKMARX_PROJECT_ID);
        createProjectResult.put("name", PROJECT_NAME);
        createProjectResult.put("owningTeam", null);

        ArrayList<Map<String,Object>> results = new  ArrayList<Map<String,Object>>();
        results.add(createProjectResult);

        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/projects?"+WireMockUtil.toFormUrlEncoded(checkProjectExistingMap,true))))).
                willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(results))
                    )
        );
        /* @formatter:on */

    }

    private LinkedHashMap<String, String> login(long tokenExpiresInSeconds) throws JSONException {
        String sessionToken = "token-returned-by-checkmarx";

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("username", USERNAME);
        map.put("password", PASSWORD);
        map.put("grant_type", "password");
        map.put("scope", "sast_rest_api");
        map.put("client_id", "resource_owner_client");
        map.put("client_secret", CheckmarxConfig.DEFAULT_CLIENT_SECRET);

        /* @formatter:off */
           /* +-----------------------------------------------------------------------+ */
           /* +............................ login ....................................+ */
           /* +-----------------------------------------------------------------------+ */
           JSONObject expectedLoginResult = new JSONObject();
           expectedLoginResult.put("access_token", sessionToken);
           expectedLoginResult.put("token_type", "json");
           expectedLoginResult.put("expires_in", tokenExpiresInSeconds);

           stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/auth/identity/connect/token")))).
                   //.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
                   withHeader("content-type", equalTo(APPLICATION_FORM_URL_ENCODED_UTF_8)).
                   withRequestBody(equalTo(WireMockUtil.toFormUrlEncoded(map))).
                   willReturn(aResponse()
                       .withStatus(HttpStatus.OK.value())
                       .withHeader("Content-Type", APPLICATION_JSON)
                       .withBody(expectedLoginResult.toString()))
                   );

           LinkedHashMap<String,String> checkProjectExistingMap = new LinkedHashMap<>();
           checkProjectExistingMap.put("projectName", PROJECT_NAME);
           checkProjectExistingMap.put("teamId", null);
        return checkProjectExistingMap;
    }

    private void simulateWaitForReportResultsReturns(String value) {
        LinkedHashMap<String,Object> reportStatus = new LinkedHashMap<>();
        LinkedHashMap<String,Object> stageMap = new LinkedHashMap<>();
        reportStatus.put("status",stageMap);
        stageMap.put("value", value);
        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/reports/sastScan/"+CHECKMARX_REPORT_ID+"/status")))).
                willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(JSONTestUtil.toJSONContainingNullValues(reportStatus))));

    }

    private void simulateWaitForQueingDoneReturns(String value) {
        simulateWaitForQueingDoneReturns(value, null);
    }

    private void simulateWaitForQueingDoneReturnsFailureWithText(String failureText) {
        simulateWaitForQueingDoneReturns("Failed", failureText);
    }

    private void simulateWaitForQueingDoneReturns(String value, String failureText) {
        LinkedHashMap<String,Object> queueStatus = new LinkedHashMap<>();
        LinkedHashMap<String,Object> stageMap = new LinkedHashMap<>();
        queueStatus.put("stage",stageMap);
        stageMap.put("value", value);

        if (failureText!=null) {
            queueStatus.put("stageDetails", failureText);
        }

        String json = JSONTestUtil.toJSONContainingNullValues(queueStatus);

        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/sast/scansQueue/"+CHECKMARX_SCAN_ID)))).
                willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody(json))
                );
    }

    private void simulateCheckScanAvailableReturns(String statusName) {
        LinkedHashMap<String,Object> scanStatus = new LinkedHashMap<>();
        LinkedHashMap<String,Object> stageMap = new LinkedHashMap<>();
        scanStatus.put("status",stageMap);
        stageMap.put("name", statusName);

        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/sast/scans/"+CHECKMARX_SCAN_ID)))).
                willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody(JSONTestUtil.toJSONContainingNullValues(scanStatus)))
                );
    }

    private Map<String, Object> findMapByValue(String name, List<Map<String, Object>> listMap) {
        Map<String, Object> result = null;

        for (Map<String, Object> map : listMap) {
            if (map.containsValue(name)) {
                result = map;
            }
        }

        return result;
    }

    private AdapterExecutionResult executeAndLogHistoryOnFailure(Callable<AdapterExecutionResult> executor) throws Exception {
        try {
            return executor.call();
        } catch (Exception e) {
            System.out.println("--------------------------HISTORY------------------------");
            System.out.println(history.toString());
            System.out.println("--------------------------HISTORY (END)------------------");

            throw e;
        }
    }

}
