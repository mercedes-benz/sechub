package com.daimler.sechub.adapter.checkmarx;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.adapter.AdapterMetaDataCallback;
import com.daimler.sechub.adapter.IcrementalAdditionalPrefixAPIURLSupport;
import com.daimler.sechub.adapter.support.APIURLSupport;
import com.daimler.sechub.test.JSONTestUtil;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.WireMockUtil;
import com.daimler.sechub.test.WiremockUrlHistory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class CheckmarxAdapterV1WireMockTest {
    private static final String CONTENT_FROM_CHECKMARX = "content-from-checkmarx";
    private static final String TARGET_TYPE = "theType";
    private static final String SECHUB_TRACE_ID = "sechub-trace-id";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_FORM_URL_ENCODED_UTF_8 = "application/x-www-form-urlencoded;charset=UTF-8";
    
    private static final int HTTPS_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPSPort();

    private static final int HTTP_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPPort();
    private static final String PASSWORD = "12345BASE64_PWD";
    private static final String TARGET_URL = "http://example.org";

    private static final String CHECKMARX_BASE_URL = "http://localhost:" + HTTP_PORT;

    private static final String POLICY_ID = "12345POLICY_ID";


    private static final String USERNAME = "sechub-user";
    private static final String PROJECT_NAME = "testproject";
    
    private static final long CHECKMARX_PROJECT_ID = 10666;
    private static final long CHECKMARX_CONFIGURATION_ID=20666;
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
        
        callback=mock(AdapterMetaDataCallback.class);
        config = mock(CheckmarxAdapterConfig.class);
        
        when(config.getTraceID()).thenReturn(SECHUB_TRACE_ID);
        when(config.getPresetIdForNewProjectsOrNull()).thenReturn(CHECKMARX_SECHUB_DEFAULT_PRESET_ID);
        when(config.getUser()).thenReturn(USERNAME);
        when(config.getTargetType()).thenReturn(TARGET_TYPE);
        when(config.getPasswordOrAPIToken()).thenReturn(PASSWORD);
        when(config.getTimeOutInMilliseconds()).thenReturn(1000*5);
        when(config.getProjectId()).thenReturn(PROJECT_NAME);

        when(config.getTargetURIs()).thenReturn(Collections.singleton(URI.create(TARGET_URL)));
        when(config.getProductBaseURL()).thenReturn(CHECKMARX_BASE_URL);
        when(config.getPolicyId()).thenReturn(POLICY_ID);
        when(config.getSourceCodeZipFileInputStream()).thenReturn(new ByteArrayInputStream("pseudo-zip-content".getBytes()));
    }
    
    @Test
    public void create_new_project_and_scan() throws Exception {
       /* prepare */
      
       
       String sessionToken = "token-returned-by-checkmarx"; 
       
       LinkedHashMap<String,String> map = new LinkedHashMap<>();
       map.put("username", USERNAME);
       map.put("password", PASSWORD);
       map.put("grant_type", "password");
       map.put("scope", "sast_rest_api");
       map.put("client_id", "resource_owner_client");
       map.put("client_secret", "014DF517-39D1-4453-B7B3-9930C563627C"); // TODO maybe this must be customizable

       

       
       /* @formatter:off */
       /* +-----------------------------------------------------------------------+ */
       /* +............................ login ....................................+ */
       /* +-----------------------------------------------------------------------+ */
       JSONObject expectedLoginResult = new JSONObject();
       expectedLoginResult.put("access_token", sessionToken);
       expectedLoginResult.put("token_type", "json");
       expectedLoginResult.put("expires_in", new Date().toString());

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

       /* +-----------------------------------------------------------------------+ */
       /* +............................ check for project.........................+ */
       /* +-----------------------------------------------------------------------+ */
       /* we send 404, because project not found */
       stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/projects?"+WireMockUtil.toFormUrlEncoded(checkProjectExistingMap,true))))).
               willReturn(aResponse()
                   .withStatus(HttpStatus.NOT_FOUND.value())
               )
       );
       /* +-----------------------------------------------------------------------+ */
       /* +............................ create new project........................+ */
       /* +-----------------------------------------------------------------------+ */
       LinkedHashMap<String,Object> createProjectParam = new LinkedHashMap<>();
       createProjectParam.put("isPublic", "false");
       createProjectParam.put("name", PROJECT_NAME);
       createProjectParam.put("owningTeam", null);
       
       LinkedHashMap<String,Object> createProjectResult = new LinkedHashMap<>();
       createProjectResult.put("id", CHECKMARX_PROJECT_ID);
       createProjectResult.put("name", PROJECT_NAME);
       createProjectResult.put("owningTeam", null);
    
       stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/projects")))).
               //.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
               withHeader("content-type", equalTo(APPLICATION_JSON+";v=2.0")).
               withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(createProjectParam))).
               willReturn(aResponse()
                   .withStatus(HttpStatus.OK.value())
                   .withHeader("Content-Type", APPLICATION_JSON)
                   .withBody(JSONTestUtil.toJSONContainingNullValues(createProjectResult)))
               );
       
       /* +-----------------------------------------------------------------------+ */
       /* +............................ fetch scan settings for project...........+ */
       /* +-----------------------------------------------------------------------+ */
       LinkedHashMap<String,Object> fetchScanSettingsResultMap = new LinkedHashMap<>();
       LinkedHashMap<String,Object> projectMap = new LinkedHashMap<>();
       LinkedHashMap<String,Object> presetMap = new LinkedHashMap<>();
       LinkedHashMap<String,Object> engineConfigurationMap = new LinkedHashMap<>();
       fetchScanSettingsResultMap.put("project", projectMap);
       fetchScanSettingsResultMap.put("preset", presetMap);
       fetchScanSettingsResultMap.put("engineConfiguration",engineConfigurationMap);
       projectMap.put("id", CHECKMARX_PROJECT_ID);
       presetMap.put("id", CHECKMARX_SECHUB_DEFAULT_PRESET_ID);
       engineConfigurationMap.put("id", CHECKMARX_CONFIGURATION_ID);

       stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/sast/scanSettings/"+CHECKMARX_PROJECT_ID)))).
               willReturn(aResponse()
                   .withStatus(HttpStatus.OK.value())
                   .withHeader("Content-Type", APPLICATION_JSON)
                   .withBody(JSONTestUtil.toJSONContainingNullValues(fetchScanSettingsResultMap)))
               );
       /* +-----------------------------------------------------------------------+ */
       /* +............................ update scan settings for project..........+ */
       /* +-----------------------------------------------------------------------+ */
       LinkedHashMap<String,Object> updateScanSettingsParamMap = new LinkedHashMap<>();
       updateScanSettingsParamMap.put("engineConfigurationId",CHECKMARX_CONFIGURATION_ID);
       updateScanSettingsParamMap.put("presetId",CHECKMARX_SECHUB_DEFAULT_PRESET_ID);
       updateScanSettingsParamMap.put("projectId",CHECKMARX_PROJECT_ID);
       
       stubFor(put(urlEqualTo(history.rememberPUT(apiURLSupport.nextURL("/cxrestapi/sast/scanSettings")))).
               withHeader("content-type", equalTo(APPLICATION_JSON+";v=1.1")).
               withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(updateScanSettingsParamMap))).
               willReturn(aResponse()
                   .withStatus(HttpStatus.OK.value())
                   .withHeader("Content-Type", APPLICATION_JSON)
                   .withBody(JSONTestUtil.toJSONContainingNullValues(fetchScanSettingsResultMap)))
               );
       
       
       /* +-----------------------------------------------------------------------+ */
       /* +............................ upload content zip........................+ */
       /* +-----------------------------------------------------------------------+ */
       stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/projects/"+CHECKMARX_PROJECT_ID+"/sourceCode/attachments")))).
               withHeader("content-type", containing("multipart/form-data;charset=UTF-8")).
               withMultipartRequestBody(
                       aMultipart().
                           withBody(equalTo("pseudo-zip-content")).
                           withName("zippedSource")
                       ).
