// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * This http request interceptor does a trace logging of all adapter
 * communication when log level for this logger is set to trace.<br>
 * <br>
 *
 * @author Albert Tregnaghi
 *
 */
public class TraceLogClientHTTPRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(TraceLogClientHTTPRequestInterceptor.class);

    @Override
    public final ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);

        log(request, body, response);

        return response;
    }

    private void log(HttpRequest request, byte[] body, ClientHttpResponse response) throws IOException {
        if (!LOG.isTraceEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\nRequest:");
        sb.append("\n Method:");
        sb.append(request.getMethod());
        sb.append("\n Headers:");
        sb.append(request.getHeaders().toString());
        sb.append("\n URI:");
        sb.append(request.getURI().toString());
        sb.append("\n Body:\n");
        sb.append(new String(body, "UTF-8"));
        sb.append("\n\nResponse:");
        sb.append("\n Status:");
        sb.append(response.getStatusCode());
        sb.append(":");
        sb.append(response.getStatusText());
        sb.append("\n Headers:");
        sb.append(response.getHeaders().toString());
        sb.append("\n Body:");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(response.getBody()))) {
            sb.append(buffer.lines().collect(Collectors.joining("\n")));
        } catch (Exception e) {
            sb.append(" Body not accessible - reason::" + e.getMessage());
        }
        sb.append(response.getHeaders().toString());

        String message = sb.toString();
        LOG.debug(message);
    }
}