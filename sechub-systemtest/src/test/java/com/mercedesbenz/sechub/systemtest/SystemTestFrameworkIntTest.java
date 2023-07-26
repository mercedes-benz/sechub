package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JsonMapperFactory;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.variable.TestEnvironmentProvider;
import com.mercedesbenz.sechub.test.TestUtil;

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
 * around times ( no repetitive server starts and stops necessary). The process
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
        LOG.info("System API tests: {}", info.getDisplayName());
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
                        admin("${secretEnv.TESTSECHUB_ADMIN_USER}","${secretEnv.TESTSECHUB_ADMIN_TOKEN}").
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
                        techUser("${secretEnv.TESTPDS_ADMIN_USER}","${secretEnv.TESTPDS_ADMIN_TOKEN}").
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

        JsonMapper mapper = JsonMapperFactory.createMapper();
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        String configurationAsPrettyPrintedJson = JSONConverter.get().toJSON(configuration,true, mapper);

        // store meta data for documentation (later)
        File generatedSecHubDocExampleFile = new File("./../sechub-doc/src/docs/asciidoc/documents/gen/examples/gen_example_systemtest_using_local_integrationtestservers.json");
        TextFileWriter writer = new TextFileWriter();
        writer.save(generatedSecHubDocExampleFile, configurationAsPrettyPrintedJson, true);
        LOG.info("Wrote configuration data as example doc file into: {}", generatedSecHubDocExampleFile.getAbsolutePath());

        TestEnvironmentProvider environmentProvider = createEnvironmentProviderForSecrets();

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
                        admin("${secretEnv.TESTSECHUB_ADMIN_USER}","${secretEnv.TESTSECHUB_ADMIN_TOKEN}").
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

                        techUser("${secretEnv.TESTPDS_ADMIN_USER}","${secretEnv.TESTPDS_ADMIN_TOKEN}").
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
                             * here we use simply the current path from caller side - this works as well
                             * and shall show that this way is also correct.
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

        TestEnvironmentProvider environmentProvider = createEnvironmentProviderForSecrets();

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

    private TestEnvironmentProvider createEnvironmentProviderForSecrets() {
        // Setup environment. When not defined, use defaults for local integration tests
        String testSecHubAdminUser = TestUtil.getSystemProperty("sechub.initialadmin.userid", "int-test_superadmin");
        String testSecHubAdminPwd = TestUtil.getSystemProperty("sechub.initialadmin.apitoken", "int-test_superadmin-pwd");
        String testPDSTechUserName = TestUtil.getSystemProperty("pds.techuser.username", "pds-inttest-techuser");
        String testPDSTechUserPwd = TestUtil.getSystemProperty("pds.techuser.apitoken", "pds-inttest-apitoken");

        TestEnvironmentProvider environmentProvider = new TestEnvironmentProvider();
        environmentProvider.setEnv("TESTSECHUB_ADMIN_USER", testSecHubAdminUser);
        environmentProvider.setEnv("TESTSECHUB_ADMIN_TOKEN", testSecHubAdminPwd);
        environmentProvider.setEnv("TESTPDS_ADMIN_USER", testPDSTechUserName);
        environmentProvider.setEnv("TESTPDS_ADMIN_TOKEN", testPDSTechUserPwd);
        return environmentProvider;
    }

}
