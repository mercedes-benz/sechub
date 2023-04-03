package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRunResult;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestError;

/**
 * An integration test if the system test api and the involved runtime +
 * configuration builder can work together and execute real (but simple fake)
 * bash scripts.
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestIntegrationTest.class);

    @BeforeEach
    void beforeEach(TestInfo info) {
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("System API tests: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    void the_faked_pds_solution_for_gosec_can_be_executed_without_errors() {
        /* @formatter:off */
        /* prepare */
        SystemTestConfiguration configuration = configure().

                addVariable("number_list","2").
                addVariable("test","3").

                localSetup().
                    secHub().
                        addStartStep().
                            script().
                                path("./01-start-single-docker-compose.sh").
                            endScript().
                        endStep().

                        addStopStep().
                            script().
                                path("./01-stop-single-docker-compose.sh").
                            endScript().
                        endStep().

                        configure().
                            addExecutor().
                                pdsProductId("PDS_GOSEC").
                            endExecutor().
                        endConfigure().

                    endSecHub().

                    addSolution("faked-gosec").
                        addStartStep().
                            script().
                                path("./05-start-single-sechub-network-docker-compose.sh").
                                envVariable("A_TEST1", "value1").
                                envVariable("B_TEST2", "value2").
                            endScript().
                        endStep().
                        addStopStep().
                            script().
                                path("./05-stop-single-sechub-network-docker-compose.sh").
                                workingDir("./").
                            endScript().
                        endStep().

                    endSolution().

                endLocalSetup().

                build();

        LOG.info("config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* ------------- */

        /* execute */
        SystemTestResult results = runSystemTests(configuration, "./src/test/resources/fake-root/sechub-pds-solutions");

        /* test */
        SystemTestRunResult result1 = results.getRuns().iterator().next();
        assertEquals("default",result1.getRunIdentifier());

        if (result1.hasError()) {
            fail("The execution failed?!?! Result was:\n"+result1.getError());
        }

        /* @formatter:on */
    }

    @Test
    void the_faked_pds_solution_for_fail1_can_not_be_executed_and_contains_error_for_line2_in_startscript() {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                localSetup().
                    addSolution("faked-fail_on_start").
                        addStartStep().script().path("./05-start-single-sechub-network-docker-compose.sh").endScript().endStep().
                        addStopStep().script().path("./05-stop-single-sechub-network-docker-compose.sh").endScript().endStep().
                    endSolution().
                endLocalSetup().
                build();

        LOG.info("loaded config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* ------------- */

        /* execute */
        SystemTestResult result = runSystemTests(configuration, "./src/test/resources/fake-root/sechub-pds-solutions");

        /* test */
        LOG.info("altered config=\n{}", JSONConverter.get().toJSON(configuration,true));

        SystemTestRunResult runResult1 = result.getRuns().iterator().next();
        assertEquals("alpine",runResult1.getRunIdentifier());

        assertTrue(runResult1.hasError());
        SystemTestError error = runResult1.getError();
        if (!error.getDetails().contains("This shall be the last fail message")) {
            fail("Error did not contain the expected part:\n"+error);
        }



        /* @formatter:on */
    }

}
