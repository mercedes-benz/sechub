// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.TestConfigConstants.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;

import com.mercedesbenz.sechub.systemtest.runtime.variable.TestEnvironmentProvider;
import com.mercedesbenz.sechub.test.ManualTest;
import com.mercedesbenz.sechub.test.TestUtil;

public class TestConfigUtil {

    public static int getSecHubIntTestServerPort() {
        return TestUtil.getSystemPropertyIntOrDefault(SYSTEM_PROPERTY_SECHUB_INTTEST_PORT, DEFAULT_SECHUB_INTTEST_PORT);
    }

    public static int getPDSIntTestServerPort() {
        return TestUtil.getSystemPropertyIntOrDefault(SYSTEM_PROPERTY_PDS_INTTEST_PORT, DEFAULT_PDS_INTTEST_PORT);
    }

    /**
     * Converts to
     *
     * <pre>
     * "${secretEnv." + name + "}"
     * </pre>
     *
     * @param name
     * @return variable
     */
    public static String toSecretEnvVariable(String name) {
        return "${secretEnv." + name + "}";
    }

    /**
     * Converts to
     *
     * <pre>
     * "${env." + name + "}"
     * </pre>
     *
     * @param name
     * @return variable
     */
    public static String toEnvVariable(String name) {
        return "${env." + name + "}";
    }

    /**
     * This method creates a test environment provider which defines defaults for
     * environment variables defined in , but also provides the possibility to
     * override those values by dedicated system properties. <br>
     * <br>
     * Benefit: {@link ManualTest} implementations which shall be started by
     * developers on demand, can use those variables to customize behavior via
     * environment variables. <br>
     * <br>
     * Please look into the implementation for exact mapping details.
     *
     * @return test environment provider
     */
    public static TestEnvironmentProvider createEnvironmentProviderForSecrets() {
        // Setup environment. When not defined, use defaults

        /* @formatter:off */
        TestEnvironmentProvider environmentProvider = new TestEnvironmentProvider();
        environmentProvider.setEnv(ENV_TEST_PDS_SERVER, getSystemProperty(SYSTEM_PROPERTY_PDS_SERVER, DEFAULT_PDS_SERVER));
        environmentProvider.setEnv(ENV_TEST_SECHUB_SERVER, getSystemProperty(SYSTEM_PROPERTY_SECHUB_SERVER, DEFAULT_SECHUB_SERVER));

        environmentProvider.setEnv(ENV_TEST_PROJECT, getSystemProperty(SYSTEM_PROPERTY_TEST_PROJECT, DEFAULT_TEST_PROJECT));
        environmentProvider.setEnv(ENV_TEST_WEBSCAN_URL, getSystemProperty(SYSTEM_PROPERTY_TEST_WEBSCAN_URL, DEFAULT_TEST_WEB_URL));

        /* for tests with integration test server */
        environmentProvider.setEnv(ENV_TEST_INTTEST_ADMIN_USERID, getSystemProperty(SYSTEM_PROPERTY_SECHUB_INITIALADMIN_USERID, DEFAULT_INTTEST_ADMIN_USERID));
        environmentProvider.setEnv(ENV_TEST_INTTEST_ADMIN_APITOKEN, getSystemProperty(SYSTEM_PROPERTY_SECHUB_INITIALADMIN_APITOKEN, DEFAULT_INTTEST_ADMIN_APITOKEN));

        environmentProvider.setEnv(ENV_TEST_INTTEST_PDS_TECHUSER_USERID,getSystemProperty(SYSTEM_PROPERTY_PDS_TECHUSER_USERNAME, DEFAULT_INTTEST_PDS_TECHUSER_USERID));
        environmentProvider.setEnv(ENV_TEST_INTTEST_PDS_TECHUSER_APITOKEN, getSystemProperty(SYSTEM_PROPERTY_PDS_TECHUSER_APITOKEN, DEFAULT_INTTEST_PDS_TECHUSER_APITOKEN));

        environmentProvider.setEnv(ENV_TEST_INTTEST_PDS_ADMIN_USERID,getSystemProperty(SYSTEM_PROPERTY_PDS_ADMIN_USERNAME, DEFAULT_INTTEST_PDS_ADMIN_USERID));
        environmentProvider.setEnv(ENV_TEST_INTTEST_PDS_ADMIN_APITOKEN, getSystemProperty(SYSTEM_PROPERTY_PDS_ADMIN_APITOKEN, DEFAULT_INTTEST_PDS_ADMIN_APITOKEN));

        /* for other servers (local, remote but no integration test setup) */
        environmentProvider.setEnv(ENV_TEST_ADMIN_USERID, getSystemProperty(SYSTEM_PROPERTY_SECHUB_INITIALADMIN_USERID, DEFAULT_SECHUB_SOLUTION_ADMIN_USERID));
        environmentProvider.setEnv(ENV_TEST_ADMIN_APITOKEN, getSystemProperty(SYSTEM_PROPERTY_SECHUB_INITIALADMIN_APITOKEN, DEFAULT_SECHUB_SOLUTION_ADMIN_APITOKEN));

        environmentProvider.setEnv(ENV_TEST_USER_USERID, getSystemProperty(SYSTEM_PROPERTY_SECHUB_USER_USERID, DEFAULT_SECHUB_TESTUSER_USERID));
        environmentProvider.setEnv(ENV_TEST_USER_APITOKEN, getSystemProperty(SYSTEM_PROPERTY_SECHUB_USER_APITOKEN, DEFAULT_SECHUB_TESTUSER_APITOKEN));

        environmentProvider.setEnv(ENV_TEST_PDS_USERID, getSystemProperty(SYSTEM_PROPERTY_PDS_TECHUSER_USERNAME, DEFAULT_PDS_SOLUTION_TECHUSER_USERID));
        environmentProvider.setEnv(ENV_TEST_PDS_APITOKEN, getSystemProperty(SYSTEM_PROPERTY_PDS_TECHUSER_APITOKEN, DEFAULT_PDS_SOLUTION_TECHUSER_APITOKEN));
        /* @formatter:on */

        return environmentProvider;
    }

}
