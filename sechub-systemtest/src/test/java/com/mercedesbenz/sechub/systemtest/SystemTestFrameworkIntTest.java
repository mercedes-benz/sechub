package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;
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


        int secHubPort = TestConfigUtil.getSecHubIntTestServerPort();
        int pdsPort = TestConfigUtil.getPDSIntTestServerPort();

        /* prepare */
        SystemTestConfiguration configuration = configure().
                addVariable("testSourceUploadFolder", "${runtime."+RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName()+"}/testsources").

                localSetup().
                    secHub().
                        url(new URL("https://localhost:"+secHubPort)).
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
                        url(new URL("https://localhost:"+pdsPort)).
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
                            workingDir("./../sechub-systemtest/src/test/resources/fake-root/test/preparation").
                            path("./prepare-inttest-copy-codescan-medium-findings.sh").
                            arguments("${variables.testSourceUploadFolder}").
                        endScript().
                    endStep().
                    runSecHubJob().
                        codeScan().
                            use("reference1").
                        endScan().
                        uploads().
                            upload().
                                sources("${variables.testSourceUploadFolder}").
                                withReferenceId("reference1").
                            endUpload().
                        endUploads().
                    endRunSecHub().
                endTest().
                build();

        LOG.info("config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        try {
            System.out.println("------> start test");
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
        }catch(Throwable t) {
            System.err.println("----> failed with:"+t.getClass());
            t.printStackTrace();

            System.err.println("---- test if server is still alive:");
            try {
                URI serverUri = new URI("https://localhost:"+secHubPort);
                SecHubClient client = new SecHubClient(serverUri, "int-test_superadmin", "int-test_superadmin-pwd");
                boolean alive = client.checkIsServerAlive();
                System.out.println("server alive at "+serverUri+" : "+alive);
            } catch (Exception e) {
                System.out.println("Not able to check server alive!");
                e.printStackTrace();
            }
            throw t;
        }
        /* @formatter:on */
    }

}
