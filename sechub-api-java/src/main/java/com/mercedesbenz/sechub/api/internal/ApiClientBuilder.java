package com.mercedesbenz.sechub.api.internal;

import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiClient;

public class ApiClientBuilder {

    public ApiClient createApiClient(SecHubClient client, ObjectMapper mapper) {
        HttpClient.Builder builder = HttpClient.newBuilder().authenticator(new SecHubClientAuthenticator(client));
        if (client.isTrustAll()) {
            builder.sslContext(createTrustAllSSLContext());
        }
        ApiClient apiClient = new ApiClient(builder, mapper, client.getServerUri().toString());
        return apiClient;

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
