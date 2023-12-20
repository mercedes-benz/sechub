// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.pdsclient.internal;

import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.systemtest.pdsclient.PDSClient;

public class PDSApiClientBuilder {

    public PDSApiClient createApiClient(PDSClient client, ObjectMapper mapper) {
        HttpClient.Builder builder = HttpClient.newBuilder();
        if (client.isTrustAll()) {
            builder.sslContext(createTrustAllSSLContext());
        }
        PDSApiClient apiClient = new PDSApiClient(builder, mapper, client.getServerUri().toString());
        apiClient.setRequestInterceptor((request) -> {
            request.setHeader("Authorization", createBasicAuthenticationHeader(client));
        });
        return apiClient;

    }

    private static final String createBasicAuthenticationHeader(PDSClient client) {
        String valueToEncode = client.getUsername() + ":" + client.getSealedApiToken();
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
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
