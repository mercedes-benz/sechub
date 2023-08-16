// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.systemtest.TestConfigConstants.*;
import static com.mercedesbenz.sechub.systemtest.TestConfigUtil.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.variable.TestEnvironmentProvider;
import com.mercedesbenz.sechub.test.ManualTest;

/**
 * An system test which needs a running SecHub server See {@link ManualTest} for
 * details about manual testing.
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestRemoteServerManualTest implements ManualTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRemoteServerManualTest.class);

    private SystemTestAPI systemTestApi;

    @BeforeEach
    void beforeEach(TestInfo info) {
        systemTestApi = new SystemTestAPI();

        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("Systemtest: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    void manual_remote_test_webscan_sechub_scan_possible_and_a_result_is_returned() throws IOException {
        /* @formatter:off */

        TestEnvironmentProvider envProvider = TestConfigUtil.createEnvironmentProviderForSecrets();

        /* prepare */
        SystemTestConfiguration configuration = configure().
                remoteSetup().
                    secHub().
                        url(new URL(envProvider.getEnv(ENV_TEST_SECHUB_SERVER))).
                        user(toEnvVariable(ENV_TEST_USER_USERID), toSecretEnvVariable(ENV_TEST_USER_APITOKEN)).
                    endSecHub().
                endRemoteSetup().
                test("test-remote1").
                    runSecHubJob().
                        project(toEnvVariable(ENV_TEST_PROJECT)).
                        webScan().
                            url(envProvider.getEnv(ENV_TEST_WEBSCAN_URL)).
                        endScan().
                    endRunSecHub().
                    asserts().
                        assertThat().
                          secHubResult().
                              containsStrings("jobUUID","trafficLight"). // just check the result contains some parts - exact content is not checked here, because situation dependent
                          endSecHubResult().
                        endAssert().
                    endAsserts().
                endTest().
                build();

        LOG.info("config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                    remoteRun().
                    workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/remote_run_test").toString()).
                    testConfiguration(configuration).
                build(), envProvider);

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed?!?!");
        }
        /* @formatter:on */
    }

}
