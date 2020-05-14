// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterRuntimeContext;

public class NetsparkerContextTest {
	private NetsparkerContext contextToTest;
	private NetsparkerAdapterConfig config;
	private NetsparkerAdapter adapter;
    private AdapterRuntimeContext runtimeContext;

	@Before
	public void before() throws Exception{
		config = mock(NetsparkerAdapterConfig.class);
		adapter = mock(NetsparkerAdapter.class);
		runtimeContext = mock(AdapterRuntimeContext.class);
		
		when(config.getProductBaseURL()).thenReturn("http://localhost");
		when(config.getTargetAsString()).thenReturn("https://my.scan.target");
		when(config.getRootTargetURIasString()).thenReturn("https://my.scan.target");
		
		contextToTest = new NetsparkerContext(config,adapter,runtimeContext);
	}

	@Test
	public void isTimeout_returns_false_when_timeout_is_1000_slept_none() {
		/* prepare */
		when(config.getTimeOutInMilliseconds()).thenReturn(1000);

		/* exeucte + test */
		assertFalse(contextToTest.isTimeOut());
	}

	@Test
	public void isTimeout_returns_true_when_timeout_is_1_and_slept_10_millisecond() throws Exception {
		/* prepare */
		when(config.getTimeOutInMilliseconds()).thenReturn(1);

		Thread.sleep(10); // NOSONAR

		/* exeucte + test */
		assertTrue(contextToTest.isTimeOut());
	}

}
