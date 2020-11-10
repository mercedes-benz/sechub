// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class NetsparkerClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private NetsparkerAdapterConfig config;

	public NetsparkerClientHttpRequestInterceptor(NetsparkerAdapterConfig config) {
		this.config = config;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		HttpHeaders headers = request.getHeaders();
		headers.remove("Content-Type"); // strange, but sometimes there was a content-type (plain-text already added)
		headers.remove("Authorization");

		headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Authorization", "Basic " + config.getCredentialsBase64Encoded());
		return execution.execute(request, body);
	}

}
