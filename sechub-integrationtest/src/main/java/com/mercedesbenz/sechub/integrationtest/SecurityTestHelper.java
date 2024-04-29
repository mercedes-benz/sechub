// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestUtil;

public class SecurityTestHelper {

    private static final String SEND_CURL_REQUEST_SHELLSCRIPT = "send_curl_request.sh";
    private static final String CIPHERTEST_SHELLSCRIPT = "ciphertest.sh";

    public enum TestTargetType {
        PDS_SERVER("pds"),

        SECHUB_SERVER("server"),

        ;

        private String id;

        private TestTargetType(String id) {
            this.id = id;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestHelper.class);

    /* no longer accepted */
    public static final String SSL_V3 = "sslv3";
    public static final String TLS_V1_0 = "TLSv1";
    public static final String TLS_V1_1 = "TLSv1.1";

    /* accepted */
    public static final String TLS_V1_3 = "TLSv1.3";
    public static final String TLS_V1_2 = "TLSv1.2";

    private URL testURL;

    private TestTargetType targetType;

    private CipherTestData cipherTestData;

    public SecurityTestHelper(TestTargetType targetType, URL testURL) {
        this.testURL = testURL;
        this.targetType = targetType;
    }

    public void assertProtocolNOTAccepted(String protocol) throws Exception {
        SSLTestContext context = new SSLTestContext();
        context.protocol = protocol;
        context.expectProtocolNotAccepted = true;

        callTestURLWithProtocol(context);
    }

    public void assertProtocolAccepted(String protocol) throws Exception {
        SSLTestContext context = new SSLTestContext();
        context.protocol = protocol;
        context.expectProtocolNotAccepted = false;

        callTestURLWithProtocol(context);
    }

    String getMac(CipherCheck check) {
        String cipher = check.cipher;
        if (cipher == null) {
            return null;
        }
        int lastIndex = cipher.lastIndexOf("-");
        if (lastIndex == -1) {
            return null;
        }
        return cipher.substring(lastIndex + 1);
    }

    private class SSLTestContext {
        String protocol;
        boolean expectProtocolNotAccepted;
    }

