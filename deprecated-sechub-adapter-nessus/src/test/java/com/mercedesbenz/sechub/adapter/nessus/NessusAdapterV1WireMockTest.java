// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.IcrementalAdditionalPrefixAPIURLSupport;
import com.mercedesbenz.sechub.adapter.support.APIURLSupport;
import com.mercedesbenz.sechub.test.TestPortProvider;
import com.mercedesbenz.sechub.test.WiremockUrlHistory;

public class NessusAdapterV1WireMockTest {

    private static final String TARGET_TYPE = "theType";
    private static final String SECHUB_TRACE_ID = "sechub-trace-id";
    private static final String EXPECTED_NAME_IN_DATA = "sechub-trace-id_" + TARGET_TYPE;
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";

    private static final int HTTP_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPPort();

    private static final String PASSWORD = "12345BASE64_PWD";

    private static final String TARGET_URL = "http://example.org";

    private static final String NETSPARKER_BASE_URL = "http://localhost:" + HTTP_PORT;

    private static final String POLICY_ID = "12345POLICY_ID";

    private static final String POLICY_UUID = "12345UUID";

    private static final String USERNAME = "sechub-user";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(HTTP_PORT));

    private NessusAdapter adapterToTest;

    private NessusAdapterConfig config;
    private IcrementalAdditionalPrefixAPIURLSupport apiURLSupport;
    private WiremockUrlHistory history;

    @Before
    public void before() {
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

        apiURLSupport = new IcrementalAdditionalPrefixAPIURLSupport("nessustest");
        history = new WiremockUrlHistory();

        adapterToTest = new NessusAdapterV1() {
            @Override
            protected APIURLSupport createAPIURLSupport() {
                return apiURLSupport;
            }
        };
        config = mock(NessusAdapterConfig.class);

        when(config.getTraceID()).thenReturn(SECHUB_TRACE_ID);
        when(config.getUser()).thenReturn(USERNAME);
        when(config.getTargetType()).thenReturn(TARGET_TYPE);
        when(config.getPasswordOrAPIToken()).thenReturn(PASSWORD);
        when(config.getTimeOutInMilliseconds()).thenReturn(1000 * 5);

        when(config.getTargetURIs()).thenReturn(Collections.singleton(URI.create(TARGET_URL)));
        when(config.getProductBaseURL()).thenReturn(NETSPARKER_BASE_URL);
        when(config.getPolicyId()).thenReturn(POLICY_ID);

    }

    @Test
    public void start_scan_returns_result_when_using_agent() throws Exception {
        /* prepare */
        String sessionToken = "token-returned-by-nessus";

        JSONObject loginJson = new JSONObject();
        loginJson.put("username", USERNAME);
        loginJson.put("password", PASSWORD);

        String loginJSONBody = loginJson.toString();
        /* @formatter:off */
    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ login ....................................+ */
    	/* +-----------------------------------------------------------------------+ */
    	stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/session"))))
    			//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
    			.withHeader("X-Cookie", equalTo("token="))
        		.withHeader("content-type", equalTo(APPLICATION_JSON))
        		.withRequestBody(equalToJson(loginJSONBody))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"token\" : \""+sessionToken+"\"}"))
//                .
//                willSetStateTo(chain.getStateAfter())
                );
    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ get all policies .........................+ */
    	/* +-----------------------------------------------------------------------+ */
    	JSONObject policyTemplatesBody = new JSONObject();
    	JSONArray policyTemplates = new JSONArray();
    	for (int i=0;i<10;i++) {

    		JSONObject policyTemplate = new JSONObject();
    		policyTemplate.put("title", "title"+i);
    		policyTemplate.put("uuid", "uuid"+i);

    		policyTemplates.put(policyTemplate);

    	}
    	JSONObject policyTemplate = new JSONObject();
		policyTemplate.put("title", POLICY_ID);
		policyTemplate.put("uuid", POLICY_UUID);

		policyTemplates.put(policyTemplate);

    	policyTemplatesBody.put("templates", policyTemplates);
    	String shrinkedPolici = policyTemplatesBody.toString();

    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ fetch templates ..........................+ */
    	/* +-----------------------------------------------------------------------+ */
    	stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.nextURL("/editor/policy/templates"))))
    			.withHeader("X-Cookie", equalTo("token="+sessionToken))
        		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
        		//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
                .willReturn(aResponse()
                    .withStatus(HttpStatus.CREATED.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody(shrinkedPolici))
//                .
//                willSetStateTo(chain.getStateAfter())
                );

    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ fetch scan id.............................+ */
    	/* +-----------------------------------------------------------------------+ */
    	int scanId = 3281;
		stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/scans"))))
				//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
    			 .withHeader("X-Cookie", equalTo("token="+sessionToken))
         		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
         		.withRequestBody(equalToJson("{  \"uuid\":\""+POLICY_UUID+"\",  \"settings\":{\n" +
         				"      \"name\":\""+EXPECTED_NAME_IN_DATA+"\",\n" +
         				"      \"description\":\"SecHub scan "+SECHUB_TRACE_ID+" for target type "+TARGET_TYPE+"\",\n" +
         				"      \"text_targets\":\""+TARGET_URL+"\"\n" +
         				"  }\n" +
         				"}"))
                 .willReturn(aResponse()
                     .withStatus(HttpStatus.OK.value())
                     .withHeader("Content-Type", APPLICATION_JSON)
                     .withBody("{\"scan\":{\"id\":"+scanId+"}}"))
