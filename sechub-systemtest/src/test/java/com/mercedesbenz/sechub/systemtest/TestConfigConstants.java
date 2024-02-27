// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

public class TestConfigConstants {

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Defaults........................ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public static final int DEFAULT_SECHUB_INTTEST_PORT = 8443;
    public static final int DEFAULT_PDS_INTTEST_PORT = 8444;
    public static final String DEFAULT_TEST_PROJECT = "test-project";
    public static final String DEFAULT_TEST_WEB_URL = "https://example.com";
    public static final String DEFAULT_SECHUB_SERVER = "https://localhost:8443";
    public static final String DEFAULT_PDS_SERVER = "https://pds-gosec:8444";

    /* default test credentials - for fallback on local testing only */
    public static final String DEFAULT_PDS_SOLUTION_TECHUSER_APITOKEN = "pds-apitoken";
    public static final String DEFAULT_PDS_SOLUTION_TECHUSER_USERID = "techuser";
    public static final String DEFAULT_SECHUB_TESTUSER_APITOKEN = "testuser-apitoken";
    public static final String DEFAULT_SECHUB_TESTUSER_USERID = "testuser";
    public static final String DEFAULT_SECHUB_SOLUTION_ADMIN_APITOKEN = "myTop$ecret!";
    public static final String DEFAULT_SECHUB_SOLUTION_ADMIN_USERID = "admin";

    public static final String DEFAULT_INTTEST_PDS_TECHUSER_USERID = "pds-inttest-techuser";
    public static final String DEFAULT_INTTEST_PDS_TECHUSER_APITOKEN = "pds-inttest-apitoken";

    public static final String DEFAULT_INTTEST_PDS_ADMIN_USERID = "pds-inttest-admin";
    public static final String DEFAULT_INTTEST_PDS_ADMIN_APITOKEN = "pds-inttest-apitoken";

    public static final String DEFAULT_INTTEST_ADMIN_APITOKEN = "int-test_superadmin-pwd";
    public static final String DEFAULT_INTTEST_ADMIN_USERID = "int-test_superadmin";

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................System properties............... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    public static final String SYSTEM_PROPERTY_SECHUB_INTTEST_PORT = "sechub.integrationtest.serverport";
    public static final String SYSTEM_PROPERTY_PDS_INTTEST_PORT = "sechub.integrationtest.pdsport";

    public static final String SYSTEM_PROPERTY_PDS_TECHUSER_APITOKEN = "pds.techuser.apitoken";
    public static final String SYSTEM_PROPERTY_PDS_TECHUSER_USERNAME = "pds.techuser.username";

    public static final String SYSTEM_PROPERTY_PDS_ADMIN_APITOKEN = "pds.admin.apitoken";
    public static final String SYSTEM_PROPERTY_PDS_ADMIN_USERNAME = "pds.admin.username";

    public static final String SYSTEM_PROPERTY_SECHUB_USER_APITOKEN = "sechub.user.apitoken";
    public static final String SYSTEM_PROPERTY_SECHUB_USER_USERID = "sechub.user.userid";
    public static final String SYSTEM_PROPERTY_SECHUB_INITIALADMIN_APITOKEN = "sechub.initialadmin.apitoken";
    public static final String SYSTEM_PROPERTY_SECHUB_INITIALADMIN_USERID = "sechub.initialadmin.userid";

    public static final String SYSTEM_PROPERTY_TEST_PROJECT = "test.project";
    public static final String SYSTEM_PROPERTY_TEST_WEBSCAN_URL = "test.webscan.url";

