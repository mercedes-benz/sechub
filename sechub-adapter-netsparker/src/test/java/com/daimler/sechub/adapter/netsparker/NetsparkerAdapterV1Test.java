// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;

public class NetsparkerAdapterV1Test {

	private NetsparkerAdapterV1 adapterToTest;
	private NetsparkerAdapterContext context;
	private NetsparkerAdapterConfig config;
	private RestTemplate template;

	@Before
	public void before() {
		// System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		adapterToTest = new NetsparkerAdapterV1();

		context = mock(NetsparkerAdapterContext.class);
		config = mock(NetsparkerAdapterConfig.class);
		template = mock(RestTemplate.class);

		when(context.getConfig()).thenReturn(config);
		when(context.getRestOperations()).thenReturn(template);
		when(config.getProductBaseURL()).thenReturn("baseURL");
		when(context.getProductContextId()).thenReturn("netsparkerId");
		when(context.json()).thenReturn(new JSONAdapterSupport(adapterToTest, context));
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
		verify(template).getForEntity("baseURL/api/1.0/scans/report/netsparkerId?Type=Vulnerabilities&Format=Xml",
				String.class);
	}

	@Test
	public void isAbleTo_extract_id_from_netsparker_v1_0_40_109_result_when_create_new_scan_triggered() throws AdapterException {
		/* prepare */
		String body = NetsparkerAdapterTestFileSupport.getTestfileSupport()
				.loadTestFile("netsparker_v1.0.40.109_new_scan_output.json");

		/* execute */
		String id = adapterToTest.extractIDFromScanResult(body,context);

		/* test */
		assertEquals("a42ab3cf-58e8-455e-6668-a88503af65fe", id);

	}

	@Test
	public void api_prefix_is_api_slash_1_0() throws Exception {

		/* test */
		assertEquals("api/1.0", adapterToTest.getAPIPrefix());
	}

}