//                 .
//                 willSetStateTo(chain.getStateAfter())
                 );

		/* +-----------------------------------------------------------------------+ */
    	/* +............................ launch scan...............................+ */
    	/* +-----------------------------------------------------------------------+ */
		String scanUUID="6048780b-ff64-db35-5f96-dfc9a2a371b9c0c1bf76077ee30e";
		stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.nextURL("/scans/"+scanId+"/launch"))))
				 //.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
    			 .withHeader("X-Cookie", equalTo("token="+sessionToken))
         		 .withHeader("Content-Type", equalTo(APPLICATION_JSON))
                 .willReturn(aResponse()
                     .withStatus(HttpStatus.OK.value())
                     .withHeader("Content-Type", APPLICATION_JSON)
                     .withBody("{\"scan_uuid\":\""+scanUUID+"\"}"))
//                 .
//                 willSetStateTo(chain.getStateAfter())
                 );

    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ get history id for scan id ...............+ */
    	/* +-----------------------------------------------------------------------+ */
    	int historyId = 3282;
		stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.assertCheck(5).nextURL("/scans/"+scanId))))
				//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
    			.withHeader("X-Cookie", equalTo("token="+sessionToken))
         		.withHeader("Content-Type", equalTo(APPLICATION_JSON))

         		.willReturn(aResponse()
                     .withStatus(HttpStatus.OK.value())
                     .withHeader("Content-Type", APPLICATION_JSON)
                     .withBody("{\"history_id_variant_comphosts\":[],\"hosts\":[],\"notes\":null,\"remediations\":{\"remediations\":null,\"num_hosts\":0,\"num_cves\":0,\"num_impacted_hosts\":0,\"num_remediated_cves\":0},\"vulnerabilities\":[],\"filters\":[{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"TEXT\",\"type\":\"entry\",\"regex\":\".*\"},\"name\":\"hostname\",\"readable_name\":\"Hostname\"},{\"operators\":[\"eq\",\"neq\"],\"control\":{\"type\":\"dropdown\",\"list\":[\"AIX Local Security Checks\",\"Amazon Linux Local Security Checks\",\"Backdoors\",\"Brute force attacks\",\"CGI abuses\",\"CGI abuses : XSS\",\"CISCO\",\"CentOS Local Security Checks\",\"DNS\",\"Databases\",\"Debian Local Security Checks\",\"Default Unix Accounts\",\"Denial of Service\",\"F5 Networks Local Security Checks\",\"FTP\",\"Fedora Local Security Checks\",\"Firewalls\",\"FreeBSD Local Security Checks\",\"Gain a shell remotely\",\"General\",\"Gentoo Local Security Checks\",\"HP-UX Local Security Checks\",\"Huawei Local Security Checks\",\"Incident Response\",\"Junos Local Security Checks\",\"MacOS X Local Security Checks\",\"Mandriva Local Security Checks\",\"Misc.\",\"Mobile Devices\",\"Netware\",\"Offsec Plugins\",\"Offsec Plugins Disabled\",\"Oracle Linux Local Security Checks\",\"OracleVM Local Security Checks\",\"Palo Alto Local Security Checks\",\"Peer-To-Peer File Sharing\",\"Policy Compliance\",\"Port scanners\",\"RPC\",\"Red Hat Local Security Checks\",\"SCADA\",\"SMTP problems\",\"SNMP\",\"Scientific Linux Local Security Checks\",\"Service detection\",\"Settings\",\"Slackware Local Security Checks\",\"Solaris Local Security Checks\",\"SuSE Local Security Checks\",\"Ubuntu Local Security Checks\",\"VMware ESX Local Security Checks\",\"Virtuozzo Local Security Checks\",\"Web Servers\",\"Windows\",\"Windows : Microsoft Bulletins\",\"Windows : User management\"]},\"name\":\"plugin_family\",\"readable_name\":\"Plugin Family\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"NUMBER\",\"type\":\"entry\",\"regex\":\"^[0-9, ]+$\"},\"name\":\"plugin_id\",\"readable_name\":\"Plugin ID\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"TEXT\",\"type\":\"entry\",\"regex\":\".*\"},\"name\":\"plugin_name\",\"readable_name\":\"Plugin Name\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"TEXT\",\"type\":\"entry\",\"regex\":\".*\"},\"name\":\"plugin_output\",\"readable_name\":\"Plugin Output\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"80\",\"type\":\"entry\",\"regex\":\"^[0-9]+$\"},\"name\":\"port\",\"readable_name\":\"Port\"},{\"operators\":[\"eq\",\"neq\"],\"control\":{\"type\":\"dropdown\",\"list\":[\"tcp\",\"udp\",\"icmp\"]},\"name\":\"protocol\",\"readable_name\":\"Protocol\"}],\"history\":[{\"alt_targets_used\":false,\"scheduler\":0,\"status\":\"running\",\"type\":\"local\",\"uuid\":\"6048780b-ff64-db35-5f96-dfc9a2a371b9c0c1bf76077ee30e\",\"last_modification_date\":1523527046,\"creation_date\":1523527046,\"owner_id\":4,\"history_id\":"+historyId+"}],\"compliance\":[],\"info\":{\"acls\":[{\"permissions\":0,\"owner\":null,\"display_name\":null,\"name\":null,\"id\":null,\"type\":\"default\"},{\"permissions\":128,\"owner\":1,\"display_name\":\"SecHub robot\",\"name\":\"sechub\",\"id\":4,\"type\":\"user\"}],\"edit_allowed\":true,\"status\":\"running\",\"alt_targets_used\":null,\"scanner_start\":1523527046,\"policy\":\"Advanced Scan\",\"pci-can-upload\":false,\"hasaudittrail\":false,\"scan_start\":1523527046,\"user_permissions\":128,\"folder_id\":null,\"no_target\":null,\"targets\":\"http://localhost\",\"control\":true,\"timestamp\":1523527046,\"object_id\":"+scanId+",\"scanner_name\":\"Local Scanner\",\"haskb\":false,\"uuid\":\"6048780b-ff64-db35-5f96-dfc9a2a371b9c0c1bf76077ee30e\",\"hostcount\":0,\"scan_type\":\"local\",\"name\":\"FALLBACK_TRACE_ID#593858262630564\"}}"))
