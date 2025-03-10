// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.test.ManualTest;
import com.mercedesbenz.sechub.test.TestConstants;

class ZapWrapperLoginScriptManualTest implements ManualTest {

    private static final String DEFAULT_PARAMETERS_PROPERTIES_PATH = "./src/test/resources/manual-test/example-parameters.properties";

    private static final Logger logger = LoggerFactory.getLogger(ZapWrapperLoginScriptManualTest.class);

    private static final String SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE = "loginscript.manualtest.configfile";

    /**
     *
     * <h3>How to use the test</h3>
     *
     * You have to set following system properties:
     * <ul>
     * {@value TestConstants#MANUAL_TEST_BY_DEVELOPER} = true</li>
     * <li>{@value ZapWrapperLoginScriptManualTest#SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE}
     * = path to parameters file (see below for format). If not set, a fallback
     * default path {@value #DEFAULT_PARAMETERS_PROPERTIES_PATH} will be used. The
     * example configuration is a good point to start, every parameter is explained
     * there. Please copy the example file to a custom local location, configure it
     * and use it by setting the system property accordingly when executing the
     * test.</li>
     * </ul>
     * Start as normal junit5 test inside your IDE. It is not necessary to have a
     * running ZAP instance- the test works standalone!
     *
     * <h3>Details</h2>
     *
     * This test will use {@link LoginScriptAssertionSupport} and a given
     * configuration file. For properties format and settings look into
     * {@link LoginScriptAssertionSupport#assertConfiguredLoginScriptCanLogin(String)}
     *
     * @throws Exception
     */
    @Test
    void manual_start_login_script_with_login_success() throws Exception {

        /* ------- */
        /* prepare */
        /* ------- */
        String testConfigurationFilePath = System.getProperty(SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE);
        if (testConfigurationFilePath == null) {
            logger.info("************************");
            logger.info("****    ATTENTION   ****");
            logger.info("************************");
            logger.info("No test configuration file defined. Will use fallback default. \nTo use your own configuration start test with jvm parameter: -D"
                    + SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE + "=your-location/your-configuration.properties");
            logger.info("************************");
            testConfigurationFilePath = DEFAULT_PARAMETERS_PROPERTIES_PATH;
        }

        logger.info("Manual login script test started");
        logger.info("--------------------------------");
        logger.info("> using configuration file from:'{}'", testConfigurationFilePath);

        if (testConfigurationFilePath == null || testConfigurationFilePath.isEmpty()) {
            throw new IllegalArgumentException("The path to the properties file for script execution is not defined!\nPlease set the path by jvm parameter: -D"
                    + SYSTEM_PROPERTY_LOGINSCRIPT_MANUALTEST_CONFIGFILE + "=/location-path/your-parameters.properties");
        }

        String testUserMessagesFolder = ZapWrapperManualTestUtil.getUserMessagesFolder().getAbsolutePath();
        String testEventsFolder = ZapWrapperManualTestUtil.getEventsFolder().getAbsolutePath();
        LoginScriptAssertionSupport assertionSupport = new LoginScriptAssertionSupport(testUserMessagesFolder, testEventsFolder, true);
        assertionSupport.assertConfiguredLoginScriptCanLogin(testConfigurationFilePath);

    }

}
