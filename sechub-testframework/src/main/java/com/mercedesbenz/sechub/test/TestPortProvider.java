// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.io.IOException;
import java.net.ServerSocket;

import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is mainly for preventing race conditions at build servers<br>
 * E.g . wire mock tests binding same port on differnt builds... will break at
 * least one build<br>
 * <br>
 * We use System properties to provide different ports or when not set use
 * defaults.<br>
 *
 * @author Albert Tregnaghi
 *
 */
public class TestPortProvider {

    private static final int DEFAULT_INTEGRATIONTEST_SERVER_PORT = 8443;
    private static final int DEFAULT_INTEGRATIONTEST_PDS_PORT = 8444;

    private static final String PROPERTY_SECHUB_TEST_RESTDOC_HTTPS_PORT = "sechub.test.restdoc.https.port";
    private static final String PROPERTY_SECHUB_TEST_MVCMOCK_HTTPS_PORT = "sechub.test.mvcmock.https.port";

    // integration tests
    private static final String PROPERTY_SECHUB_INTEGRATIONTEST_SERVER_PORT = "sechub.integrationtest.serverport";
    private static final String PROPERTY_SECHUB_INTEGRATIONTEST_PDS_PORT = "sechub.integrationtest.pdsport";

    private SystemPropertyProvider systemPropertyProvider = new TestEnvironmentProvider();

    private int integrationTestServerPort;
    private int restDocPort;
    private int mvcMockPort;
    private int integrationTestPDSPort;

    private static final Logger LOG = LoggerFactory.getLogger(TestPortProvider.class);

    public static final TestPortProvider DEFAULT_INSTANCE = new TestPortProvider();

    TestPortProvider() {
        restDocPort = getSystemPropertyOrDefault(PROPERTY_SECHUB_TEST_RESTDOC_HTTPS_PORT, findFreePort());
        mvcMockPort = getSystemPropertyOrDefault(PROPERTY_SECHUB_TEST_MVCMOCK_HTTPS_PORT, findFreePort());

        integrationTestServerPort = getSystemPropertyOrDefault(PROPERTY_SECHUB_INTEGRATIONTEST_SERVER_PORT, DEFAULT_INTEGRATIONTEST_SERVER_PORT);
        integrationTestPDSPort = getSystemPropertyOrDefault(PROPERTY_SECHUB_INTEGRATIONTEST_PDS_PORT, DEFAULT_INTEGRATIONTEST_PDS_PORT);

        LOG.info("Test port provider created");
        LOG.info("Wiremock                https: {}, http: {}", "dynamic", "dynamic");
        LOG.info("Restdoc                 https: {}", restDocPort);
        LOG.info("MVCmock                 https: {}", mvcMockPort);
        LOG.info("Integration test server https: {}", integrationTestServerPort);
        LOG.info("Integration test PDS    https: {}", integrationTestPDSPort);

    }

    int getSystemPropertyOrDefault(String name, int defaultValue) {
        String systemProperty = getSystemPropertyProvider().getSystemProperty(name);
        int value = convertToInt(systemProperty, defaultValue);
        if (value < 0) {
            return defaultValue;
        }
        return value;
    }

    int convertToInt(String intValueAsString, int defaultValue) {
        if (intValueAsString == null) {
            return defaultValue;
        }
        if (intValueAsString.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(intValueAsString);
        } catch (NumberFormatException e) {
            LOG.error("Was not able to convert to int:" + intValueAsString, e);
            return defaultValue;
        }

    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new TestAbortedException("Failed to find a free port", e);
        }
    }

    public final int getWireMockTestHTTPPort() {
        return findFreePort();
    }

    public final int getWireMockTestHTTPSPort() {
        return findFreePort();
    }

    public final int getIntegrationTestServerPort() {
        return integrationTestServerPort;
    }

    public final int getIntegrationTestPDSPort() {
        return integrationTestPDSPort;
    }

    public final int getRestDocTestPort() {
        return restDocPort;
    }

    /**
     * Returns port used for MVC testing - this interesting for build servers not
     * running tests in separated environments - for example standard jenkins run on
     * same machine with n worker nodes. This avoids race conditions
     *
     * @return common port, used for mocked MVC tests.
     */
    public int getWebMVCTestHTTPSPort() {
        return mvcMockPort;
    }

    void setSystemPropertyProvider(SystemPropertyProvider systemPropertyProvider) {
        if (systemPropertyProvider == null) {
            throw new IllegalArgumentException("System property provider may not be null!");
        }
        this.systemPropertyProvider = systemPropertyProvider;
    }

    SystemPropertyProvider getSystemPropertyProvider() {
        return systemPropertyProvider;
    }

}
