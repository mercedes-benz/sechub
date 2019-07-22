// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.testclasses.TestAbstractSpringRestAdapterContext;
import com.daimler.sechub.adapter.testclasses.TestAdapterConfigInterface;
import com.daimler.sechub.adapter.testclasses.TestAdapterInterface;

public class AbstractSpringRestAdapterContextTest {
	/*
	 * we use the TestAbstractSpringRestAdapterContext as an representant for
	 * AbstractSpringRestAdapterContext
	 */
	private TestAbstractSpringRestAdapterContext contextToTest;
	private TestAdapterConfigInterface config;
	private TestAdapterInterface adapter;

	@Before
	public void before() throws Exception {
		config = mock(TestAdapterConfigInterface.class);
		adapter = mock(TestAdapterInterface.class);

		when(config.getProductBaseURL()).thenReturn("http://localhost");
		when(config.getTargetURI()).thenReturn(new URI("https://my.scan.target"));

		contextToTest = new TestAbstractSpringRestAdapterContext(config, adapter);
	}

	@Test
	public void isTimeout_returns_false_when_timeout_is_1000_slept_none() {
		/* prepare */
		when(config.getTimeOutInMilliseconds()).thenReturn(1000);

		/* execute + test */
		assertFalse(contextToTest.isTimeOut());
	}

	@Test
	public void isTimeout_returns_true_when_timeout_is_1_and_slept_2_millisecond() throws Exception {
		/* prepare */
		when(config.getTimeOutInMilliseconds()).thenReturn(1);

		Thread.sleep(2); // NOSONAR

		/* execute + test */
		assertTrue(contextToTest.isTimeOut());
	}
}