    private void callTestURLWithProtocol(SSLTestContext context) throws Exception {
        LOG.info("********************************************************************************");
        LOG.info("** Start test for protocol:{}, expect to be accepted:{}", context.protocol, !context.expectProtocolNotAccepted);
        LOG.info("** TestURL: {}", testURL);
        LOG.info("********************************************************************************");
        SSLContext sslContext = SSLContext.getInstance(context.protocol);

        TrustManager tm = createAcceptAllTrustManger();
        sslContext.init(null, new TrustManager[] { tm }, null);

        URLConnection urlConnection = testURL.openConnection();
        HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        httpsConnection.setSSLSocketFactory(socketFactory);

        /*
         * next fetch of conent is also necessary and we do also get otherwise we have a
         * "java.lang.IllegalStateException: connection not yet open"
         */
        print_content(httpsConnection);

        fetchContentAndCheckForSSLHandshakeFailures(context, httpsConnection);
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

    private void fetchContentAndCheckForSSLHandshakeFailures(SSLTestContext context, HttpsURLConnection httpsConnection) {
        if (httpsConnection == null) {
            throw new IllegalArgumentException("con may not be null!");
        }
        SSLHandshakeException handshakeException = null;
        try {

            /*
             * next connect is important, otherwise we have a
             * "java.lang.IllegalStateException: connection not yet open"
             */
            httpsConnection.connect();

            LOG.info("Response Code : " + httpsConnection.getResponseCode());
            LOG.info("Cipher Suite : " + httpsConnection.getCipherSuite());

            Certificate[] certs = httpsConnection.getServerCertificates();
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
        } catch (ConnectException e) {
            fail("Was not able to connect to " + httpsConnection.getURL() + " - output was:" + e.getMessage());
        } catch (IOException e) {
            throw new IllegalStateException("should not happen in test case");
        }
        if (context.expectProtocolNotAccepted) {
            if (handshakeException == null) {
                fail("Protocol " + context.protocol + " was accepted! There was no handshake exception !");
            }
        } else {
            if (handshakeException != null) {
                handshakeException.printStackTrace();
                fail("Protocol " + context.protocol + " was NOT accepted! There was a handshake exception:" + handshakeException.getMessage());
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

    public void assertNotContainedMacsInCiphers(String... notAllowedMacs) throws Exception {
        ensureCipherTestDone();

        StringBuilder problems = new StringBuilder();

        for (CipherCheck check : cipherTestData.cipherChecks) {
            if ("true".equals(check.verified)) {
                String mac = getMac(check);

                if (mac == null) {
                    problems.append("Mac is null in test cipher - should never happen!\n  * " + check + "\n");
                }

                for (String notAllowedMac : notAllowedMacs) {

                    if (mac.equalsIgnoreCase(notAllowedMac)) {
                        problems.append("Not wanted mac: " + mac + " found inside verfified cipher: " + check.cipher + "\n");
                    }
                }
            }
        }
        if (!problems.isEmpty()) {
            fail("Problems found:\n" + problems.toString());
        }

    }

    public void assertOnlyAcceptedSSLCiphers(String... cipherNames) throws Exception {
        ensureCipherTestDone();

        for (String cipherName : cipherNames) {
            assertSSLCipher(cipherName, true);
        }

        int verified = 0;
        for (CipherCheck check : cipherTestData.cipherChecks) {
            if ("true".equals(check.verified)) {
                verified++;
            }
        }

        assertEquals("Amount of verified not as expected", cipherNames.length, verified);

    }

    public void assertSSLCipherNotAccepted(String cipherName) throws Exception {
        assertSSLCipher(cipherName, false);
    }

    public void assertSSLCipherAccepted(String cipherName) throws Exception {
        assertSSLCipher(cipherName, true);
    }

    private void assertSSLCipher(String cipherName, boolean cipherShallBeVerifiedWithTrue) throws Exception {
        ensureCipherTestDone();

        boolean found = false;
        for (CipherCheck check : cipherTestData.cipherChecks) {
            if (!cipherName.equals(check.cipher)) {
                continue;
            }
            found = true;
            if ("true".equalsIgnoreCase(check.verified)) {
                if (cipherShallBeVerifiedWithTrue) {
                    return;
                } else {
                    fail("Cipher:" + cipherName + " was accepted by " + targetType + ", but should not!");
                }
            } else if ("false".equalsIgnoreCase(check.verified)) {
                if (!cipherShallBeVerifiedWithTrue) {
                    return;
                } else {
                    fail("Cipher:" + cipherName + " was NOT accepted by " + targetType + ", but should!");
                }
            }
            throw new IllegalStateException("The expected cipher was found in cipher test data, but it was not possible to check verification :" + cipherName
                    + " was verified as " + check.verified);
        }
        if (!found) {
            throw new IllegalStateException("The expected cipher:" + cipherName + " \n was NOT found in cipher test data:" + cipherName + " at " + targetType
                    + "\n" + collectionOfSupportedCiphers());
        }
    }

    private String collectionOfSupportedCiphers() throws Exception {
        ensureCipherTestDone();

        StringBuilder sb = new StringBuilder();
        sb.append("Accepted ciphers from server:\n");
        for (CipherCheck check : cipherTestData.cipherChecks) {
            if ("true".equalsIgnoreCase(check.verified)) {
                sb.append(check.cipher);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void sendCurlRequest(String url, String customRequestmethod) throws Exception {
        List<String> commands = new ArrayList<>();
        commands.add("./" + SEND_CURL_REQUEST_SHELLSCRIPT);
        commands.add(url);
        commands.add(customRequestmethod);

        ProcessBuilder pb = new ProcessBuilder(commands);
        Process process = pb.start();
        boolean exited = process.waitFor(10, TimeUnit.SECONDS);
        if (!exited) {
            throw new IllegalStateException("Was not able to wait for " + SEND_CURL_REQUEST_SHELLSCRIPT + " result");
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IllegalStateException("Was not able to execute `" + SEND_CURL_REQUEST_SHELLSCRIPT + "`, exit code was:" + exitCode);
        }

    }

    private void ensureCipherTestDone() throws Exception {
        if (cipherTestData != null) {
            assertNoConnectionHasBeenRefused();
            return;
        }
        List<String> commands = new ArrayList<>();
        commands.add("./" + CIPHERTEST_SHELLSCRIPT);
        commands.add("localhost:" + testURL.getPort());
        commands.add(targetType.id);

        /*
         * now we call cipher test shell script with parameters - will create
         * /sechub-integrationtest/build/testresult/ciphertest/sechub-pds.json or
         * /sechub-integrationtest/build/testresult/ciphertest/sechub-server.json
         */

        ProcessBuilder pb = new ProcessBuilder(commands);
        Process process = pb.start();
        boolean exited = process.waitFor(10, TimeUnit.SECONDS);
        if (!exited) {
            throw new IllegalStateException("Was not able to wait for " + CIPHERTEST_SHELLSCRIPT + " result");
        }
        int exitCode = process.exitValue();
        if (exitCode == 3) {
            throw new IllegalStateException("No openssl installed at your machine - cannot test ciphers!");
        } else if (exitCode != 0) {
            String message = "`" + CIPHERTEST_SHELLSCRIPT + "` script call failed with unexpected exit code:" + exitCode;
            if (TestUtil.isWindows()) {
                /* @formatter:off */
                message += "\n"
                        + "HINT: You are using windows and this test needs bash + open ssl\n"
                        + "- bash executeable must be inside your PATH variable so it can be called!\n"
                        + "- you also need a openssl installation\n"
                        + "> Proposal: Install gitbash for windows";
                /* @formatter:on */
            }
            throw new IllegalStateException(message);
        }

        File file = new File("./build/test-results/ciphertest/sechub-" + targetType.id + ".json");
        String text = TestFileReader.loadTextFile(file);

        ObjectMapper mapper = JSONTestSupport.DEFAULT.createObjectMapper();
        cipherTestData = mapper.readValue(text.getBytes(), CipherTestData.class);

        assertNoConnectionHasBeenRefused();

        StringBuilder sb = new StringBuilder();
        sb.append("Accepted ciphers for " + targetType + " from " + testURL + "\n");
        for (CipherCheck check : cipherTestData.cipherChecks) {
            if (Boolean.valueOf(check.verified)) {
                sb.append("- " + check.cipher + "\n");
            }
        }
        LOG.info(sb.toString());

    }

    /* Sanity check - when no server is available we have only unknown results */
    private void assertNoConnectionHasBeenRefused() {
        boolean atLeastOneConnectionRefused = false;
        for (CipherCheck check : cipherTestData.cipherChecks) {
            if (check.error == null || check.error.isEmpty()) {
                continue;
            }
            if (check.error.toLowerCase().indexOf("connection refused") != -1) {
                atLeastOneConnectionRefused = true;
                break;

            }
        }
        if (atLeastOneConnectionRefused) {
            throw new IllegalStateException("At least one cipher tests has a 'connection refused' - seems " + targetType + " has not been started!");
        }
    }
}
