package com.daimler.sechub.client.java.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.client.utils.URIBuilder;

public class ApiUrlBuilder {
    private static final String API_PROJECT = "api/project";
    private static final String API_ANONYMOUS = "api/anonymous";

    private String protocol;
    private String hostname;
    int port;

    public ApiUrlBuilder(String protocol, String hostname, int port) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
    }

    public URI buildCheckIsAliveUrl() throws URISyntaxException {
        return buildUrl(API_ANONYMOUS, "check/alive");
    }

    public URI buildGetJobReportUrl(String projectId, UUID jobUUID) throws URISyntaxException {
        return buildUrl(API_PROJECT, projectId, "report", jobUUID.toString());
    }

    private URI buildUrl(String... parts) throws URISyntaxException {
        String path = buildPath(parts);

        URI uri = new URIBuilder().setScheme(this.protocol).setHost(this.hostname).setPort(port).setPath(path).build();

        return uri;
    }

    private String buildPath(String... parts) {
        StringBuilder pathBuilder = new StringBuilder();

        for (Object part : parts) {
            pathBuilder.append("/");
            pathBuilder.append(part);
        }

        return pathBuilder.toString();
    }
}
