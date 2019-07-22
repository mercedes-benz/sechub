// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

public class IllegalInetAddressTargetDetectorTest {

	private IllegalInetAddressTargetDetector detectorToTest;

	private LoopbackAddressFinder loopBackFinder;

	@Before
	public void before() throws Exception {
		detectorToTest = new IllegalInetAddressTargetDetector();
		loopBackFinder = mock(LoopbackAddressFinder.class);
		detectorToTest.loopbackfinder=loopBackFinder;
	}

	@Test
	public void null_is_always_illegal() {
		detectorToTest.loopbackfinder=null; // to ensure finder is not used...
		assertTrue(detectorToTest.isIllegal(null));
	}

	@Test
	public void loopbacks_are_illegal() {
		/* prepare */
		InetAddress address = mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("127.0.0.1");

		when(loopBackFinder.isLoopback(address)).thenReturn(true);

		/* test */
		assertIllegal(address);
		verify(loopBackFinder,atLeast(1)).isLoopback(address);
	}

	@Test
	public void non_loopbacks_are_legal() {
		/* prepare */
		InetAddress address = mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("198.178.2.3");

		when(loopBackFinder.isLoopback(address)).thenReturn(false);

		/* test */
		assertLegal(address);
		verify(loopBackFinder,atLeast(1)).isLoopback(address);
	}


	private void assertLegal(InetAddress ip) {
		common_assertIllegal(ip, false);
	}

	private void assertIllegal(InetAddress ip) {
		common_assertIllegal(ip, true);
	}
	private void common_assertIllegal(InetAddress ip, boolean illegal) {
		boolean result = detectorToTest.isIllegal(ip);
		assertEquals(ip.toString(), illegal,result);
	}
}
