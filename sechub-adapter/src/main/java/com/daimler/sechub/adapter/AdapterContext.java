// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.Map;

import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.support.JSONAdapterSupport;

public interface AdapterContext<C extends AdapterConfig> extends TraceIdProvider {

	/**
	 * Set an id used by the product to identify the request/session
	 * @param productContextId
	 */
	void setProductContextId(String productContextId);

	/**
	 * Returns an id used by the product to identify the request/session
	 * 
	 * @return id
	 */
	String getProductContextId();

	RestOperations getRestOperations();

	AdapterException asAdapterException(String message, Throwable t);
	
	/**
	 * @return configuration, never <code>null</code>
	 */
	C getConfig();

	void setResult(String result);

	String getResult();

	boolean isTimeOut();

	long getMillisecondsRun();
	
	JSONAdapterSupport json();
	
	/**
	 * Gives back full API URL, containing of base url from configuration and given api path
	 * @param apiPath
	 * @return full API URL - e.g. "https://localhost/prefix/$apiPath"
	 */
	String getAPIURL(String apiPath);
	
	/**
	 * Gives back full API URL, containing of base url from configuration and given api path.
	 * At the end of the url the map entries are added
	 * @param apiPath
	 * @param map - can be <code>null</code>
	 * @return full API URL - e.g. "https://localhost/prefix/$apiPath?key1=value1&key2=value2"
	 */
	String getAPIURL(String apiPart, Map<String,String> map);

}