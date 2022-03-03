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
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.config.ProxyInformation;

/**
 * This class is used to test if a target URI is reachable. This way we can stop
 * the scan before it starts in case the target URI cannot be reached.
 *
 */
public class TargetConnectionChecker {
    private static final Logger LOG = LoggerFactory.getLogger(TargetConnectionChecker.class);

    /**
     * Tests if site is reachable - no matter if certificate is self signed or not
     * trusted!
     *
     * @param targetUri
     * @param proxyInformation
     * @return <code>true</code> when reachable otherwise <code>false</code>
     */
    public static boolean isSiteReachable(URI targetUri, ProxyInformation proxyInformation) {
        URL urlToCheckConnection;
        try {
            urlToCheckConnection = targetUri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Target URI: " + targetUri + " could not be transformed into URL!");
        }
        if ("https".equals(urlToCheckConnection.getProtocol())) {
            TrustManager[] trustAllCerts = createTrustAllCertsManager();

            installAllTrustingTrustManager(urlToCheckConnection, trustAllCerts);

            HostnameVerifier allHostsValid = createAllTrustingHostNameVerifier();
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }

        try {
            LOG.info("Trying to reach target URL: {}", urlToCheckConnection.toExternalForm());
            HttpURLConnection connection;
            if (proxyInformation == null) {
                connection = (HttpURLConnection) urlToCheckConnection.openConnection();
            } else {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInformation.getHost(), proxyInformation.getPort()));
                connection = (HttpURLConnection) urlToCheckConnection.openConnection(proxy);
            }
            int responseCode = connection.getResponseCode();
            if (isReponseCodeValid(responseCode)) {
                LOG.info("Target is reachable.");
                return true;
            } else {
                LOG.info("Target is NOT reachable. Aborting Scan...");
            }
        } catch (IOException e) {
            LOG.error("An exception occurred while checking if target URL is reachable: {} because: {}", urlToCheckConnection.toExternalForm(), e.getMessage());
        }
        return false;
    }

    static boolean isReponseCodeValid(int responseCode) {
        return responseCode < 400 || responseCode == 401 || responseCode == 403;
    }

    private static TrustManager[] createTrustAllCertsManager() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        return trustAllCerts;
    }

    private static void installAllTrustingTrustManager(URL targetUrl, TrustManager[] trustAllCerts) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            LOG.error("An exception occurred while installing the all-trusting trust manager for checking if target URL is reachable: {}",
                    targetUrl.toExternalForm(), e);
        }
    }

    private static HostnameVerifier createAllTrustingHostNameVerifier() {
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        return allHostsValid;
    }

}
