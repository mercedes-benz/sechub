// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.variable.TestEnvironmentProvider;
import com.mercedesbenz.sechub.test.ManualTest;
import com.mercedesbenz.sechub.test.TestUtil;

/**
 * A local test for developers to check if GOSEC integration works. See
 * {@link ManualTest} for details about manual testing.
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestLocalGoSecManualTest implements ManualTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestLocalGoSecManualTest.class);

    private SystemTestAPI systemTestApi;

    @BeforeEach
    void beforeEach(TestInfo info) {
        systemTestApi = new SystemTestAPI();

        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("Systemtest: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    void manual_local_gosec_test_can_run_without_errors() throws IOException {
        /* @formatter:off */

        /* prepare */

        String variant = TestUtil.getSystemProperty("systemtest.variant","");
        String configFile = null;

        if ("integrationtest".equals(variant)) {
            configFile = "systemtest_gosec_example-with-sechub-integrationtest-server.json";
        }else if ("docker".equals(variant)) {
            configFile="systemtest_gosec_example.json";
        }else if ("docker-mock".equals(variant)) {
            configFile="systemtest_gosec_mock_example.json";
        }else {
            throw new RuntimeException("this variant is not supported:"+variant+". Please use 'docker, docker-mock or integrationtest'");
        }
        LOG.info("VARIANT: {}", variant);
        LOG.info("CONFIG : {}", configFile);

        TextFileReader reader = new TextFileReader();
        String json = reader.readTextFromFile(new File("./src/test/resources/"+configFile));
        SystemTestConfiguration config = JSONConverter.get().fromJSON(SystemTestConfiguration.class, json);

        TestEnvironmentProvider environmentProvider= TestConfigUtil.createEnvironmentProviderForSecrets() ;
        LOG.debug("PDS tech user id: {}", environmentProvider.getEnv(TestConfigConstants.ENV_TEST_PDS_USERID));
        LOG.debug("PDS tech user apitoken: {}", environmentProvider.getEnv(TestConfigConstants.ENV_TEST_PDS_APITOKEN));
        LOG.debug("SecHub admin user id: {}", environmentProvider.getEnv(TestConfigConstants.ENV_TEST_ADMIN_USERID));
        LOG.debug("SecHub admin user apitoken: {}", environmentProvider.getEnv(TestConfigConstants.ENV_TEST_ADMIN_APITOKEN));

        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                    pdsSolutionPath("./../sechub-pds-solutions").
                    workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/local_gosec_test").toString()).
                    additionalResourcesPath("./../").
                    testConfiguration(config).
                build(), environmentProvider);

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed?!?!");
        }
        /* @formatter:on */
    }

}
