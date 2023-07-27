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

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.variable.TestEnvironmentProvider;
import com.mercedesbenz.sechub.test.TestUtil;

/**
 * An integration test which needs a running SecHub server
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestLocalGoSecManualTest {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestLocalGoSecManualTest.class);

    private SystemTestAPI systemTestApi;

    @BeforeEach
    void beforeEach(TestInfo info) {
        systemTestApi = new SystemTestAPI();

        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("System API tests: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    @EnabledIfSystemProperty(named = "sechub.manual.test.by.developer", matches = "true")
    void manual_local_gosec_test_can_run_without_errors() throws IOException {
        /* @formatter:off */

        /* prepare */

        String variant = TestUtil.getSystemProperty("systemtest.variant","");
        String configFile = null;

        if ("integrationtest".equals(variant)) {
            configFile = "systemtest_gosec_example-with-sechub-integrationtest-server.json";
        }else if ("docker".equals(variant)) {
            configFile="systemtest_gosec_example.json";
        }else {
            throw new RuntimeException("this variant is not supported:"+variant);
        }
        TextFileReader reader = new TextFileReader();
        String json = reader.loadTextFile(new File("./src/test/resources/"+configFile));
        SystemTestConfiguration config = JSONConverter.get().fromJSON(SystemTestConfiguration.class, json);

        TestEnvironmentProvider environmentProvider= createEnvironmentProviderForSecrets() ;
        LOG.info("Using variant: {} -> config file: {}", variant, configFile);

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

    private TestEnvironmentProvider createEnvironmentProviderForSecrets() {
        // Setup environment. When not defined, use defaults for local integration tests
        String testSecHubAdminUser = TestUtil.getSystemProperty("sechub.initialadmin.userid", "admin");
        String testSecHubAdminPwd = TestUtil.getSystemProperty("sechub.initialadmin.apitoken", "myTop$ecret!");
        String testPDSTechUserName = TestUtil.getSystemProperty("pds.techuser.username", "techuser");
        String testPDSTechUserPwd = TestUtil.getSystemProperty("pds.techuser.apitoken", "pds-apitoken");

        TestEnvironmentProvider environmentProvider = new TestEnvironmentProvider();
        environmentProvider.setEnv("ADMIN_USERID", testSecHubAdminUser);
        environmentProvider.setEnv("ADMIN_APITOKEN", testSecHubAdminPwd);

        environmentProvider.setEnv("PDS_USERID", testPDSTechUserName);
        environmentProvider.setEnv("PDS_APITOKEN", testPDSTechUserPwd);

        environmentProvider.setEnv("PDS_SERVER", "https://localhost:8444");
        environmentProvider.setEnv("SECHUB_SERVER", "https://localhost:8443");
        return environmentProvider;
    }

}
