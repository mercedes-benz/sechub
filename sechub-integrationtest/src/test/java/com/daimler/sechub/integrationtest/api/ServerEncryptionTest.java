package com.daimler.sechub.integrationtest.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

class ServerEncryptionTest {
    private static final Logger LOG = LoggerFactory.getLogger(ServerEncryptionTest.class);

    private static final String TLS_V1_0 = "TLSv1";
    private static final String TLS_V1_3 = "TLSv1.3";
    private static final String TLS_V1_2 = "TLSv1.2";
    private static final String TLS_V1_1 = "TLSv1.1";

    @Test
    @DisplayName("Test JDK really fetches different instances for ssl context")
    // "Test that JDK fetches different instances for ssl context, so it's clear we
    // do not change it globally inside our test")
    void sanity_check_SSLContext_getInstance_returns_always_new_objects() throws Exception {
        /* prepare */
        String sslToCheck = TLS_V1_2;

        /* execute */
        SSLContext scA = SSLContext.getInstance(sslToCheck);
        SSLContext scB = SSLContext.getInstance(sslToCheck);

        /* test */
        Assertions.assertNotSame(scA, scB);
    }

    @Test
    void tls_1_2_must_be_accepted() throws Exception {
        assertProtocolAccepted(TLS_V1_2);

    }

    @Test
    void tls_1_3_must_be_accepted() throws Exception {
        assertProtocolAccepted(TLS_V1_3);

    }

    @Test
    void tls_1_1_is_not_accepted() throws Exception {
        assertProtocolNOTAccepted(TLS_V1_1);

    }

    @Test
    void tls_1_0_is_not_accepted() throws Exception {
        assertProtocolNOTAccepted(TLS_V1_0);

    }

    private void assertProtocolNOTAccepted(String protocol) throws Exception {
        testCheckIsAliveURL(protocol, true);
    }

    private void assertProtocolAccepted(String protocol) throws Exception {
        testCheckIsAliveURL(protocol, false);
    }

    private void testCheckIsAliveURL(String protocol, boolean expectProtocolNotAccepted) throws Exception {
        LOG.info("********** Start test for protocol:{}, expect to be accepted:{} *********", protocol, !expectProtocolNotAccepted);
        SSLContext sc = SSLContext.getInstance(protocol);

        TrustManager tm = createAcceptAllTrustManger();
        sc.init(null, new TrustManager[] { tm }, null);

        IntegrationTestContext context = IntegrationTestContext.get();

        String checkAlive = context.getUrlBuilder().buildCheckIsAliveUrl();
        URL url = new URL(checkAlive);

        URLConnection urlConnection = url.openConnection();
        HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;

        httpsConnection.setSSLSocketFactory(sc.getSocketFactory());
        /*
         * next fetch of conent is also necessary and we do also getotherwise we have a
         * "java.lang.IllegalStateException: connection not yet open"
         */
        print_content(httpsConnection);
        /*
         * next connect is important, otherwise we have a
         * "java.lang.IllegalStateException: connection not yet open"
         */
        httpsConnection.connect();

        fetchContentAndCheckForSSLHandshakeFailures(protocol, httpsConnection, expectProtocolNotAccepted);
    }

    private void print_content(HttpsURLConnection con) {
        if (con == null) {
            throw new IllegalArgumentException("con may not be null!");
        }

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String input;

            while ((input = br.readLine()) != null) {
                LOG.info(input);
            }
            br.close();

        } catch (IOException e) {
            /* just ignore */
            LOG.info("no content found - reason:{}", e.getMessage());
        }

    }

    private void fetchContentAndCheckForSSLHandshakeFailures(String protocol, HttpsURLConnection con, boolean expectSSLhandShakeFailure) {
        if (con == null) {
            throw new IllegalArgumentException("con may not be null!");
        }
        SSLHandshakeException handshakeException = null;
        try {

            LOG.info("Response Code : " + con.getResponseCode());
            LOG.info("Cipher Suite : " + con.getCipherSuite());

            Certificate[] certs = con.getServerCertificates();
            for (Certificate cert : certs) {
                LOG.info("Cert Type : " + cert.getType());
                LOG.info("Cert Hash Code : " + cert.hashCode());
                LOG.info("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
                LOG.info("Cert Public Key Format : " + cert.getPublicKey().getFormat());
            }

        } catch (SSLPeerUnverifiedException e) {
            throw new IllegalStateException("should not happen in test case");
        } catch (SSLHandshakeException e) {
            handshakeException = e;
        } catch (IOException e) {
            throw new IllegalStateException("should not happen in test case");
        }
        if (expectSSLhandShakeFailure) {
            if (handshakeException == null) {
                Assert.fail("Protocol " + protocol + " was accepted! There was no handshake exception !");
            }
        } else {
            if (handshakeException != null) {
                handshakeException.printStackTrace();
                Assert.fail("Protocol " + protocol + " was NOT accepted! There was a handshake exception:" + handshakeException.getMessage());
            }
        }

    }

    private TrustManager createAcceptAllTrustManger() {
        TrustManager tm = new X509TrustManager() {

            private X509Certificate[] emptyCertificatesArray = new X509Certificate[] {};

            public void /* NOSONAR */ checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                /* we do not check the client - we trust all */
            }

            public void /* NOSONAR */ checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                /* we do not check the server - we trust all */
            }

            public X509Certificate[] getAcceptedIssuers() {
                return emptyCertificatesArray;
            }
        };
        return tm;
    }

}
