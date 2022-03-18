// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.ProxyInformation;

/**
 * This class is used to test if a target URI is reachable. This way we can stop
 * the scan before it starts in case the target URI cannot be reached.
 *
 */
public class TargetConnectionChecker {
    private static final Logger LOG = LoggerFactory.getLogger(TargetConnectionChecker.class);
    private static final String TLS = "TLS";

    /**
     * Tests if site is reachable - no matter if certificate is self signed or not
     * trusted!
     *
     * @param targetUri
     * @param proxyInformation
     * @return <code>true</code> when reachable otherwise <code>false</code>
     */
    public boolean isTargetReachable(URI targetUri, ProxyInformation proxyInformation) {

        URL urlToCheckConnection;
        try {
            urlToCheckConnection = targetUri.toURL();
        } catch (MalformedURLException e) {
            throw new MustExitRuntimeException("Target URI " + targetUri + " could not be converted to URL!", null);
        }

        TrustManager pseudoTrustManager = createTrustManagerWhichTrustsEveryBody();
        SSLContext sslContext = createSSLContextForTrustManager(pseudoTrustManager);

        try {
            LOG.info("Trying to reach target URL: {}", urlToCheckConnection.toExternalForm());
            HttpURLConnection connection;
            if (proxyInformation == null) {
                connection = (HttpURLConnection) urlToCheckConnection.openConnection();
            } else {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInformation.getHost(), proxyInformation.getPort()));
                connection = (HttpURLConnection) urlToCheckConnection.openConnection(proxy);
            }
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) connection;
                httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsUrlConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
            }

            int responseCode = connection.getResponseCode();
            if (isReponseCodeValid(responseCode)) {
                LOG.info("Target is reachable.");
                return true;
            } else {
                LOG.error("Target is NOT reachable. Aborting Scan...");
            }
        } catch (IOException e) {
            LOG.error("An exception occurred while checking if target URL is reachable: {} because: {}", urlToCheckConnection.toExternalForm(), e.getMessage());
        }
        return false;

    }

    boolean isReponseCodeValid(int responseCode) {
        return responseCode < 400 || responseCode == 401 || responseCode == 403;
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

    private class AllowAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
