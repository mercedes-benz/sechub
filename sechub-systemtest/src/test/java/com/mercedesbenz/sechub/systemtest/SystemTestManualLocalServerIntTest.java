package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;

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
 * An integration test which needs a running SecHub server
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestManualLocalServerIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestManualLocalServerIntTest.class);

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
                        url(new URL("https://localhost:8443")).
                        admin("int-test_superadmin","int-test_superadmin-pwd"). // because an URL is defined, the framework will check that his server is alive!
                    endSecHub().
                endLocalSetup().
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
