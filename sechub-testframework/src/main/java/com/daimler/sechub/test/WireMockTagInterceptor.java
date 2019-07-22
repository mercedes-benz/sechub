// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * A special interceptor which provides a workaround for wire mock tests when
 * calling same URL multiple times. The origin approach done by wiremock with
 * "scenario" did not work - at least for getters.<br>
 * <br>
 * The workaround is following: Set an instance of the interceptor to a config. When a context provides REST access
 * it can fetch the interceptor from configuration options map and use it... So its possible for wire mock to 
 * identify the requests even when originally same. A little bit dirty, but the only way to provide it. 
 * @author Albert Tregnaghi
 *
 */
public class WireMockTagInterceptor implements ClientHttpRequestInterceptor {
	private static final String HEADER_KEY = "wiremock-workaround";
	private int value;
	private int expectedValue;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getHeaders().put(getHeaderKey(), Collections.singletonList("" + value));
		value++;
		return execution.execute(request, body);
	}

	public String getHeaderKey() {
		return HEADER_KEY;
	}

	/**
	 * @return next expected value (will increase on every call)
	 */
	public int getExpectedValue() {
		return expectedValue++;
	}
	
}
