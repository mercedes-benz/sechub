// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.mercedesbenz.sechub.integrationtest.api.UserContext;

public class TestSecHubRestAPIClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(TestSecHubRestAPIClientHttpRequestInterceptor.class);

    private UserContext user;

    public TestSecHubRestAPIClientHttpRequestInterceptor(UserContext user) {
        this.user = user;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        /*
         * we always create a new base64 token, because api token in test user data does
         * change at runtime
         */

        HttpHeaders headers = request.getHeaders();
        List<String> contentType = headers.get("Content-Type");
        if (contentType == null || contentType.isEmpty()) {
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        }
        headers.remove("Authorization");

        if (!user.isAnonymous()) {
            String text = user.getUserId() + ":" + user.getApiToken();
            String base64Token = Base64.getEncoder().encodeToString(text.getBytes());
            headers.add("Authorization", "Basic " + base64Token);

        }
        LOG.trace("...............REST call for user: {} ............................", user.getUserId());

        return execution.execute(request, body);
    }

}
