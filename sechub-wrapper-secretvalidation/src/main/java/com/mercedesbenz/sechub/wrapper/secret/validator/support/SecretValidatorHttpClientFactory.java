// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.support;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.stereotype.Component;

@Component
public class SecretValidatorHttpClientFactory {
    private static final String TLS = "TLS";

    public HttpClient createProxyHttpClient(boolean trustAllCertificates) {
        if (trustAllCertificates) {
            TrustManager pseudoTrustManager = createTrustManagerWhichTrustsEveryBody();
            SSLContext sslContext = createSSLContextForTrustManager(pseudoTrustManager);
            /* @formatter:off */
            return HttpClient.newBuilder()
                                     .proxy(ProxySelector.getDefault())
                                     .sslContext(sslContext)
                                     .build();
            /* @formatter:on */
        } else {
            /* @formatter:off */
            return HttpClient.newBuilder()
                                     .proxy(ProxySelector.getDefault())
                                     .build();
            /* @formatter:on */
        }
    }

    public HttpClient createDirectHttpClient(boolean trustAllCertificates) {
        if (trustAllCertificates) {
            TrustManager pseudoTrustManager = createTrustManagerWhichTrustsEveryBody();
            SSLContext sslContext = createSSLContextForTrustManager(pseudoTrustManager);
            /* @formatter:off */
            return HttpClient.newBuilder()
                                     .sslContext(sslContext)
                                     .build();
            /* @formatter:on */
        } else {
            return HttpClient.newBuilder().build();
        }
    }

    private X509TrustManager createTrustManagerWhichTrustsEveryBody() {
        return new X509TrustManager() {

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
    }

    private SSLContext createSSLContextForTrustManager(TrustManager trustManager) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance(TLS);
            sslContext.init(null, new TrustManager[] { trustManager }, null);

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Was not able to create trust all context", e);
        }

    }
}
