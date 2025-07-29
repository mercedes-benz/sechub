// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.IcrementalAdditionalPrefixAPIURLSupport;
import com.mercedesbenz.sechub.adapter.support.APIURLSupport;
import com.mercedesbenz.sechub.test.TestPortProvider;

public class NetsparkerAdapterV1WireMockTest {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";

    private static final String WEBSITE_ID = "93cc5894f38546f45f7aa8860366c07e";

    private static final int HTTP_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPPort();

    private static final String LICENSE_ID = "12345licenseID";

    private static final String BASE_64_TOKEN = "12345BASE64_TOKEN";

    private static final String ROOT_URL = "http://example.org";

    private static final String TARGET_URL = "http://example.org";

    private static final String NETSPARKER_BASE_URL = "http://localhost:" + HTTP_PORT;

    private static final String POLICY_ID = "12345POLICY_ID";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(HTTP_PORT));

    private NetsparkerAdapter adapterToTest;

    private NetsparkerAdapterConfig config;
    private IcrementalAdditionalPrefixAPIURLSupport apiURLSupport;

    @Before
    public void before() {
        apiURLSupport = new IcrementalAdditionalPrefixAPIURLSupport("netsparkertest");
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        adapterToTest = new NetsparkerAdapterV1() {
            @Override
            protected APIURLSupport createAPIURLSupport() {
                return apiURLSupport;
            }
        };

        config = mock(NetsparkerAdapterConfig.class);

        when(config.getTargetAsString()).thenReturn(TARGET_URL);
        when(config.getProductBaseURL()).thenReturn(NETSPARKER_BASE_URL);
        when(config.getCredentialsBase64Encoded()).thenReturn(BASE_64_TOKEN);
        when(config.getLicenseID()).thenReturn(LICENSE_ID);
        when(config.getPolicyId()).thenReturn(POLICY_ID);
        when(config.getAgentName()).thenReturn("agentName");
        when(config.getAgentGroupName()).thenReturn("agentGroupName");
        when(config.getTimeOutInMilliseconds()).thenReturn(1000 * 10);
        when(config.getWebsiteName()).thenReturn(WEBSITE_ID);
    }

    @Test
    public void start_scan_returns_result_when_using_agentgroup() throws Exception {
        common_start_scan_returns_result(true);
    }

    @Test
    public void start_scan_returns_result_when_using_agent() throws Exception {
        common_start_scan_returns_result(false);
    }

    private void common_start_scan_returns_result(boolean configHasAgentGroup) throws Exception {
        /* prepare */

        when(config.hasAgentGroup()).thenReturn(configHasAgentGroup);

        JSONObject newWebsiteBodyJSON = new JSONObject();
        newWebsiteBodyJSON.put("RootUrl", ROOT_URL);
        newWebsiteBodyJSON.put("Name", WEBSITE_ID);
        newWebsiteBodyJSON.put("LicenseType", "Subscription");
        newWebsiteBodyJSON.put("SubscriptionBasedProductLicenseId", LICENSE_ID);

        String createNewWebsiteBody = newWebsiteBodyJSON.toString();
        /* @formatter:off */
		/* +-----------------------------------------------------------------------+ */
    	/* +............................ check website ............................+ */
    	/* +-----------------------------------------------------------------------+ */
    	stubFor(get(urlEqualTo(apiURLSupport.nextURL("/api/1.0/websites/get?query="+WEBSITE_ID)))
        		.withHeader("Authorization", equalTo("Basic "+BASE_64_TOKEN))
        		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));
    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ create website ...........................+ */
    	/* +-----------------------------------------------------------------------+ */
    	stubFor(post(urlEqualTo(apiURLSupport.nextURL("/api/1.0/websites/new")))
        		.withHeader("Authorization", equalTo("Basic "+BASE_64_TOKEN))
        		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
        		.withRequestBody(equalToJson(createNewWebsiteBody))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.CREATED.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("[{\"ID\" : \"1234567890\"}]")));
    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ new scan .................................+ */
    	/* +-----------------------------------------------------------------------+ */
    	JSONObject newScanBodyJSON = new JSONObject();
    	newScanBodyJSON.put("TargetUri", TARGET_URL);
    	if (configHasAgentGroup) {
    		newScanBodyJSON.put("AgentGroupName", "agentGroupName");
    	}else {
    		newScanBodyJSON.put("AgentName", "agentName");
    	}
    	newScanBodyJSON.put("ExcludeAuthenticationPages", "true");
    	newScanBodyJSON.put("PolicyId", POLICY_ID);

    	String newScanBodyJSONString = newScanBodyJSON.toString();

    	stubFor(post(urlEqualTo(apiURLSupport.nextURL("/api/1.0/scans/new")))
        		.withHeader("Authorization", equalTo("Basic "+BASE_64_TOKEN))
        		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
        		.withRequestBody(equalToJson(newScanBodyJSONString))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.CREATED.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("[{\"Id\" : \"1234567890\"}]")));
    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ check scan state .........................+ */
    	/* +-----------------------------------------------------------------------+ */
    	 stubFor(get(urlEqualTo(apiURLSupport.nextURL("/api/1.0/scans/status/1234567890")))
    			.inScenario("checkState").whenScenarioStateIs(Scenario.STARTED).willSetStateTo("scanning")
         		.withHeader("Authorization", equalTo("Basic "+BASE_64_TOKEN))
         		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
                 .willReturn(aResponse()
                     .withStatus(HttpStatus.OK.value())
                     .withHeader("Content-Type", APPLICATION_JSON)
                     .withBody("{\"State\":\"Scanning\",\"EstimatedSteps\":5000,\"CompletedSteps\":5000,\"EstimatedLaunchTime\":null}")));
    	/* hm.. this does not work - only the last stub is used...*/
        stubFor(get(urlEqualTo(apiURLSupport.nextURL("/api/1.0/scans/status/1234567890")))
        		.inScenario("checkState").whenScenarioStateIs("scanning").willSetStateTo("complete")
        		.withHeader("Authorization", equalTo("Basic "+BASE_64_TOKEN))
        		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"State\":\"Complete\",\"EstimatedSteps\":5000,\"CompletedSteps\":5000,\"EstimatedLaunchTime\":null}")));

        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
        		+ "<netsparker-cloud generated=\"13/02/2018 16:23\"/>";

    	/* +-----------------------------------------------------------------------+ */
    	/* +............................ get report ..... .........................+ */
    	/* +-----------------------------------------------------------------------+ */
        stubFor(get(urlEqualTo(apiURLSupport.nextURL("/api/1.0/scans/report/1234567890?Type=Vulnerabilities&Format=Xml")))
        		.withHeader("Authorization", equalTo("Basic "+BASE_64_TOKEN))
        		.withHeader("Content-Type", equalTo(APPLICATION_JSON))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", APPLICATION_XML)
                    .withBody(xml)));
        /* @formatter:on */

        AdapterMetaDataCallback callback = mock(AdapterMetaDataCallback.class);
        /* execute */
        AdapterExecutionResult adapterResult = adapterToTest.start(config, callback);

        /* test */
        verify(getRequestedFor(urlEqualTo("/netsparkertest_1/api/1.0/websites/get?query=" + WEBSITE_ID)));
        verify(postRequestedFor(urlEqualTo("/netsparkertest_2/api/1.0/websites/new")));
        verify(postRequestedFor(urlEqualTo("/netsparkertest_3/api/1.0/scans/new")));
        verify(getRequestedFor(urlEqualTo("/netsparkertest_4/api/1.0/scans/status/1234567890"))); // scanning
        verify(getRequestedFor(urlEqualTo("/netsparkertest_5/api/1.0/scans/status/1234567890"))); // complete...

        assertEquals(xml, adapterResult.getProductResult());
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}