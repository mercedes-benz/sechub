// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetType;

public class TargetResolverServiceTest {

	@Test
	public void no_strategies_defined_uri_resolved_as_INTRANET() {
		/* prepare */
		serviceToTest.definedUriStrategy = null;
		serviceToTest.definedInetAddressStrategy = null;
		URI uri = URI.create("https://productfailure.demo.example.org");

		/* execute */
		Target found = serviceToTest.resolveTarget(uri);

		/* test */
		assertEquals(new Target(uri,TargetType.INTERNET),found);
	}

	@Test
	public void no_strategies_set_uri_resolved_as_INTRANET() {
		/* prepare */

		// simulate no strategies found, so jut new  instance:
		serviceToTest = new TargetResolverService();
		// prepare defined strategies, so wrong configured...*/
		prepareDefinedStrategies();

		URI uri = URI.create("https://productfailure.demo.example.org");

		/* execute */
		Target found = serviceToTest.resolveTarget(uri);

		/* test */
		assertEquals(new Target(uri,TargetType.INTERNET),found);

	}


	@Test
	public void no_strategies_defined_ip_resolved_as_INTRANET() throws Exception{
		/* prepare */
		serviceToTest.definedUriStrategy = null;
		serviceToTest.definedInetAddressStrategy = null;
		/* prepare */
		InetAddress address = Inet4Address.getByName("172.217.22.99");

		/* execute */
		Target found = serviceToTest.resolveTarget(address);

		/* test */
		assertEquals(new Target(address,TargetType.INTERNET),found);
	}

	@Test
	public void no_strategies_set_ip_resolved_as_INTRANET() throws Exception {
		/* prepare */

		// simulate no strategies found, so jut new  instance:
		serviceToTest = new TargetResolverService();
		// prepare defined strategies, so wrong configured...*/
		prepareDefinedStrategies();

		/* prepare */
		InetAddress address = Inet4Address.getByName("172.217.22.99");

		/* execute */
		Target found = serviceToTest.resolveTarget(address);

		/* test */
		assertEquals(new Target(address,TargetType.INTERNET),found);

	}

	@Test
	public void null_URI_is_resolved_as_unknown_without_strategy_call() throws Exception {
		/* null always handled by fallback strategy as unknown */
		assertEquals(new Target((URI) null, TargetType.UNKNOWN), serviceToTest.resolveTarget((URI) null));

		verify(uriTestStrategy1, never()).resolveTargetFor(any());
	}

	@Test
	public void null_IP_address_is_resolved_as_unknown_without_strategy_call() throws Exception {
		assertEquals(new Target((InetAddress) null, TargetType.UNKNOWN), serviceToTest.resolveTarget((InetAddress) null));
		verify(ipTestStrategy1, never()).resolveTargetFor(any());
	}

	@Test
	public void when_illegal_uri_target_detector_denies_uri_it_returns_target_but_illegal() throws Exception {
		/* prepare */
		URI uri = URI.create("illegal.example.com");
		when(illegalURITargetDetector.isIllegal(uri)).thenReturn(true);

		/* test */
		assertEquals(new Target(uri, TargetType.ILLEGAL), serviceToTest.resolveTarget(uri));
		verify(illegalURITargetDetector).isIllegal(uri);
	}

	@Test
	public void when_illegal_ip_target_detector_denies_ip_it_returns_target_but_illegal() throws Exception {

		/* prepare */
		InetAddress inetAddress = mock(InetAddress.class);
		when(illegalInetAdressTargetDetector.isIllegal(inetAddress)).thenReturn(true);

		/* execute + test */
		assertEquals(new Target(inetAddress, TargetType.ILLEGAL), serviceToTest.resolveTarget(inetAddress));

	}

	@Test
	public void inetaddress_strategy_result_is_used_as_result() throws Exception {
		/* execute + test */

		assertEquals(inetAddressTarget1, serviceToTest.resolveTarget(mock(InetAddress.class)));
	}

