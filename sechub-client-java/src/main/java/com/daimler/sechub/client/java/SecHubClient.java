// SPDX-License-Identifier: MIT
package com.daimler.sechub.client.java;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.crypto.SealedObject;

import org.apache.http.client.utils.URIBuilder;

import com.daimler.sechub.commons.core.security.CryptoAccess;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Entry point for client actions. In future network access will be accessible be here as well, when
 * generated openapi parts are integrated.
 * @author Albert Tregnaghi
 *
 */
public class SecHubClient {

    private String username;
    private SealedObject sealedApiToken;
    private URI hostUri;
    private int hostPort;
    private boolean trustAll;

    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();
    
    public static SecHubClient create(String username, String apiToken, String hostUri, int hostPort) {
        return create(username, apiToken, hostUri, hostPort, false);
    }

    public static SecHubClient create(String username, String apiToken, String hostUri, int hostPort, boolean trustAll) {
        URI baseUri = null;

        try {
            baseUri = new URIBuilder(hostUri).build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Not an acceptable uri:" + hostUri, e);
        }
        return new SecHubClient(username, apiToken, baseUri, hostPort, trustAll);
    }

    private SecHubClient(String username, String apiToken, URI hostUri, int hostPort, boolean trustAll) {
        this.username = username;
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
        this.hostUri = hostUri;
        this.hostPort = hostPort;
        this.trustAll = trustAll;
    }

    public String getUsername() {
        return username;
    }

    public String getSealedApiToken() {
        return apiTokenAccess.unseal(sealedApiToken);
    }

    public URI getHostUri() {
        return hostUri;
    }

    public int getHostPort() {
        return hostPort;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    /**
     * Imports a a given sechub rpeort file
     * @param file
     * @return
     * @throws SecHubReportException
     */
    public static SecHubReport importSecHubJsonReport(File file) throws SecHubReportException {
        SecHubReport report = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Do not fail on unknown properties
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            report = mapper.readValue(file, SecHubReport.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new SecHubReportException("Content is not valid JSON", e);
        } catch (IOException e) {
            throw new SecHubReportException("Wasn't able to read report file", e);
        }

        return report;
    }
}
