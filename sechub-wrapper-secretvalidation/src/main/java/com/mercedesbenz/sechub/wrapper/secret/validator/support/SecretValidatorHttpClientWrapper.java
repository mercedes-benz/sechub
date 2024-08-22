// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.support;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorProperties;

@Component
public class SecretValidatorHttpClientWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorHttpClientWrapper.class);

    private static final String TLS = "TLS";

    private final HttpClient proxiedHttpClientVerifyCertificate;
    private HttpClient proxiedHttpClientIgnoreCertificate;

    private final HttpClient directHttpClientVerifyCertificate;
    private final HttpClient directHttpClientIgnoreCertificate;

    public SecretValidatorHttpClientWrapper(SecretValidatorProperties properties) {
        TrustManager pseudoTrustManager = createTrustManagerWhichTrustsEveryBody();
        SSLContext sslContext = createSSLContextForTrustManager(pseudoTrustManager);

        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
        /* @formatter:off */
        proxiedHttpClientVerifyCertificate = HttpClient.newBuilder()
                                                          .proxy(ProxySelector.getDefault())
                                                          .connectTimeout(timeout)
                                                       .build();

        proxiedHttpClientIgnoreCertificate = HttpClient.newBuilder()
                                                          .proxy(ProxySelector.getDefault())
                                                          .sslContext(sslContext)
                                                          .connectTimeout(timeout)
                                                       .build();

        directHttpClientVerifyCertificate = HttpClient.newBuilder()
                                                          .connectTimeout(timeout)
                                                      .build();

        directHttpClientIgnoreCertificate = HttpClient.newBuilder()
                                                          .sslContext(sslContext)
                                                          .connectTimeout(timeout)
                                                      .build();
        /* @formatter:on */
    }

    public HttpResponse<String> sendProxiedRequestVerifyCertificate(HttpRequest httpRequest) {
        try {
            return proxiedHttpClientVerifyCertificate.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.warn("Performing validation request via proxy with verifying certificate enabled to URL: {} failed!", httpRequest.uri());
        }
        return null;
    }

    public HttpResponse<String> sendProxiedRequestIgnoreCertificate(HttpRequest httpRequest) {
        try {
            return proxiedHttpClientIgnoreCertificate.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.warn("Performing validation request via proxy with verifying certificate disabled to URL: {} failed!", httpRequest.uri());
        }
        return null;
    }

    public HttpResponse<String> sendDirectRequestVerifyCertificate(HttpRequest httpRequest) {
        try {
            return directHttpClientVerifyCertificate.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.warn("Performing validation request directly without proxy with verifying certificate enabled to URL: {} failed!", httpRequest.uri());
        }
        return null;
    }

    public HttpResponse<String> sendDirectRequestIgnoreCertificate(HttpRequest httpRequest) {
        try {
            return directHttpClientIgnoreCertificate.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.warn("Performing validation request directly without proxy with verifying certificate disabled to URL: {} failed!", httpRequest.uri());
        }
        return null;
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
