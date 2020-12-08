// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.adapter.support.URIShrinkSupport;
import com.daimler.sechub.adapter.testclasses.TestAdapterConfigBuilder;
import com.daimler.sechub.adapter.testclasses.TestAdapterConfigInterface;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class AbstractAdapterConfigBuilderTest {

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();

	@Test
	public void getter_for_set_password_results_in_same_string_even_when_sealed() {
		/* we use now a sealed object to contain password in config builder and config it self */
		TestAdapterConfigBuilder builderToTest = new TestAdapterConfigBuilder();

		/* execute */
		TestAdapterConfigInterface config = builderToTest.setPasswordOrAPIToken("my-password").build();

		/* test */
		assertEquals("my-password",config.getPasswordOrAPIToken());

	}

	@Test
	public void rootURIShrinker_is_used_when_building() throws Exception {

		/* prepare */
		URIShrinkSupport shrinker = mock(URIShrinkSupport.class);
		TestAdapterConfigBuilder builderToTest = new TestAdapterConfigBuilder() {
			protected URIShrinkSupport createURIShrinker() {
				return shrinker;
			}
		};

		builderToTest.setProductBaseUrl("baseUrl");
		Set<URI> targetURIs = new LinkedHashSet<>();
		targetURIs.add(new URI("http://www.mycoolstuff.com/app1"));
		targetURIs.add(new URI("http://www.mycoolstuff.com/app2"));

		Set<URI> mockedShrink = new LinkedHashSet<>();
		mockedShrink.add(new URI("http://www.shrinked.com"));

		when(shrinker.shrinkToRootURIs(eq(targetURIs))).thenReturn(mockedShrink);

		/* execute */
		TestAdapterConfigInterface config = builderToTest.setTargetURIs(targetURIs).build();

		/* test */
		verify(shrinker).shrinkToRootURIs(eq(targetURIs));
		assertEquals(mockedShrink, config.getRootTargetURIs());

	}

	@Test
	public void when_projectId_is_set_its_also_in_configuration() {
		/* execute */
		TestAdapterConfigInterface configToTest = validConfigAnd().setProjectId("myproject").build();

		/* test */
		assertEquals("myproject",configToTest.getProjectId());

	}

	@Test
	public void when_no_target_url_set_the_config_has_null_as_target_uri_and_string_and_an_empty_list_of_uris()
			throws Exception {

		/* execute */
		TestAdapterConfigInterface configToTest = validConfigAnd().build();

		/* test */
		assertNull(configToTest.getTargetURI());
		assertNull(configToTest.getTargetAsString());

		assertNotNull(configToTest.getTargetURIs());
		assertTrue(configToTest.getTargetURIs().isEmpty());

	}

	@Test
	public void when_no_target_ips_set_the_config_has_an_empty_list_of_ips()
			throws Exception {

		/* execute */
		TestAdapterConfigInterface configToTest = validConfigAnd().build();

		/* test */
		assertNotNull(configToTest.getTargetIPs());
		assertTrue(configToTest.getTargetIPs().isEmpty());

	}

	@Test
	public void when_one_target_ips_set_the_config_has_one_inet_adress_entry()
			throws Exception {

		/* execute */
		TestAdapterConfigInterface configToTest = validConfigAnd().setTargetIP(InetAddress.getByName("192.168.1.1")).build();

		/* test */
		assertNotNull(configToTest.getTargetIPs());
		assertFalse(configToTest.getTargetIPs().isEmpty());
		assertTrue(configToTest.getTargetIPs().contains(InetAddress.getByName("192.168.1.1")));

	}

	@Test
	public void when_one_target_uri_is_set__target_uri_is_as_expected_and_list_is_1() throws Exception {

		/* prepare */
		String uriString = "http://www.my.cool.stuff.com";
		URI uri = new URI(uriString);
		/* execute */

		TestAdapterConfigInterface configToTest = validConfigAnd().setTargetURIs(Collections.singleton(uri)).build();

		/* test */
		assertEquals(uri, configToTest.getTargetURI());
		assertEquals(uriString, configToTest.getTargetAsString());

		assertNotNull(configToTest.getTargetURIs());
		assertEquals(1, configToTest.getTargetURIs().size());

	}

	@Test
	public void when_target_uri_is_set_by_string__target_uri_is_as_expected_and_list_is_1() throws Exception {
		/* prepare */
		String uriString = "http://www.my.cool.stuff.com";
		URI uri = new URI(uriString);
		/* execute */

		TestAdapterConfigInterface configToTest = validConfigAnd().setTargetURI(uri).build();

		/* test */
		assertEquals(uri, configToTest.getTargetURI());
		assertEquals(uriString, configToTest.getTargetAsString());

		assertNotNull(configToTest.getTargetURIs());
		assertEquals(1, configToTest.getTargetURIs().size());
	}

	@Test
	public void when_doublicated_target_uri_is_set__target_uri_is_as_expected_and_list_is_1() throws Exception {

		/* prepare */
		String uriString = "http://www.my.cool.stuff.com";
		URI uri = new URI(uriString);

		List<URI> uris = new ArrayList<>();
		uris.add(uri);
		uris.add(new URI(uriString)); // doublicated entry

		/* execute */

		TestAdapterConfigInterface configToTest = validConfigAnd().setTargetURIs(Collections.singleton(uri)).build();

		/* test */
		assertEquals(uri, configToTest.getTargetURI());
		assertEquals(uriString, configToTest.getTargetAsString());

		assertNotNull(configToTest.getTargetURIs());
		assertEquals(1, configToTest.getTargetURIs().size());

	}

	@Test
	public void isProxyDefined_false_no_proxy_set_getProxyHostname_returns_null() {
		/* prepare */
		TestAdapterConfigInterface config = validConfigAnd().setProxyPort(888).build();

		/* execute + test */
		assertNull(config.getProxyHostname());
		assertFalse(config.isProxyDefined());

	}

	@Test
	public void defining_a_proxy_hostname_but_no_port_throws_illegal_state() {
		/* prepare */
		expected.expect(IllegalStateException.class);

		/* execute */
		validConfigAnd().setProxyHostname("proxyname").build();

	}

	@Test
	public void isProxyDefined_true_when_proxy_set_getProxyHostname_and_port_config_returns_both_values() {
		/* prepare */
		TestAdapterConfigInterface config = validConfigAnd().setProxyHostname("proxyname").setProxyPort(9858).build();

		/* execute + test */
		assertEquals("proxyname", config.getProxyHostname());
		assertEquals(9858, config.getProxyPort());
		assertTrue(config.isProxyDefined());
	}

	@Test
	public void get_trace_id_returned_as_defined() throws Exception {
		/* prepare */
		/* execute */
		TestAdapterConfigInterface cfg = validConfigAnd().setTraceID("myTraceID").build();

		/* test */
		assertEquals("myTraceID", cfg.getTraceID());
	}

	@Test
	public void get_trace_id_returned_not_null_even_when_defined_null_as_defined() throws Exception {

		/* prepare */
		/* execute */
		TestAdapterConfigInterface cfg = validConfigAnd().setTraceID(null).build();

		/* test */
		assertNotNull(cfg.getTraceID());
	}

	@Test
	public void config_without_timetowait_set_has_one_minute() {
		TestAdapterConfigInterface cf1 = validConfigAnd().build();

		assertEquals(1 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
	}

	@Test
	public void config_with_timetowait_n1_has_one_minute() {
		TestAdapterConfigInterface cf1 = validConfigAnd().setTimeToWaitForNextCheckOperationInMinutes(-1).build();

		assertEquals(1 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
	}

	@Test
	public void config_without_timeout_set_has_5_days_per_default() {
		TestAdapterConfigInterface cf1 = validConfigAnd().build();

		/* 5 days a 24 hours a 60 minutes a 60 seconds a 1000 milliseconds */
		assertEquals(5 * 24 * 60 * 60 * 1000, cf1.getTimeOutInMilliseconds());
	}

	@Test
	public void config_with_timeout_n1_set_has_5_days_per_default() {
		TestAdapterConfigInterface cf1 = validConfigAnd().setScanResultTimeOutInMinutes(-1).build();

		/* 5 days a 24 hours a 60 minutes a 60 seconds a 1000 milliseconds */
		assertEquals(5 * 24 * 60 * 60 * 1000, cf1.getTimeOutInMilliseconds());
	}

	@Test
	public void config_with_timeout_set_with_3_minutes_has_3_minutes() {
		TestAdapterConfigInterface cf1 = validConfigAnd().setScanResultTimeOutInMinutes(3).build();

		/* 3 hours a 60 minutes a 60 seconds a 1000 milliseconds */
		assertEquals(3 * 60 * 1000, cf1.getTimeOutInMilliseconds());
	}

	@Test
	public void config_with_time_to_check_set_with_3_minutes_has_3_minutes() {
		TestAdapterConfigInterface cf1 = validConfigAnd().setTimeToWaitForNextCheckOperationInMinutes(3).build();

		/* 3 hours a 60 minutes a 60 seconds a 1000 milliseconds */
		assertEquals(3 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
	}

	@Test
	public void config_userid_password_set_has_correct_Base64_token() {
		/* prepare */
		String pwdOrApiToken = "pwd-developer";
		String user = "developer";

		/* execute */
		TestAdapterConfigInterface cf1 = validConfigAnd().setUser(user).setPasswordOrAPIToken(pwdOrApiToken).build();

		/* test */
		String base64Encoded = cf1.getCredentialsBase64Encoded();
		String base64Decoded = new String(Base64.getDecoder().decode(base64Encoded));
		assertEquals(user+":"+pwdOrApiToken,base64Decoded);
	}

	private TestAdapterConfigBuilder validConfigAnd() {
		return new TestAdapterConfigBuilder().setProductBaseUrl("baseUrl");
	}

}