//         		.willSetStateTo(chain.getStateAfter())
				);

		/* +-----------------------------------------------------------------------+ */
    	/* +............................ fetch history information for  history id + */
    	/* +-----------------------------------------------------------------------+ */
		simulateCheckScanState(sessionToken, scanId, historyId, "running",6);
		simulateCheckScanState(sessionToken, scanId, historyId, "running",7);
		simulateCheckScanState(sessionToken, scanId, historyId, "completed",8);

    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ trigger export ...........................+ */
    	/* +-----------------------------------------------------------------------+ */
		int fileId=1455461011;
        String resultExport = "{\"token\":\"bd92bd4a297fcae1f9e3a7a18d9fec9269d9ab997c5e58d9fe00ade4ecf5ecb0\",\"file\":"+fileId+"}";

        stubFor(post(urlEqualTo(history.rememberPOST(apiURLSupport.assertCheck(9).nextURL("/scans/"+scanId+"/export"))))
        		//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
        		.withHeader("X-Cookie", equalTo("token="+sessionToken))
          		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
//          		.withRequestBody(equalToJson("{\"history_id\":\""+historyId+"\",\n" +
//          				"    \"format\":\"nessus\"}")) // see todo inside simulateCheckScanState about WireMock problems with requestBody in GET methods...
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody(resultExport))
//                .
//                willSetStateTo(chain.getStateAfter())
                );

        /* +-----------------------------------------------------------------------+ */
    	/* +............................ fetch status of export ...................+ */
    	/* +-----------------------------------------------------------------------+ */
        simulateServerRepsonseForFileExportStatus(sessionToken, scanId, fileId, "loading",10);
        simulateServerRepsonseForFileExportStatus(sessionToken, scanId, fileId, "loading",11);
        simulateServerRepsonseForFileExportStatus(sessionToken, scanId, fileId, "ready",12);

        /* +-----------------------------------------------------------------------+ */
    	/* +............................ Download report ..........................+ */
    	/* +-----------------------------------------------------------------------+ */
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
				+ "<NessusClientData_v2/>";
        stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.assertCheck(13).nextURL("/scans/"+scanId+"/export/"+fileId+"/download"))))
				//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
    			.withHeader("X-Cookie", equalTo("token="+sessionToken))
         		.withHeader("Content-Type", equalTo(APPLICATION_JSON))

         		.willReturn(aResponse()
                     .withStatus(HttpStatus.OK.value())
                     .withHeader("Content-Type", APPLICATION_JSON)
                     .withBody(xml))
