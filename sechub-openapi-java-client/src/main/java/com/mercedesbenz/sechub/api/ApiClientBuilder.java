// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class ApiClientBuilder {

    public ApiClient createApiClient(SecHubClient client, ObjectMapper mapper) {
        HttpClient.Builder builder = HttpClient.newBuilder();
        if (client.isTrustAll()) {
            builder.sslContext(createTrustAllSSLContext());
        }

        ApiClient apiClient = new ApiClient(builder, mapper, client.getServerUri().toString());
        apiClient.setRequestInterceptor((request) -> {
            request.setHeader("Authorization", createBasicAuthenticationHeader(client));
        });
        return apiClient;

    }

    private static final String createBasicAuthenticationHeader(SecHubClient client) {
        String valueToEncode = client.getUserId() + ":" + client.getSealedApiToken();
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private SSLContext createTrustAllSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");

            TrustManager trustManager = new TrustAllManager();

            sslContext.init(null, new TrustManager[] { trustManager }, null);

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }

    }

    private class TrustAllManager extends X509ExtendedTrustManager {

        private X509Certificate[] emptyCertificatesArray = new X509Certificate[] {};

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            /* we do not check - we trust all */
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            /* we do not check - we trust all */
        }

        public X509Certificate[] getAcceptedIssuers() {
            return emptyCertificatesArray;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            /* we do not check - we trust all */
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            /* we do not check - we trust all */
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            /* we do not check - we trust all */

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            /* we do not check - we trust all */
        }
    };
}
