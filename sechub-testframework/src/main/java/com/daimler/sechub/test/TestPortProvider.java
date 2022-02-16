// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is mainly for preventing race conditions at build servers<br>
 * E.g . wire mock tests binding same port on differnt builds... will break at
 * least one build<br>
 * <br>
 * We use System environment entries to provide different ports or when not set
 * use defaults.<br>
 * This class uses no spring boot env/property magic, so build scripts must
 * setup environment variables. When defining some environment parts (e.g.
 * SERVER_PORT) you will override some spring boot setup (e.g. "server.port") as
 * well. For SERVER_PORT this is wanted because integration test server relies
 * on this
 *
 * @author Albert Tregnaghi
 *
 */
public class TestPortProvider {

    private static final int DEFAULT_WIREMOCK_HTTPS_PORT = 8444;
    private static final int DEFAULT_WIREMOCK_HTTP_PORT = 8087;
    private static final int DEFAULT_INTEGRATIONTEST_SERVER_PORT = 8443;
    private static final int DEFAULT_INTEGRATIONTEST_PDS_PORT = 8444;
    private static final int DEFAULT_RESTDOC_HTTPS_PORT = 8081;
    private static final int DEFAULT_MVC_MOCK_HTTPS_PORT = 8081;

    private static final String ENV_SECHUB_TEST_WIREMOCK_HTTP_PORT = "SECHUB_TEST_WIREMOCK_HTTP_PORT";

    private static final String ENV_SECHUB_TEST_RESTDOC_HTTPS_PORT = "SECHUB_TEST_RESTDOC_HTTPS_PORT";
    private static final String ENV_SECHUB_TEST_WIREMOCK_HTTPS_PORT = "SECHUB_TEST_WIREMOCK_HTTPS_PORT";
    private static final String ENV_SECHUB_TEST_MVCMOCK_HTTPS_PORT = "SECHUB_TEST_MVCMOCK_HTTPS_PORT";
    private static final String ENV_SECHUB_TEST_S3MOCK_HTTP_PORT = "SECHUB_TEST_S3MOCK_HTTP_PORT";
    private static final String ENV_SECHUB_TEST_S3MOCK_HTTPS_PORT = "SECHUB_TEST_S3MOCK_HTTPS_PORT";

    private static final String ENV_SERVER_PORT = "SERVER_PORT"; // we reuse spring boot "server.port"
    private static final String ENV_PDS_PORT = "PDS_PORT";

    private static final int DEFAULT_S3MOCK_HTTP_PORT = 9090;
    private static final int DEFAULT_S3MOCK_HTTPS_PORT = 9190;

    private int wireMockHttpPort;
    private int wireMockHttpsPort;
    private int integrationTestServerPort;
    private EnvironmentEntryProvider envProvider = new DefaultEnvironmentEntryProvider();
    private int restDocPort;
    private int mvcMockPort;
    private int s3MockHttpPort;
    private int s3MockHttpsPort;
    private int integrationTestPDSPort;

    private static final Logger LOG = LoggerFactory.getLogger(TestPortProvider.class);

    public static final TestPortProvider DEFAULT_INSTANCE = new TestPortProvider();

    TestPortProvider() {
        wireMockHttpPort = getEnvOrDefault(ENV_SECHUB_TEST_WIREMOCK_HTTP_PORT, DEFAULT_WIREMOCK_HTTP_PORT);
        wireMockHttpsPort = getEnvOrDefault(ENV_SECHUB_TEST_WIREMOCK_HTTPS_PORT, DEFAULT_WIREMOCK_HTTPS_PORT);

        restDocPort = getEnvOrDefault(ENV_SECHUB_TEST_RESTDOC_HTTPS_PORT, DEFAULT_RESTDOC_HTTPS_PORT);
        mvcMockPort = getEnvOrDefault(ENV_SECHUB_TEST_MVCMOCK_HTTPS_PORT, DEFAULT_MVC_MOCK_HTTPS_PORT);

        integrationTestServerPort = getEnvOrDefault(ENV_SERVER_PORT, DEFAULT_INTEGRATIONTEST_SERVER_PORT);
        integrationTestPDSPort = getEnvOrDefault(ENV_PDS_PORT, DEFAULT_INTEGRATIONTEST_PDS_PORT);

        s3MockHttpPort = getEnvOrDefault(ENV_SECHUB_TEST_S3MOCK_HTTP_PORT, DEFAULT_S3MOCK_HTTP_PORT);
        s3MockHttpsPort = getEnvOrDefault(ENV_SECHUB_TEST_S3MOCK_HTTPS_PORT, DEFAULT_S3MOCK_HTTPS_PORT);
    }

    int getEnvOrDefault(String name, int defaultValue) {
        int value = convertToInt(getEnvProvider().getEnvEntry(name), defaultValue);
        if (value < 0) {
            return defaultValue;
        }
        return value;
    }

    int convertToInt(String intValueAsString, int defaultValue) {
        if (intValueAsString == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(intValueAsString);
        } catch (NumberFormatException e) {
            LOG.error("Was not able to convert to int:" + intValueAsString, e);
            return defaultValue;
        }

    }

    public final int getWireMockTestHTTPPort() {
        return wireMockHttpPort;
    }

    public final int getWireMockTestHTTPSPort() {
        return wireMockHttpsPort;
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

    void setEnvironmentEntryProvider(EnvironmentEntryProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider may not be null!");
        }
        this.envProvider = provider;
    }

    EnvironmentEntryProvider getEnvProvider() {
        return envProvider;
    }

    public int getS3MockServerHttpPort() {
        return s3MockHttpPort;
    }

    public int getS3MockServerHttpsPort() {
        return s3MockHttpsPort;
    }

}