//         		.willSetStateTo(chain.getStateAfter())
				);

        /* +-----------------------------------------------------------------------+ */
    	/* +............................ DELETE session ...........................+ */
    	/* +-----------------------------------------------------------------------+ */
        stubFor(delete(urlEqualTo(history.rememberDELETE(apiURLSupport.assertCheck(14).nextURL("/session"))))
        		//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
        		.withHeader("X-Cookie", equalTo("token="+sessionToken))
          		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_XML)
                    .withBody("{Connection=[close], Cache-Control=[], Pragma=[], Expires=[0], Content-Length=[0], Server=[NessusWWW], Date=[: Thu, 12 Apr 2018 10:30:31 GMT], X-Frame-Options=[DENY]}"))
//                .willSetStateTo(chain.getStateAfter())
        		);

        AdapterMetaDataCallback callback = mock(AdapterMetaDataCallback.class);
        /* @formatter:on */

        /* execute */
        AdapterExecutionResult adapterResult = adapterToTest.start(config, callback);

        /* test */
        history.assertAllRememberedUrlsWereRequested();

        assertEquals(xml, adapterResult.getProductResult());
    }

    private void simulateCheckScanState(String sessionToken, int scanId, int historyId, String state, int expectedCheckNr) {
        /* @formatter:off */
//		String jsonBody = "{\"history_id\":\""+historyId+"\"}";
		stubFor(get(urlEqualTo(history.rememberGET(apiURLSupport.assertCheck(expectedCheckNr).nextURL("/scans/"+scanId))))
				//.inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
				.withHeader("X-Cookie", equalTo("token="+sessionToken))
				.withHeader("Content-Type", equalTo(APPLICATION_JSON))
//				.withRequestBody(equalTo(jsonBody)) // TODO Albert Tregnaghi, 2018-04-13: Maybe change when WireMock supports it...
				                                    // WireMock has currently the problem that only post requests are checked by
				                                    // withRequestBody. It seems to be defined in stubMapping but not on find method...
													// so we use the apiUrlSupport to create unique urls and do the mapping as expected...

				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", APPLICATION_JSON)
						.withBody("{\"comphosts\":[],\"hosts\":[],\"notes\":null,\"remediations\":{\"remediations\":null,\"num_hosts\":0,\"num_cves\":0,\"num_impacted_hosts\":0,\"num_remediated_cves\":0},\"vulnerabilities\":[],\"filters\":[{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"TEXT\",\"type\":\"entry\",\"regex\":\".*\"},\"name\":\"hostname\",\"readable_name\":\"Hostname\"},{\"operators\":[\"eq\",\"neq\"],\"control\":{\"type\":\"dropdown\",\"list\":[\"AIX Local Security Checks\",\"Amazon Linux Local Security Checks\",\"Backdoors\",\"Brute force attacks\",\"CGI abuses\",\"CGI abuses : XSS\",\"CISCO\",\"CentOS Local Security Checks\",\"DNS\",\"Databases\",\"Debian Local Security Checks\",\"Default Unix Accounts\",\"Denial of Service\",\"F5 Networks Local Security Checks\",\"FTP\",\"Fedora Local Security Checks\",\"Firewalls\",\"FreeBSD Local Security Checks\",\"Gain a shell remotely\",\"General\",\"Gentoo Local Security Checks\",\"HP-UX Local Security Checks\",\"Huawei Local Security Checks\",\"Incident Response\",\"Junos Local Security Checks\",\"MacOS X Local Security Checks\",\"Mandriva Local Security Checks\",\"Misc.\",\"Mobile Devices\",\"Netware\",\"Offsec Plugins\",\"Offsec Plugins Disabled\",\"Oracle Linux Local Security Checks\",\"OracleVM Local Security Checks\",\"Palo Alto Local Security Checks\",\"Peer-To-Peer File Sharing\",\"Policy Compliance\",\"Port scanners\",\"RPC\",\"Red Hat Local Security Checks\",\"SCADA\",\"SMTP problems\",\"SNMP\",\"Scientific Linux Local Security Checks\",\"Service detection\",\"Settings\",\"Slackware Local Security Checks\",\"Solaris Local Security Checks\",\"SuSE Local Security Checks\",\"Ubuntu Local Security Checks\",\"VMware ESX Local Security Checks\",\"Virtuozzo Local Security Checks\",\"Web Servers\",\"Windows\",\"Windows : Microsoft Bulletins\",\"Windows : User management\"]},\"name\":\"plugin_family\",\"readable_name\":\"Plugin Family\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"NUMBER\",\"type\":\"entry\",\"regex\":\"^[0-9, ]+$\"},\"name\":\"plugin_id\",\"readable_name\":\"Plugin ID\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"TEXT\",\"type\":\"entry\",\"regex\":\".*\"},\"name\":\"plugin_name\",\"readable_name\":\"Plugin Name\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"TEXT\",\"type\":\"entry\",\"regex\":\".*\"},\"name\":\"plugin_output\",\"readable_name\":\"Plugin Output\"},{\"operators\":[\"eq\",\"neq\",\"match\",\"nmatch\"],\"control\":{\"readable_regex\":\"80\",\"type\":\"entry\",\"regex\":\"^[0-9]+$\"},\"name\":\"port\",\"readable_name\":\"Port\"},{\"operators\":[\"eq\",\"neq\"],\"control\":{\"type\":\"dropdown\",\"list\":[\"tcp\",\"udp\",\"icmp\"]},\"name\":\"protocol\",\"readable_name\":\"Protocol\"}],\"history\":[{\"alt_targets_used\":false,\"scheduler\":0,\"status\":\""+state+"\",\"type\":\"local\",\"uuid\":\"6048780b-ff64-db35-5f96-dfc9a2a371b9c0c1bf76077ee30e\",\"last_modification_date\":1523527046,\"creation_date\":1523527046,\"owner_id\":4,\"history_id\":"+historyId+"}],\"compliance\":[],\"info\":{\"acls\":[{\"permissions\":0,\"owner\":null,\"display_name\":null,\"name\":null,\"id\":null,\"type\":\"default\"},{\"permissions\":128,\"owner\":1,\"display_name\":\"SecHub robot\",\"name\":\"sechub\",\"id\":4,\"type\":\"user\"}],\"edit_allowed\":true,\"status\":\""+state+"\",\"alt_targets_used\":null,\"scanner_start\":1523527046,\"policy\":\"Advanced Scan\",\"pci-can-upload\":false,\"hasaudittrail\":false,\"scan_start\":1523527046,\"user_permissions\":128,\"folder_id\":null,\"no_target\":null,\"targets\":\"http://localhost\",\"control\":true,\"timestamp\":1523527046,\"object_id\":"+scanId+",\"scanner_name\":\"Local Scanner\",\"haskb\":false,\"uuid\":\"6048780b-ff64-db35-5f96-dfc9a2a371b9c0c1bf76077ee30e\",\"hostcount\":0,\"scan_type\":\"local\",\"name\":\"FALLBACK_TRACE_ID#593858262630564\"}}"))
//				.willSetStateTo(chain.getStateAfter())
				);
		/* @formatter:on */
    }

    private void simulateServerRepsonseForFileExportStatus(String sessionToken, int scanId, int fileId, String status, int expectedCheckIndex) {
        String resultExport = "{\"status\":\"" + status + "\"}";
        ;
        stubFor(get(
                urlEqualTo(history.rememberGET(apiURLSupport.assertCheck(expectedCheckIndex).nextURL("/scans/" + scanId + "/export/" + fileId + "/status"))))
                // .inScenario(chain.getScenario()).whenScenarioStateIs(chain.getStateBefore())
                .withHeader("X-Cookie", equalTo("token=" + sessionToken)).withHeader("Content-Type", equalTo(APPLICATION_JSON))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", APPLICATION_JSON).withBody(resultExport))
//                    ).
//                willSetStateTo(chain.getStateAfter())
        );
    }

}