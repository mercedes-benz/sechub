// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Map;

import com.daimler.sechub.adapter.support.JSONAdapterSupport;

/**
 * Base context for adapter execution. Contains adapter config, adapter , runtime context and json adapter support.
 * simple String as result).
 * 
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractAdapterContext<C extends AdapterConfig, A extends Adapter<C>>
		implements AdapterContext<C> {

	private C config;

	private String productContextId;
	private long started;

	private String result;
	private A adapter;

	private JSONAdapterSupport jsonAdapterSupport;

    private AdapterRuntimeContext runtimeContext;

	public AbstractAdapterContext(C config, A adapter, AdapterRuntimeContext runtimeContext) {
		if (config == null) {
			throw new IllegalArgumentException("config may not be null");
		}
		if (adapter == null) {
			throw new IllegalArgumentException("adapter may not be null");
		}
		if (runtimeContext == null) {
            throw new IllegalArgumentException("runtimeContext may not be null");
        }
		
		this.config = config;
		this.adapter = adapter;
		this.runtimeContext=runtimeContext;
		this.started = System.currentTimeMillis();

	}
	
	public AdapterRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
	
	@Override
	public AdapterException asAdapterException(String message, Throwable t) {
		return adapter.asAdapterException(message, t, getConfig());
	}

	@Override
	public String getAPIURL(String apiPart) {
		return getAPIURL(apiPart, null);
	}

	@Override
	public String getAPIURL(String apiPart, Map<String, String> map) {
		return this.adapter.createAPIURL(apiPart, this.config, map);
	}

	@Override
	public final void setProductContextId(String netsparkerId) {
		this.productContextId = netsparkerId;
	}

	protected final A getAdapter() {
		return adapter;
	}

	/**
	 * Returns an id used by the product to identify the request/session
	 * 
	 * @return id
	 */
	@Override
	public final String getProductContextId() {
		return productContextId;
	}

	@Override
	public JSONAdapterSupport json() {
		if (jsonAdapterSupport == null) {
			jsonAdapterSupport = new JSONAdapterSupport(adapter, this);
		}
		return jsonAdapterSupport;
	}

	/**
	 * @return configuration, never <code>null</code>
	 */
	@Override
	public final C getConfig() {
		return config;
	}

	@Override
	public final void setResult(String result) {
		this.result = result;
	}

	@Override
	public final String getResult() {
		return result;
	}

	@Override
	public final boolean isTimeOut() {
		long millis = getMillisecondsRun();
		return millis > config.getTimeOutInMilliseconds();
	}

	@Override
	public final long getMillisecondsRun() {
		return System.currentTimeMillis() - started;
	}

	@Override
	public String getTraceID() {
		return getConfig().getTraceID();
	}

}
