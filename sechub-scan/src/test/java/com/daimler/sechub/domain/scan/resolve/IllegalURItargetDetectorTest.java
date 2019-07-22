// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class IllegalURItargetDetectorTest {

	private IllegalURItargetDetector detectorToTest;
	private LoopbackAddressFinder loopBackFinder;

	@Before
	public void before() throws Exception {
		detectorToTest = new IllegalURItargetDetector();
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
		when(loopBackFinder.isLoopback("localhost")).thenReturn(true);

		/* test */
		assertIllegal("localhost");
		verify(loopBackFinder,atLeast(1)).isLoopback(eq("localhost"));
	}

	@Test
	public void non_loopbacks_are_legal() {
		/* prepare */
		when(loopBackFinder.isLoopback("example.com")).thenReturn(false);

		/* test */
		assertLegal("example.com");
		verify(loopBackFinder,atLeast(1)).isLoopback("example.com");
	}


	private void assertLegal(String hostname) {
		common_assertIllegal(hostname, false);
	}

	private void assertIllegal(String hostname) {
		common_assertIllegal(hostname, true);
	}
	private void common_assertIllegal(String hostname, boolean illegal) {
		common_assertIllegalURI(illegal,"sftp://"+hostname);
		common_assertIllegalURI(illegal,"http://"+hostname+"/index.thml");
		common_assertIllegalURI(illegal,"https://"+hostname+"/somewhere/over/the/rainbow");
		common_assertIllegalURI(illegal,"ftp://"+hostname+"/somewhere/else");
	}

	private void common_assertIllegalURI(boolean illegal, String uri) {
		boolean result = detectorToTest.isIllegal(URI.create(uri));
		assertEquals(uri, illegal,result);
	}
}
