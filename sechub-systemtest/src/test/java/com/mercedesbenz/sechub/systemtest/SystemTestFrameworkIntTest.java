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

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;

/**
 * A special integration test
 *
 * How to use:
 *
 * <pre>
 * - start SecHub server in integration test mode from your IDE
 * - start PDS server in integration test mode from your IDE
 * - run this test wit dedicated system properties (see inside test method for details)
 * </pre>
 *
 * Purpose: An integration test for CI/CD - ensures system test framework works
 * with a started local SecHub environment.x It makes it also easier to test and
 * develop system test framework at developmenttime: Less turn around times ( no
 * repetitive server starts and stops necessary). The process start/stop
 * automation is tested in a {@link SystemTestDryRunTest} separately.
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestFrameworkIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestFrameworkIntTest.class);

    private static int SECHUB_PORT = TestConfigUtil.getSecHubIntTestServerPort();
    private static int PDS_PORT = TestConfigUtil.getPDSIntTestServerPort();

    @BeforeEach
    void beforeEach(TestInfo info) {
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("System API tests: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    @EnabledIfSystemProperty(named = "sechub.integrationtest.running", matches = "true")
    void even_integration_test_setup_can_be_tested__codescan() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                addVariable("testSourceUploadFolder", "${runtime."+RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName()+"}/testsources").

                localSetup().
                    secHub().
                        url(new URL("https://localhost:"+SECHUB_PORT)).
                        admin("int-test_superadmin","int-test_superadmin-pwd").
                        /*
                         * We do not define any steps here - developers must have started the
                         * integration test SecHub server locally in IDE
                         */
                        configure().
                            addExecutor().
                                pdsProductId("PDS_INTTEST_PRODUCT_CODESCAN").
                                /* add mandatory parameters for this product:*/
                                parameter("product1.qualititycheck.enabled","true").
                                parameter("product1.level","A").
                            endExecutor().
                        endConfigure().
                    endSecHub().

                    addSolution("PDS_INTTEST_PRODUCT_CODESCAN").
                        url(new URL("https://localhost:"+PDS_PORT)).
                        techUser("pds-inttest-techuser", "pds-inttest-apitoken").
                        /*
                         * We do not define any steps here - the PDS and SecHub instances
                         * must be started already.
                         *
                         * The next line is important: The path cannot be auto calculated because we use a
                         * SecHub server started by here - so we set the path */
                        pathToServerConfigFile(new File("./../sechub-integrationtest/src/main/resources/pds-config-integrationtest.json").toPath().toString()).
                    endSolution().
                endLocalSetup().

                test("test1").
                    prepareStep().
                        script().
                            workingDir("./../sechub-systemtest/src/test/resources/fake-root/test/preparation").
                            path("./prepare-inttest-copy-codescan-medium-findings.sh").
                            arguments("${variables.testSourceUploadFolder}").
                        endScript().
                    endStep().
                    runSecHubJob().
                        codeScan().
                        endScan().
                        uploads().
                            upload().
                                sourceFolder("${variables.testSourceUploadFolder}").
                            endUpload().
                        endUploads().
                    endRunSecHub().
                endTest().
                build();

        LOG.info("config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        SystemTestResult result = runSystemTests(
                params().
                    localRun().
                    workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest_run").toString()).
                    testConfiguration(configuration).
                build());

        /* test */
        if (result.hasFailedTests()) {
            fail(result.toString());
        }
        /* @formatter:on */
    }

}