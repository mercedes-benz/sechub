// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.SharedKernelTestFileSupport;
import com.daimler.sechub.sharedkernel.util.JSONConverter;
import com.daimler.sechub.test.PojoTester;

public class SecHubConfigurationTest {

	private SecHubConfiguration configurationToTest;

	@Before
	public void before() {
		configurationToTest = new SecHubConfiguration();
	}

	@Test
	public void sechub_config0_json_file_from_json_has_no_webconfig_or_infraconfig_but_api_version_1() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config0.json");

		/* execute */
		SecHubConfiguration result = SecHubConfiguration.OBJECT.fromJSON(json);

		/* test */
		assertFalse("webscan config must NOT be present", result.getWebScan().isPresent());
		assertFalse("infracan config must NOT be present", result.getInfraScan().isPresent());
		assertEquals("1.0", result.getApiVersion());
	}

	@Test
	public void sechub_config1_json_file_from_json_has_webconfig_with_url() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config1.json");

		/* execute */
		SecHubConfiguration result = SecHubConfiguration.OBJECT.fromJSON(json);

		/* test */
		assertTrue("webscan config must be present", result.getWebScan().isPresent());
		assertTrue(result.getWebScan().get().getUris().contains(new URI("https://fscan.intranet.example.org/")));
	}
	
	@Test
	public void sechub_config2_json_file_from_json_has_infraconfig_with_url() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

		/* execute */
		SecHubConfiguration result = SecHubConfiguration.OBJECT.fromJSON(json);

		/* test */
		assertTrue("infrascan config must be present", result.getInfraScan().isPresent());
		assertTrue(result.getInfraScan().get().getUris().contains(new URI("https://fscan.intranet.example.org/")));
	}
	
	@Test
	public void sechub_config2_json_file_from_json_has_infraconfig_with_ips() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");
		
		/* execute */
		SecHubConfiguration result = SecHubConfiguration.OBJECT.fromJSON(json);
		
		/* test */
		assertTrue("infrascan config must be present", result.getInfraScan().isPresent());
		List<InetAddress> ips = result.getInfraScan().get().getIps();
		assertTrue(ips.contains(InetAddress.getByName("192.168.1.1")));
		assertTrue(ips.contains(InetAddress.getByName("58.112.44.32")));
	}	
	
	@Test
	public void sechub_config2_json_file_from_json_has_no_codescanconfig() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");
		
		/* execute */
		SecHubConfiguration result = SecHubConfiguration.OBJECT.fromJSON(json);
		
		/* test */
		assertFalse("codescan config must NOT be present", result.getCodeScan().isPresent());
	
	}
	
		
	@Test
	public void sechub_config4_json_file_from_json_has_codescanconfig_with_folders() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config4.json");
		
		/* execute */
		SecHubConfiguration result = SecHubConfiguration.OBJECT.fromJSON(json);
		
		/* test */
		assertTrue("codescan config must be present", result.getCodeScan().isPresent());
		List<String> ips = result.getCodeScan().get().getFileSystem().get().getFolders();
		assertTrue(ips.contains("src/main/java"));
		assertTrue(ips.contains("src/main/resources"));
	}		


	@Test
	public void new_instance_returns_not_null_for_asJSON() throws Exception {
		assertNotNull(configurationToTest.toJSON());
	}

	@Test
	public void new_instance_returns_null_for_getApiVersion() {
		assertNull(configurationToTest.getApiVersion());
	}

	@Test
	public void uses_json_converter_when_toJSON_is_called() throws Exception {
		/* prepare */
		JSONConverter mockedConverter = mock(JSONConverter.class);

		// - integrate mocked converter not possible otherwise
		SecHubConfiguration specialConfigurationToTest = new SecHubConfiguration() {
			@Override
			public JSONConverter getConverter() {
				return mockedConverter;
			}
		};
		when(mockedConverter.toJSON(specialConfigurationToTest)).thenReturn("mockedJSONResult");

		/* execute */
		String result = specialConfigurationToTest.toJSON();

		/* test */
		verify(mockedConverter).toJSON(specialConfigurationToTest);
		assertEquals("mockedJSONResult", result);
	}

	@Test
	public void configuration_setter_getter_testing() throws Exception {

		PojoTester.testSetterAndGetter(new SecHubConfiguration());

	}

	@Test
	public void when_webscan_set_its_present() {
		/* prepare */
		configurationToTest.setWebScan(mock(SecHubWebScanConfiguration.class));

		/* test */
		assertTrue(configurationToTest.getWebScan().isPresent());
	}
	
	@Test
	public void when_infracan_set_its_present() {
		/* prepare */
		configurationToTest.setInfraScan(mock(SecHubInfrastructureScanConfiguration.class));

		/* test */
		assertTrue(configurationToTest.getInfraScan().isPresent());
	}

}
