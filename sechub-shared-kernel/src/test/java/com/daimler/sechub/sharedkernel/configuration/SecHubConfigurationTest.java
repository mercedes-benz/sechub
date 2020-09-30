// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.sharedkernel.SharedKernelTestFileSupport;
import com.daimler.sechub.sharedkernel.configuration.login.AutoDetectUserLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.BasicLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.FormLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.ScriptEntry;
import com.daimler.sechub.sharedkernel.configuration.login.WebLoginConfiguration;
import com.daimler.sechub.test.PojoTester;

public class SecHubConfigurationTest {

	private SecHubConfiguration configurationToTest;
	private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

	@Before
	public void before() {
		configurationToTest = new SecHubConfiguration();
	}

	@Test
	public void webscan_login_basic_json_has_webconfig_as_expected() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_basic.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
		assertTrue("webscan config must be present", webScanOption.isPresent());

		SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
		Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
		assertTrue("login config must be present", loginOption.isPresent());
		WebLoginConfiguration loginConfiguration = loginOption.get();
		assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

		/*-- basic --*/
		Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
		assertTrue("basic login config must be present", basic.isPresent());
		assertEquals("realm0", basic.get().getRealm().get());
		assertEquals("user0", new String(basic.get().getUser()));
		assertEquals("pwd0", new String(basic.get().getPassword()));

		/*-- form --*/
		Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
		assertFalse("form login config must NOT be present", form.isPresent());

	}

	@Test
	public void webscan_login_form_autodetec_json_has_webconfig_as_expected() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_autodetect.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
		assertTrue("webscan config must be present", webScanOption.isPresent());

		SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
		Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
		assertTrue("login config must be present", loginOption.isPresent());
		WebLoginConfiguration loginConfiguration = loginOption.get();
		assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

		Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
		assertFalse("basic login config must NOT be present", basic.isPresent());

		/*-- form --*/
		Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
		assertTrue("form login config must be present", form.isPresent());

		/*-- form : auto detect --*/
		Optional<AutoDetectUserLoginConfiguration> autodetect = form.get().getAutodetect();
		assertTrue("auto detect config must be present", autodetect.isPresent());
		assertEquals("user1", new String(autodetect.get().getUser()));
		assertEquals("pwd1", new String(autodetect.get().getPassword()));

		Optional<List<ScriptEntry>> script = form.get().getScript();
		assertFalse("script config must NOT be present", script.isPresent());
	}

	@Test
	public void webscan_login_form_script_json_has_webconfig_as_expected() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_login_form_script.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
		assertTrue("webscan config must be present", webScanOption.isPresent());

		SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
		Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
		assertTrue("login config must be present", loginOption.isPresent());
		WebLoginConfiguration loginConfiguration = loginOption.get();
		assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

		Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
		assertFalse("basic login config must NOT be present", basic.isPresent());

		/*-- form --*/
		Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
		assertTrue("form login config must be present", form.isPresent());

		/*-- form : auto detect --*/
		Optional<AutoDetectUserLoginConfiguration> autodetect = form.get().getAutodetect();
		assertFalse("auto detect config must NOT be present", autodetect.isPresent());

		Optional<List<ScriptEntry>> script = form.get().getScript();
		assertTrue("script config must be present", script.isPresent());
		List<ScriptEntry> entries = script.get();
		assertEquals("Must have 3 script entries", 3,entries.size());
		ScriptEntry entry1 = entries.get(0);
		ScriptEntry entry2 = entries.get(1);
		ScriptEntry entry3 = entries.get(2);

		assertEquals("input",entry1.getStep());
		assertEquals("#example_login_userid",entry1.getSelector().get());
		assertEquals("user2",entry1.getValue().get());

		assertEquals("input",entry2.getStep());
		assertEquals("#example_login_pwd",entry2.getSelector().get());
		assertEquals("pwd2",entry2.getValue().get());

		assertEquals("click",entry3.getStep());
		assertEquals("#example_login_login_button",entry3.getSelector().get());

	}
	@Test
	public void webscan_alloptions_json_has_webconfig_with_all_examples() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("webscan/webscan_alloptions.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		Optional<SecHubWebScanConfiguration> webScanOption = result.getWebScan();
		assertTrue("webscan config must be present", webScanOption.isPresent());

		SecHubWebScanConfiguration secHubWebScanConfiguration = webScanOption.get();
		Optional<WebLoginConfiguration> loginOption = secHubWebScanConfiguration.getLogin();
		assertTrue("login config must be present", loginOption.isPresent());
		WebLoginConfiguration loginConfiguration = loginOption.get();
		assertEquals(new URL("https://productfailure.demo.example.org/login"), loginConfiguration.getUrl());

		/*-- basic --*/
		Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
		assertTrue("basic login config must be present", basic.isPresent());
		assertEquals("realm0", basic.get().getRealm().get());
		assertEquals("user0", new String(basic.get().getUser()));
		assertEquals("pwd0", new String(basic.get().getPassword()));

		/*-- form --*/
		Optional<FormLoginConfiguration> form = loginConfiguration.getForm();
		assertTrue("form login config must be present", form.isPresent());

		/*-- form : auto detect --*/
		Optional<AutoDetectUserLoginConfiguration> autodetect = form.get().getAutodetect();
		assertTrue("auto detect config must be present", autodetect.isPresent());
		assertEquals("user1", new String(autodetect.get().getUser()));
		assertEquals("pwd1", new String(autodetect.get().getPassword()));

		/*-- form : script --*/
		Optional<List<ScriptEntry>> script = form.get().getScript();
		assertTrue("script config must be present", script.isPresent());
		List<ScriptEntry> entries = script.get();
		assertEquals("Must have 3 script entries", 3,entries.size());
		ScriptEntry entry1 = entries.get(0);
		ScriptEntry entry2 = entries.get(1);
		ScriptEntry entry3 = entries.get(2);

		assertEquals("input",entry1.getStep());
		assertEquals("#example_login_userid",entry1.getSelector().get());
		assertEquals("user2",entry1.getValue().get());

		assertEquals("input",entry2.getStep());
		assertEquals("#example_login_pwd",entry2.getSelector().get());
		assertEquals("pwd2",entry2.getValue().get());

		assertEquals("click",entry3.getStep());
		assertEquals("#example_login_login_button",entry3.getSelector().get());

	}
	@Test
	public void sechub_config0_json_file_from_json_has_no_webconfig_or_infraconfig_but_api_version_1() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config0.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

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
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		assertTrue("webscan config must be present", result.getWebScan().isPresent());
		assertTrue(result.getWebScan().get().getUris().contains(new URI("https://fscan.intranet.example.org/")));
	}

	@Test
	public void sechub_config2_json_file_from_json_has_infraconfig_with_url() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		assertTrue("infrascan config must be present", result.getInfraScan().isPresent());
		assertTrue(result.getInfraScan().get().getUris().contains(new URI("https://fscan.intranet.example.org/")));
	}

	@Test
	public void sechub_config2_json_file_from_json_has_infraconfig_with_ips() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config2.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

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
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

		/* test */
		assertFalse("codescan config must NOT be present", result.getCodeScan().isPresent());

	}


	@Test
	public void sechub_config4_json_file_from_json_has_codescanconfig_with_folders() throws Exception {
		/* prepare */
		String json = SharedKernelTestFileSupport.getTestfileSupport().loadTestFile("sechub_config4.json");

		/* execute */
		SecHubConfiguration result = SECHUB_CONFIG.fromJSON(json);

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
