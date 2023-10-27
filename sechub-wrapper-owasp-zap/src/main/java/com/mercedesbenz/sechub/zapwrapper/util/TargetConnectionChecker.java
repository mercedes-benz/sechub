// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;

/**
 * This class is used to test if a target URI is reachable. This way we can stop
 * the scan before it starts in case the target URI cannot be reached.
 *
 */
public class TargetConnectionChecker {
    private static final Logger LOG = LoggerFactory.getLogger(TargetConnectionChecker.class);
    private static final String TLS = "TLS";

    public void assertApplicationIsReachable(ZapScanContext scanContext) {
        boolean isReachable = false;
        Iterator<String> iterator = scanContext.getZapURLsIncludeSet().iterator();
        while (iterator.hasNext() && isReachable == false) {
            // trying to reach the target URL and all includes until the first reachable
            // URL is found.
            String nextUrl = iterator.next();
            try {
                URL url = new URL(nextUrl);
                isReachable = isSiteCurrentlyReachable(scanContext, url, scanContext.getMaxNumberOfConnectionRetries(),
                        scanContext.getRetryWaittimeInMilliseconds());
            } catch (MalformedURLException e) {
                throw new ZapWrapperRuntimeException("URL: " + nextUrl + " is invalid. Cannot check if URL is reachable.",
                        ZapWrapperExitCode.TARGET_URL_INVALID);
            }
        }
        if (!isReachable) {
            // Build error message containing proxy if it was set.
            String errorMessage = createErrorMessage(scanContext);
            throw new ZapWrapperRuntimeException(errorMessage, ZapWrapperExitCode.TARGET_URL_NOT_REACHABLE);
        }
    }

    boolean isReponseCodeValid(int responseCode) {
        return responseCode < 500 && responseCode != 404;
    }

    private boolean isSiteCurrentlyReachable(ZapScanContext scanContext, URL url, int maxNumberOfConnectionRetries, int retryWaittimeInMilliseconds) {
        if (isTargetReachable(url, scanContext.getProxyInformation())) {
            return true;
        }
        // retry if the first connection attempt failed
        for (int i = 0; i < maxNumberOfConnectionRetries; i++) {
            wait(retryWaittimeInMilliseconds);
            if (isTargetReachable(url, scanContext.getProxyInformation())) {
                return true;
            }
        }
        // write message to the user for each URL that was not reachable
        scanContext.getZapProductMessageHelper().writeSingleProductMessage(new SecHubMessage(SecHubMessageType.WARNING,
                "The URL " + url + " was not reachable after trying " + maxNumberOfConnectionRetries + 1 + " times. It might cannot be scanned."));
        return false;
    }

    /**
     * Tests if site is reachable - no matter if certificate is self signed or not
     * trusted!
     *
     * @param targetUri
     * @param proxyInformation
     * @return <code>true</code> when reachable otherwise <code>false</code>
     */
    private boolean isTargetReachable(URL urlToCheckConnection, ProxyInformation proxyInformation) {
        TrustManager pseudoTrustManager = createTrustManagerWhichTrustsEveryBody();
        SSLContext sslContext = createSSLContextForTrustManager(pseudoTrustManager);

        try {
            LOG.info("Trying to reach URL: {}", urlToCheckConnection.toExternalForm());
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
                LOG.info("URL " + urlToCheckConnection + " is reachable.");
                return true;
            } else {
                LOG.warn("URL " + urlToCheckConnection + " is NOT reachable.");
            }
        } catch (IOException e) {
            LOG.error("An exception occurred while checking if target URL is reachable: {} because: {}", urlToCheckConnection.toExternalForm(), e.getMessage());
        }
        return false;
    }

    private void wait(int waittimeInMilliseconds) {
        try {
            Thread.sleep(waittimeInMilliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String createErrorMessage(ZapScanContext scanContext) {
        ProxyInformation proxyInformation = scanContext.getProxyInformation();

        String errorMessage = "Target url: " + scanContext.getTargetUrl() + " is not reachable";
        if (proxyInformation != null) {
            errorMessage += errorMessage + " via " + proxyInformation.getHost() + ":" + proxyInformation.getPort();
        }
        return errorMessage;
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
