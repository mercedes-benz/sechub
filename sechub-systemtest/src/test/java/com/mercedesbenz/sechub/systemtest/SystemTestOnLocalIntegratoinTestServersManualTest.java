package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;

/**
 * A special manual test for developers.
 *
 * How to use:
 *
 * <pre>
 * - start SecHub server in integration test mode from your IDE
 * - start PDS server in integration test mode from your IDE
 * - run this test wit dedicated system properties (see inside test method for details)
 * </pre>
 *
 * Purpose: Easier to test and develop system test framework: Less turn around
 * times ( no repetitive server starts and stops necessary)
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestOnLocalIntegratoinTestServersManualTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestOnLocalIntegratoinTestServersManualTest.class);

    @BeforeEach
    void beforeEach(TestInfo info) {
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("System API tests: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    @EnabledIfSystemProperty(named = "sechub.manual.test.by.developer", matches = "true")
    void manual_test_sechub_inttestserver_running() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                localSetup().
                    secHub().
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
                                /* credentials */
                                credentials("pds-inttest-techuser", "pds-inttest-apitoken").
                            endExecutor().
                        endConfigure().
                    endSecHub().
                    addSolution("PDS_INTTEST_PRODUCT_CODESCAN").
                        /*
                         * We do not define any steps here - developers must have started the
                         * integration test PDS server locally in IDE
                         *
                         * The next line is important: The path cannot be auto calculated because we use a
                         * SecHub server started by here - so we set the path */
                        pathToServerConfigFile(new File("./../sechub-integrationtest/src/main/resources/pds-config-integrationtest.json").toPath().toString()).
                    endSolution().
                endLocalSetup().

                test("test1").
                    prepareStep().
                        script().
                        endScript().
                    endStep().
                    runSecHubJob().
                        uploadBinaries("").
                    endRunSecHub().
                endTest().
                build();

        LOG.info("config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        SystemTestResult result = runSystemTests(
                params().
                    localRun().
                    workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/remote_run_test").toString()).
                    testConfiguration(configuration).
                build());

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed?!?!");
        }
        /* @formatter:on */
    }

}