	@Test
	public void uri_strategy_result_is_used_as_result() throws Exception {
		/* execute + test */
		assertEquals(uriTarget1, serviceToTest.resolveTarget(URI.create("https://example.com")));
	}

	@Test
	public void path_always_resolved_as_code() throws Exception {
		assertEquals(new Target("x", TargetType.CODE_UPLOAD), serviceToTest.resolveTargetForPath("x"));
	}
	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
	/* + ................Helpers......................... + */
	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

	private TargetResolverService serviceToTest;

	private URITargetResolveStrategy uriTestStrategy1;
	private URITargetResolveStrategy uriTestStrategy2;
	private Target uriTarget1;

	private IllegalInetAddressTargetDetector illegalInetAdressTargetDetector;
	private IllegalURItargetDetector illegalURITargetDetector;

	private InetAdressTargetResolveStrategy ipTestStrategy1;
	private InetAdressTargetResolveStrategy ipTestStrategy2;
	private Target inetAddressTarget1;


	@Before
	public void before() throws Exception {
		List<InetAdressTargetResolveStrategy> inetAddressTargetResolveStrategies = new ArrayList<>();
		List<URITargetResolveStrategy> uriAddressTargetResolveStrategies = new ArrayList<>();

		/* detectors for illegal targets */
		illegalInetAdressTargetDetector = mock(IllegalInetAddressTargetDetector.class);
		illegalURITargetDetector = mock(IllegalURItargetDetector.class);

		prepareURIStrategies(uriAddressTargetResolveStrategies);
		prepareIPStrategies(inetAddressTargetResolveStrategies);

		serviceToTest = new TargetResolverService();
		serviceToTest.illegalInetAddressTargetDetector=
				illegalInetAdressTargetDetector;
		serviceToTest.illegalURItargetDetector=
				illegalURITargetDetector;
		serviceToTest.uriTargetResolveStrategies=
				uriAddressTargetResolveStrategies;
		serviceToTest.inetAddressTargetResolveStrategies = inetAddressTargetResolveStrategies;

		prepareDefinedStrategies();
	}

	private void prepareDefinedStrategies() {
		serviceToTest.definedUriStrategy = "uri-test-strategy-1"; // we use always "1"
		serviceToTest.definedInetAddressStrategy = "ip-test-strategy-1";// we use always "1"
	}

	private void prepareIPStrategies(List<InetAdressTargetResolveStrategy> inetAddressTargetResolveStrategies) {

		/* inet inetAddress test strategies */
		ipTestStrategy1 = mock(InetAdressTargetResolveStrategy.class);
		ipTestStrategy2 = mock(InetAdressTargetResolveStrategy.class);

		inetAddressTarget1 = mock(Target.class);

		when(ipTestStrategy1.initialize("ip-test-strategy-1")).thenReturn(true);
		when(ipTestStrategy1.resolveTargetFor(any())).thenReturn(inetAddressTarget1);

		when(ipTestStrategy2.initialize("ip-test-strategy-2")).thenReturn(true);
		when(ipTestStrategy2.resolveTargetFor(any())).thenReturn(null);

		inetAddressTargetResolveStrategies.add(ipTestStrategy1);
		inetAddressTargetResolveStrategies.add(ipTestStrategy2);

	}

	private void prepareURIStrategies(List<URITargetResolveStrategy> uriAddressTargetResolveStrategies) {
		/* uri test strategies */
		uriTestStrategy1 = mock(URITargetResolveStrategy.class);
		uriTestStrategy2 = mock(URITargetResolveStrategy.class);

		uriTarget1 = mock(Target.class);

		when(uriTestStrategy1.initialize("uri-test-strategy-1")).thenReturn(true);
		when(uriTestStrategy1.resolveTargetFor(any())).thenReturn(uriTarget1);

		when(uriTestStrategy2.initialize("uri-test-strategy-2")).thenReturn(true);
		when(uriTestStrategy2.resolveTargetFor(any())).thenReturn(null);

		uriAddressTargetResolveStrategies.add(uriTestStrategy1);
		uriAddressTargetResolveStrategies.add(uriTestStrategy2);
	}


}
