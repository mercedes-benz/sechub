// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.daimler.sechub.adapter.AbstractSpringRestAdapterContext;
import com.daimler.sechub.adapter.AdapterRuntimeContext;

/**
 * Context for NETSPARKER execution.
 *
 * @author Albert Tregnaghi
 *
 */
public class NetsparkerContext extends AbstractSpringRestAdapterContext<NetsparkerAdapterConfig, NetsparkerAdapter> implements NetsparkerAdapterContext {

    public NetsparkerContext(NetsparkerAdapterConfig config, NetsparkerAdapter adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
    }

    @Override
    protected ClientHttpRequestInterceptor createInterceptorOrNull(NetsparkerAdapterConfig config) {
        return new NetsparkerClientHttpRequestInterceptor(config);
    }

}
