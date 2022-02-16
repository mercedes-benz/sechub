// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class NessusClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private NessusContext context;

    public NessusClientHttpRequestInterceptor(NessusContext context) {
        this.context = context;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.remove("content-type"); // strange, but sometimes there was a content-type (plain-text already added)
        headers.remove("Authorization");

        headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("X-Cookie", "token=" + context.nessusSessionToken);
        return execution.execute(request, body);
    }

}
