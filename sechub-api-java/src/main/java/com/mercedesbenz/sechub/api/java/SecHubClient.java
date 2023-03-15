// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.crypto.SealedObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

/**
 * Entry point for client actions. In future network access will be accessible
 * be here as well, when generated openapi parts are integrated.
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubClient {

    private String username;
    private SealedObject sealedApiToken;
    private URI hostUri;
    private boolean trustAll;

    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();

    public static SecHubClient create(String username, String apiToken, URI hostUri) {
        return create(username, apiToken, hostUri, false);
    }

    public static SecHubClient create(String username, String apiToken, URI hostUri, boolean trustAll) {
        return new SecHubClient(username, apiToken, hostUri, trustAll);
    }

    private SecHubClient(String username, String apiToken, URI hostUri, boolean trustAll) {
        this.username = username;
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
        this.hostUri = hostUri;
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

    public boolean isTrustAll() {
        return trustAll;
    }

    /**
     * Imports a a given sechub rpeort file
     *
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
