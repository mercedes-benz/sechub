// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class CheckmarxClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private CheckmarxContext context;

    public CheckmarxClientHttpRequestInterceptor(CheckmarxContext context) {
        this.context = context;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (context.isOAuthenticated()) {
            /* login done ... */
            HttpHeaders headers = request.getHeaders();
            headers.remove("Authorization");
            headers.add("Authorization", context.getAuthorizationHeaderValue());
        }
        return execution.execute(request, body);
    }

}
