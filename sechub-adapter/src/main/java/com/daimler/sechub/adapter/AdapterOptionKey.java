package com.daimler.sechub.adapter;

import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Adapter option keys
 * @author Albert Tregnaghi
 *
 */
public enum AdapterOptionKey {

	CLIENT_HTTP_REQUEST_INTERCEPTOR(ClientHttpRequestInterceptor.class),
	
	/**
	 * Option contains mock configuration result (lower cased) - so "green", "yellow", "red" or 
	 */
	MOCK_CONFIGURATION_RESULT(String.class);
	
	;
	
	private Class<?> valueClass;

	private <T> AdapterOptionKey(Class<T> valueClass) {
		this.valueClass=valueClass;
	}
	
	public Class<?> getValueClass() {
		return valueClass;
	}
	
}
