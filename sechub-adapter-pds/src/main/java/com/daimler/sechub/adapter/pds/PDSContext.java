// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.daimler.sechub.adapter.AbstractSpringRestAdapterContext;
import com.daimler.sechub.adapter.AdapterRuntimeContext;

/**
 * Context for NETSPARKER execution.
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSContext extends AbstractSpringRestAdapterContext<PDSAdapterConfig,PDSAdapter> implements PDSAdapterContext{

	public PDSContext(PDSAdapterConfig config, PDSAdapter adapter, AdapterRuntimeContext runtimeContext)  {
		super(config,adapter,runtimeContext);
	}

	@Override
	protected ClientHttpRequestInterceptor createInterceptorOrNull(PDSAdapterConfig config) {
		return new PDSClientHttpRequestInterceptor(config);
	}

}