//               withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(updateScanSettingsParamMap))).
               willReturn(aResponse()
                       .withStatus(HttpStatus.NO_CONTENT.value()))
               );
       
       /* +-----------------------------------------------------------------------+ */
       /* +............................ start scan ...............................+ */
       /* +-----------------------------------------------------------------------+ */
       LinkedHashMap<String,Object> startScanMap = new LinkedHashMap<>();
       startScanMap.put("comment", "sechub job:sechub-trace-id");
       startScanMap.put("forceScan", false);
       startScanMap.put("isIncremental", true);
       startScanMap.put("isPublic", false);
       startScanMap.put("projectId", CHECKMARX_PROJECT_ID);
       
       LinkedHashMap<String,Object> startScanResult = new LinkedHashMap<>();
       startScanResult.put("id", CHECKMARX_SCAN_ID);
       
       stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/sast/scans")))).
               //.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
               withHeader("content-type", equalTo(APPLICATION_JSON+";v=1.0")).
               withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(startScanMap))).
               willReturn(aResponse()
                   .withStatus(HttpStatus.CREATED.value())
                   .withHeader("Content-Type", APPLICATION_JSON)
                   .withBody(JSONTestUtil.toJSONContainingNullValues(startScanResult)))
               );
       /* +-----------------------------------------------------------------------+ */
       /* +............................ wait for queing done......................+ */
       /* +-----------------------------------------------------------------------+ */
       simulateWaitForQueingDoneResults("New");
       simulateWaitForQueingDoneResults("New");
       simulateWaitForQueingDoneResults("Finished");
       
       /* +-----------------------------------------------------------------------+ */
       /* +............................ check scan available......................+ */
       /* +-----------------------------------------------------------------------+ */
       simulateCheckScanAvailable("Running");
       simulateCheckScanAvailable("Running");
       simulateCheckScanAvailable("Finished");

       /* +-----------------------------------------------------------------------+ */
       /* +............................ trigger report creation...................+ */
       /* +-----------------------------------------------------------------------+ */
       LinkedHashMap<String,Object> scanReportCreation = new LinkedHashMap<>();
       scanReportCreation.put("reportType","XML");
       scanReportCreation.put("scanId",CHECKMARX_SCAN_ID);
       
       LinkedHashMap<String,Object> scanReportAnswert= new LinkedHashMap<>();
       scanReportAnswert.put("reportId",CHECKMARX_REPORT_ID);
       
       stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/cxrestapi/reports/sastScan")))).
               //.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
               withHeader("content-type", equalTo(APPLICATION_JSON)).
               withRequestBody(equalToJson(JSONTestUtil.toJSONContainingNullValues(scanReportCreation))).
               willReturn(aResponse()
                   .withStatus(HttpStatus.ACCEPTED.value())
                   .withHeader("Content-Type", APPLICATION_JSON)
                   .withBody(JSONTestUtil.toJSONContainingNullValues(scanReportAnswert)))
               );
       
       /* +-----------------------------------------------------------------------+ */
       /* +............................ get report status ........................+ */
       /* +-----------------------------------------------------------------------+ */
       simulateWaitForReportResults("Something");
       simulateWaitForReportResults("Something");
       simulateWaitForReportResults("Created");
       
       /* +-----------------------------------------------------------------------+ */
       /* +............................ provide report ...........................+ */
       /* +-----------------------------------------------------------------------+ */
       stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/reports/sastScan/"+CHECKMARX_REPORT_ID)))).
               willReturn(aResponse()
                       .withStatus(HttpStatus.OK.value())
                       .withHeader("Content-Type", APPLICATION_JSON)
                       .withBody(CONTENT_FROM_CHECKMARX)));

       
       
       /* execute */
       String result = adapterToTest.start(config, callback);
       
       /* @formatter:on */
       /* test */
       assertEquals(CONTENT_FROM_CHECKMARX,result);
       history.assertAllRememberedUrlsWereRequested();
       
    }
    
    private void simulateWaitForReportResults(String value) {
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

    private void simulateWaitForQueingDoneResults(String value) {
        LinkedHashMap<String,Object> queueStatus = new LinkedHashMap<>();
        LinkedHashMap<String,Object> stageMap = new LinkedHashMap<>();
        queueStatus.put("stage",stageMap);
        stageMap.put("value", value);

        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/cxrestapi/sast/scansQueue/"+CHECKMARX_SCAN_ID)))).
                willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody(JSONTestUtil.toJSONContainingNullValues(queueStatus)))
                );
    }
    
    private void simulateCheckScanAvailable(String statusName) {
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

}
