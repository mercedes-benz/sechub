// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Map;

/**
 * Marker interface for SecHub adapters
 * 
 * @author Albert Tregnaghi
 *
 */
public interface Adapter<C extends AdapterConfig> {

	public AdapterLogId getAdapterLogId(TraceIdProvider provider);

	public AdapterCanceledByUserException asAdapterCanceledByUserException(TraceIdProvider provider);

	public AdapterException asAdapterException(String message, TraceIdProvider provider);

	public AdapterException asAdapterException(String message, Throwable t, TraceIdProvider provider);

	public String createAPIURL(String apiPart, C config);

	public String createAPIURL(String apiPart, C config, Map<String, String> map);

	public int getAdapterVersion();
	
	/**
	 * Starts and returns result 
	 * 
	 * @param config
	 * @return result
	 * @throws NessusAdapterException
	 */
	String start(C config) throws AdapterException;
}
