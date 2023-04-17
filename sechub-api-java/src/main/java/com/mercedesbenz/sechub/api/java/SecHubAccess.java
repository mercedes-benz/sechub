// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.crypto.SealedObject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;

/**
 * Provides
 * 
 * @author Albert Tregnaghi
 *
 */
public class SecHubAccess {

    private String username;
    private SealedObject sealedApiToken;
    private URI serverUri;
    private boolean trustAll;

    private CryptoAccess<String> apiTokenAccess = new CryptoAccess<>();
    private ApiClient apiClient;
    private AnonymousApi anonymousApi;
    private AdminApi adminApi;
    private static JsonMapper mapper = JsonMapperFactory.createMapper();

    public SecHubAccess(URI serverUri, String username, String apiToken) {
        this(serverUri, username, apiToken, false);
    }

    public SecHubAccess(URI serverUri, String username, String apiToken, boolean trustAll) {

        this.username = username;
        this.sealedApiToken = apiTokenAccess.seal(apiToken);
        this.serverUri = serverUri;
        this.trustAll = trustAll;

        apiClient = createApiClient();
    }

    private ApiClient getApiClient() {
        return apiClient;
    }
    
    public AnonymousApi getAnonymousApi() {
        if (anonymousApi==null) {
            anonymousApi = new AnonymousApi(getApiClient());
        }
        return anonymousApi;
    }
    
    public AdminApi getAdminApi() {
        if (adminApi==null) {
            adminApi = new AdminApi(getApiClient());
        }
        return adminApi;
    }

    private ApiClient createApiClient() {
        HttpClient.Builder builder = HttpClient.newBuilder().authenticator(new SecHubAccessAuthenticator(this));
        if (isTrustAll()) {
            builder.sslContext(createTrustAllSSLContext());
        }

        return new ApiClient(builder, mapper, getServerUri().toString());

    }

    public String getUsername() {
        return username;
    }

    public String getSealedApiToken() {
        return apiTokenAccess.unseal(sealedApiToken);
    }

    public URI getServerUri() {
        return serverUri;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    public static SecHubReport readSecHubReport(File file) throws SecHubReportException {
        SecHubReport report = null;

        try {
            report = mapper.readValue(file, SecHubReport.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new SecHubReportException("Content is not valid JSON", e);
        } catch (IOException e) {
            throw new SecHubReportException("Wasn't able to read report file", e);
        }

        return report;
    }

    private SSLContext createTrustAllSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");

            TrustManager tm = new X509TrustManager() {

                private X509Certificate[] emptyCertificatesArray = new X509Certificate[] {};

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    /* we do not check the client - we trust all */
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    /* we do not check the server - we trust all */
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return emptyCertificatesArray;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }

    }
}
