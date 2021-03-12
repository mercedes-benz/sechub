// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.BasicLoginConfig;
import com.daimler.sechub.adapter.SecHubTimeUnit;
import com.daimler.sechub.adapter.SecHubTimeUnitData;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;

public class NetsparkerAdapterV1Test {

	private NetsparkerAdapterV1 adapterToTest;
	private NetsparkerAdapterContext context;
	private NetsparkerAdapterConfig config;
	private RestTemplate template;
	private JSONAdapterSupport jsonAdapterSupport;

	@Before
	public void before() {
		// System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		adapterToTest = new NetsparkerAdapterV1();

		context = mock(NetsparkerAdapterContext.class);
		config = mock(NetsparkerAdapterConfig.class);
		template = mock(RestTemplate.class);

		jsonAdapterSupport = new JSONAdapterSupport(adapterToTest, context);

		when(context.getConfig()).thenReturn(config);
		when(context.getRestOperations()).thenReturn(template);
		when(config.getProductBaseURL()).thenReturn("baseURL");
		when(context.getProductContextId()).thenReturn("netsparkerId");
		when(context.json()).thenReturn(jsonAdapterSupport);
	}

	@Test
	public void build_json_for_new_scan_with_basic_auth_contains_basic_login_parts() throws Exception {
		/*prepare */
		BasicLoginConfig basicLoginConfig = mock(BasicLoginConfig.class);
		when(config.getLoginConfig()).thenReturn(basicLoginConfig);
		when(basicLoginConfig.asBasic()).thenReturn(basicLoginConfig);
		when(basicLoginConfig.isBasic()).thenReturn(true);

		when(basicLoginConfig.getLoginURL()).thenReturn(new URL("https://www.example.com/"));
		when(basicLoginConfig.getUser()).thenReturn("weblogin-user");
		when(basicLoginConfig.getPassword()).thenReturn("weblogin-password");
		when(basicLoginConfig.getRealm()).thenReturn("www.example.com");

		/* execute */
		String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

		/* test */
		String expected = NetsparkerAdapterTestFileSupport.getTestfileSupport().loadTestFile("json/basic_weblogin_expected1.json");
		assertEquals(expected, json);
	}
	
	@Test
	public void build_json_for_new_scan_with_max_scan_duration_one_hour() throws Exception {
	    /* prepare */
	    SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(60, SecHubTimeUnit.MINUTE);
	    when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
	    when(config.hasMaxScanDuration()).thenReturn(true);
	    
	    /* execute */
	    String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);
	    
	    /* test */
	    String expected = NetsparkerAdapterTestFileSupport.getTestfileSupport().loadTestFile("json/max_duration_one_hour_expected.json");
	    assertEquals(expected, json);
	}
	
    @Test
    public void build_json_for_new_scan_with_max_scan_duration_5_minutes() throws Exception {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(5, SecHubTimeUnit.MINUTE);
        when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
        when(config.hasMaxScanDuration()).thenReturn(true);
        
        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);
        
        /* test */
        
        // the minimum scan duration for Netsparker is 1 hour
        String expected = NetsparkerAdapterTestFileSupport.getTestfileSupport().loadTestFile("json/max_duration_one_hour_expected.json");
        assertEquals(expected, json);
    }
    
    @Test
    public void build_json_for_new_scan_with_max_scan_duration_129_minutes() throws Exception {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(129, SecHubTimeUnit.MINUTE);
        when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
        when(config.hasMaxScanDuration()).thenReturn(true);
        
        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);
        
        /* test */
        String expected = NetsparkerAdapterTestFileSupport.getTestfileSupport().loadTestFile("json/max_duration_two_hours_expected.json");
        assertEquals(expected, json);
    }

	@Test
	public void a_fetch_report__triggers_rest_tempate_with_correct_params() {
		/* prepare */

		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = mock(ResponseEntity.class);
		when(template.getForEntity(eq("baseURL/api/1.0/scans/report/netsparkerId?Type=Vulnerabilities&Format=Xml"), eq(String.class))).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.OK);

		/* execute */
		adapterToTest.fetchReport(context);

		/* test */
		verify(template).getForEntity("baseURL/api/1.0/scans/report/netsparkerId?Type=Vulnerabilities&Format=Xml", String.class);
	}

	@Test
	public void isAbleTo_extract_id_from_netsparker_v1_0_40_109_result_when_create_new_scan_triggered() throws AdapterException {
		/* prepare */
		String body = NetsparkerAdapterTestFileSupport.getTestfileSupport().loadTestFile("netsparker_v1.0.40.109_new_scan_output.json");

		/* execute */
		String id = adapterToTest.extractIDFromScanResult(body, context);

		/* test */
		assertEquals("a42ab3cf-58e8-455e-6668-a88503af65fe", id);

	}

	@Test
	public void api_prefix_is_api_slash_1_0() throws Exception {

		/* test */
		assertEquals("api/1.0", adapterToTest.getAPIPrefix());
	}

}