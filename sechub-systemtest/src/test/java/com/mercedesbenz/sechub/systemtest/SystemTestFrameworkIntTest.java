// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.systemtest.TestSystemExampleWriter.*;
import static com.mercedesbenz.sechub.systemtest.TestConfigConstants.*;
import static com.mercedesbenz.sechub.systemtest.TestConfigUtil.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.variable.TestEnvironmentProvider;

/**
 * A special integration test
 *
 * How to use:
 *
 * <pre>
 * - start SecHub server in integration test mode from your IDE
 * - start PDS server in integration test mode from your IDE
 * - run this test with dedicated system properties (see inside test method for details)
 * </pre>
 *
 * Purpose: An integration test for CI/CD - ensures system test framework works
 * with a started local SecHub (integration test) environment. It makes it also
 * easier to test and develop system test framework for development: Less turn
 * around times (no repetitive server starts and stops necessary). The process
 * start/stop automation is tested in a {@link SystemTestDryRunTest} separately.
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestFrameworkIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestFrameworkIntTest.class);

    private static int SECHUB_PORT = TestConfigUtil.getSecHubIntTestServerPort();
    private static int PDS_PORT = TestConfigUtil.getPDSIntTestServerPort();

    private SystemTestAPI systemTestApi;

    @BeforeEach
    void beforeEach(TestInfo info) {
        systemTestApi = new SystemTestAPI();

        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("Systemtest: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    @EnabledIfSystemProperty(named = "sechub.integrationtest.running", matches = "true")
    void even_integration_test_setup_can_be_tested_codescan_source_only_and_gen_example() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                addVariable("testSourceUploadFolder", "${runtime."+RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName()+"}/testsources").

                localSetup().
                    secHub().
                        comment("We do not define start/stop here, because reuse running local SecHub server").
                        url(new URL("https://localhost:"+SECHUB_PORT)).
                        admin(toEnvVariable(ENV_TEST_INTTEST_ADMIN_USERID),toSecretEnvVariable(ENV_TEST_INTTEST_ADMIN_APITOKEN)).
                        project().
                            addURItoWhiteList("https://example.org/testproject1").
                        endProject().
                        /*
                         * We do not define any steps here - developers must have started the
                         * integration test SecHub server locally in IDE
                         */
                        configure().
                            addExecutor().
                                comment("This executor will be re-created by the framework").
                                name("system-test-codescan-exec1").
                                pdsProductId("PDS_INTTEST_PRODUCT_CODESCAN").
                                /* add mandatory parameters for this product:*/
                                parameter("product1.qualititycheck.enabled","true").
                                parameter("product1.level","A").
                                /* next parameter only necessary, because we are in integration test mode but we want to have real PDS server response */
                                parameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_MOCKING_DISABLED, "true").
                            endExecutor().
                        endConfigure().
                    endSecHub().

                    addSolution("PDS_INTTEST_PRODUCT_CODESCAN").
                        url(new URL("https://localhost:"+PDS_PORT)).
                        comment("We do not define start/stop here, because reuse running local PDS server").
                        techUser(toEnvVariable(ENV_TEST_INTTEST_PDS_TECHUSER_USERID), toSecretEnvVariable(ENV_TEST_INTTEST_PDS_TECHUSER_APITOKEN)).
                        adminUser(toEnvVariable(ENV_TEST_INTTEST_PDS_ADMIN_USERID), toSecretEnvVariable(ENV_TEST_INTTEST_PDS_ADMIN_APITOKEN)).
                        /*
                         * We do not define any steps here - the PDS and SecHub instances
                         * must be started already.
                         *
                         * The next line is important: The path cannot be auto calculated because we use an integration test
                         * SecHub server which has been already started - so we set the path */
                        pathToServerConfigFile(new File("./../sechub-integrationtest/src/main/resources/pds-config-integrationtest.json").toPath().toString()).
                    endSolution().
                endLocalSetup().

                test("test1").
                    prepareStep().
                        script().
                        workingDir("./src/test/resources/additional-resources/preparation").
                        path("./prepare-inttest-copy-codescan-medium-findings.sh").
                            arguments("${variables.testSourceUploadFolder}").
                        endScript().
                    endStep().
                    runSecHubJob().
                        codeScan().
                        endScan().
                        uploads().
                            addSourceUploadWithDefaultRef("${variables.testSourceUploadFolder}").
                        endUploads().
                    endRunSecHub().

                    asserts().
                        assertThat().
                            secHubResult().
                                hasTrafficLight(TrafficLight.YELLOW).
                                equalsFile("./src/test/resources/additional-resources/expected-output/sechub-result1.json").
                                containsStrings("MEDIUM","ERROR","WARNING","INFO","SUCCESS","integration test code only!").// not necessary, because already file check, but good to show possibility inside this test
                            endSecHubResult().
                        endAssert().
                    endAsserts().
                endTest().
                build();

        writeExample(configuration,"gen_example_systemtest_using_local_integrationtestservers.json");

        TestEnvironmentProvider environmentProvider = TestConfigUtil.createEnvironmentProviderForSecrets();

        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                    localRun().
                    workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest_codescan_source").toString()).
                    testConfiguration(configuration).
                build(), environmentProvider);

        /* test */
        if (result.hasFailedTests()) {
            fail(result.toString());
        }
        /* @formatter:on */
    }

    @Test
    @EnabledIfSystemProperty(named = "sechub.integrationtest.running", matches = "true")
    void even_integration_test_setup_can_be_tested__codescan_with_binary_and_sources() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                addVariable("testSourceUploadFolder", "${runtime."+RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName()+"}/testsources").

                localSetup().
                    secHub().
                        url(new URL("https://localhost:"+SECHUB_PORT)).
                        admin(toEnvVariable(ENV_TEST_INTTEST_ADMIN_USERID),toSecretEnvVariable(ENV_TEST_INTTEST_ADMIN_APITOKEN)).
                        /*
                         * We do not define any steps here - developers must have started the
                         * integration test SecHub server locally in IDE
                         */
                        configure().
                            addExecutor().
                                name("system-test-codescan-exec1").
                                pdsProductId("PDS_INTTEST_PRODUCT_CODESCAN").
                                /* add mandatory parameters for this product:*/
                                parameter("product1.qualititycheck.enabled","true").
                                parameter("product1.level","A").
                                /* next parameter only necessary, because we are in integration test mode but we want to have real PDS server response */
                                parameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_MOCKING_DISABLED, "true").
                            endExecutor().
                        endConfigure().
                    endSecHub().

                    addSolution("PDS_INTTEST_PRODUCT_CODESCAN").
                        url(new URL("https://localhost:"+PDS_PORT)).

                        techUser(toEnvVariable(ENV_TEST_INTTEST_PDS_TECHUSER_USERID),toSecretEnvVariable(ENV_TEST_INTTEST_PDS_TECHUSER_APITOKEN)).
                        adminUser(toEnvVariable(ENV_TEST_INTTEST_PDS_ADMIN_USERID), toSecretEnvVariable(ENV_TEST_INTTEST_PDS_ADMIN_APITOKEN)).

                        /*
                         * We do not define any steps here - the PDS and SecHub instances
                         * must be started already.
                         *
                         * The next line is important: The path cannot be auto calculated because we use an integration test
                         * SecHub server which has been already started - so we set the path */
                        pathToServerConfigFile(new File("./../sechub-integrationtest/src/main/resources/pds-config-integrationtest.json").toPath().toString()).
                    endSolution().
                endLocalSetup().

                test("test1").
                    prepareStep().
                        script().
                            /* we could have used the optional runtime.additionalResourcesFolder but
                             * here we use simply the current path from caller side. It just shows that this way also works well.
                             */
                            workingDir("./../sechub-systemtest/src/test/resources/additional-resources/preparation").
                            path("./prepare-inttest-copy-codescan-medium-findings.sh").
                            arguments("${variables.testSourceUploadFolder}").
                        endScript().
                    endStep().
                    runSecHubJob().
                        codeScan().
                        endScan().
                        uploads().
                            addSourceUploadWithDefaultRef("${variables.testSourceUploadFolder}").
                            addBinaryUpload("i-am-usesless-for-the-systemtest-but-i-test-that-a-binary-upload-works","${variables.testSourceUploadFolder}").
                        endUploads().
                    endRunSecHub().

                    asserts().
                        assertThat().
                            secHubResult().
                                hasTrafficLight(TrafficLight.YELLOW).
                                equalsFile("./../sechub-systemtest/src/test/resources/additional-resources/expected-output/sechub-result1.json").
                                containsStrings("MEDIUM","ERROR","WARNING","INFO","SUCCESS","integration test code only!").// not necessary, because already file check, but good to show possibility inside this test
                            endSecHubResult().
                        endAssert().
                    endAsserts().
                endTest().
                build();

        TestEnvironmentProvider environmentProvider = TestConfigUtil.createEnvironmentProviderForSecrets();

        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                    localRun().
                    workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest_codescan_source_bin").toString()).
                    testConfiguration(configuration).
                build(), environmentProvider);

        /* test */
        if (result.hasFailedTests()) {
            fail(result.toString());
        }
        /* @formatter:on */
    }

}
