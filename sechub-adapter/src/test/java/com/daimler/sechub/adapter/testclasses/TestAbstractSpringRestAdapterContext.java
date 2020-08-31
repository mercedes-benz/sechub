// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.daimler.sechub.adapter.AbstractSpringRestAdapterContext;
import com.daimler.sechub.adapter.AdapterRuntimeContext;

public class TestAbstractSpringRestAdapterContext extends AbstractSpringRestAdapterContext<TestAdapterConfigInterface, TestAdapterInterface> implements TestAdapterContextInterface{

	public TestAbstractSpringRestAdapterContext(TestAdapterConfigInterface config, TestAdapterInterface adapter, AdapterRuntimeContext runtimeContext) {
		super(config, adapter, runtimeContext);
	}

	@Override
	protected ClientHttpRequestInterceptor createInterceptorOrNull(TestAdapterConfigInterface config) {
		return null;
	}
	
}