// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.testclasses.TestAbstractSpringRestAdapterContext;
import com.daimler.sechub.adapter.testclasses.TestAdapterConfigInterface;
import com.daimler.sechub.adapter.testclasses.TestAdapterInterface;

public class AbstractSpringRestAdapterContextTest {
	/*
	 * we use the TestAbstractSpringRestAdapterContext as a representative for
	 * AbstractSpringRestAdapterContext
	 */
	private TestAbstractSpringRestAdapterContext contextToTest;
	private TestAdapterConfigInterface config;
	private TestAdapterInterface adapter;
    private AdapterRuntimeContext runtimeContext;

	@Before
	public void before() throws Exception {
		config = mock(TestAdapterConfigInterface.class);
		adapter = mock(TestAdapterInterface.class);
		runtimeContext = mock(AdapterRuntimeContext.class);

		when(config.getProductBaseURL()).thenReturn("http://localhost");

        contextToTest = new TestAbstractSpringRestAdapterContext(config, adapter, runtimeContext);
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

		Thread.sleep(2);

		/* execute + test */
		assertTrue(contextToTest.isTimeOut());
	}
}