    public static final String SYSTEM_PROPERTY_SECHUB_SERVER = "test.sechub.server";
    public static final String SYSTEM_PROPERTY_PDS_SERVER = "test.pds.server";

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Environment variables........... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /**
     * Per default
     * {@value TestConfigConstants#DEFAULT_PDS_SOLUTION_TECHUSER_APITOKEN} will be
     * used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_TECHUSER_APITOKEN}. <br>
     * <br>
     *
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_PDS_APITOKEN = "TEST_PDS_APITOKEN";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_PDS_SOLUTION_TECHUSER_USERID}
     * will be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_TECHUSER_USERNAME}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_PDS_USERID = "TEST_PDS_USERID";

    /**
     * Per default
     * {@value TestConfigConstants#DEFAULT_SECHUB_SOLUTION_ADMIN_APITOKEN} will be
     * used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_INITIALADMIN_APITOKEN}.
     * <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_ADMIN_APITOKEN = "TEST_ADMIN_APITOKEN";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_SECHUB_SOLUTION_ADMIN_USERID}
     * will be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_INITIALADMIN_USERID}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_ADMIN_USERID = "TEST_ADMIN_USERID";

    /**
     * Per default
     * {@value TestConfigConstants#DEFAULT_INTTEST_PDS_TECHUSER_APITOKEN} will be
     * used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_TECHUSER_APITOKEN}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_INTTEST_PDS_TECHUSER_APITOKEN = "TEST_INTTEST_PDS_TECHUSER_APITOKEN";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_INTTEST_PDS_TECHUSER_USERID}
     * will be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_TECHUSER_USERNAME}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_INTTEST_PDS_TECHUSER_USERID = "TEST_INTTEST_PDS_TECHUSER_USERID";

    /**
     * Per default
     * {@value TestConfigConstants#DEFAULT_INTTEST_PDS_TECHUSER_APITOKEN} will be
     * used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_TECHUSER_APITOKEN}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_INTTEST_PDS_ADMIN_APITOKEN = "TEST_INTTEST_PDS_ADMIN_APITOKEN";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_INTTEST_PDS_TECHUSER_USERID}
     * will be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_TECHUSER_USERNAME}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_INTTEST_PDS_ADMIN_USERID = "TEST_INTTEST_PDS_ADMIN_USERID";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_INTTEST_ADMIN_APITOKEN} will
     * be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_INITIALADMIN_APITOKEN}.
     * <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_INTTEST_ADMIN_APITOKEN = "TEST_INTTEST_ADMIN_APITOKEN";

    /**
     *
     * Per default {@value TestConfigConstants#DEFAULT_INTTEST_ADMIN_USERID} will be
     * used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_INITIALADMIN_USERID}. <br>
     * <br>
     *
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_INTTEST_ADMIN_USERID = "TEST_INTTEST_ADMIN_USERID";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_SECHUB_SERVER} will be used.
     * You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_SERVER}. <br>
     * <br>
     *
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_SECHUB_SERVER = "TEST_SECHUB_SERVER";
    /**
     * Per default {@value TestConfigConstants#DEFAULT_PDS_SERVER} will be used. You
     * can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_PDS_SERVER}. <br>
     * <br>
     *
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_PDS_SERVER = "TEST_PDS_SERVER";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_SECHUB_TESTUSER_USERID} will
     * be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_USER_USERID}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_USER_USERID = "TEST_USER_USERID";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_SECHUB_TESTUSER_APITOKEN}
     * will be used. You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_SECHUB_USER_APITOKEN}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_USER_APITOKEN = "TEST_USER_APITOKEN";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_TEST_PROJECT} will be used.
     * You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_TEST_PROJECT}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_PROJECT = "TEST_PROJECT";

    /**
     * Per default {@value TestConfigConstants#DEFAULT_TEST_WEB_URL} will be used.
     * You can change via system property:
     * {@value TestConfigConstants#SYSTEM_PROPERTY_TEST_WEBSCAN_URL}. <br>
     * <br>
     * Please look into
     * {@linkplain TestConfigUtil#createEnvironmentProviderForSecrets() } for
     * mapping details
     */
    public static final String ENV_TEST_WEBSCAN_URL = "TEST_WEBSCAN_URL";

}
