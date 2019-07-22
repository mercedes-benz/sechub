// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.testclasses;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.daimler.sechub.adapter.AbstractSpringRestAdapterContext;

public class TestAbstractSpringRestAdapterContext extends AbstractSpringRestAdapterContext<TestAdapterConfigInterface, TestAdapterInterface> implements TestAdapterContextInterface{

	public TestAbstractSpringRestAdapterContext(TestAdapterConfigInterface config, TestAdapterInterface adapter) {
		super(config, adapter);
	}

	@Override
	protected ClientHttpRequestInterceptor createInterceptorOrNull(TestAdapterConfigInterface config) {
		return null;
	}
	
}