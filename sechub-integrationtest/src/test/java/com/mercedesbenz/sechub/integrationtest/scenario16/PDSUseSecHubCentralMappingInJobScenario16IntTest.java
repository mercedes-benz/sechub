// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario16;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario16.Scenario16.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.scenario22.PDSPrepareIntegrationScenario22IntTest;

public class PDSUseSecHubCentralMappingInJobScenario16IntTest {

    private static final String TRUE = "true";
    private static final String FORBIDDEN = "forbidden";
    private static final String ACCEPTED = "accepted";
    private static final String INTEGRATIONTEST_PDS_STARTED_BY_SCRIPT = "INTEGRATIONTEST_PDS_STARTED_BY_SCRIPT";
    private static final String INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED = "INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED";
    private static final String INTEGRATIONTEST_SCRIPT_ENV_FORBIDDEN = "INTEGRATIONTEST_SCRIPT_ENV_FORBIDDEN";

    private static final Logger LOG = LoggerFactory.getLogger(PDSPrepareIntegrationScenario22IntTest.class);

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario16.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    public void pds_script_does_have_the_mappings_from_sechub_injected_as_environment_variables() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(PROJECT_1,NOT_MOCKED);

        /* execute */
        as(USER_1).
            approveJob(project, jobUUID);


        /* test */
        waitForJobDone(project, jobUUID,30,true);

        MappingData expectedData1 = new MappingData();
        expectedData1.getEntries().add(new MappingEntry(
                    IntegrationTestExampleConstants.MAPPING_1_PATTERN_ANY_PROJECT1,
                    IntegrationTestExampleConstants.MAPPING_1_REPLACEMENT_FOR_PROJECT1,
                    IntegrationTestExampleConstants.MAPPING_1_COMMENT));
        String expectedMapping1Json = expectedData1.toJSON();

        String expectedMapping2Json = "{}";

        // check the script has the mappings injected :
        UUID pdsJobUUID = TestAPI.assertAndFetchPDSJobUUIDForSecHubJob(jobUUID);
        assertPDSJob(pdsJobUUID).
            containsVariableTestOutput(IntegrationTestExampleConstants.PDS_ENV_NAME_MAPPING_ID_1_REPLACE_ANY_PROJECT1, expectedMapping1Json).
            containsVariableTestOutput(IntegrationTestExampleConstants.PDS_ENV_NAME_MAPPING_ID_2_NOT_EXISTING_IN_SECHUB, expectedMapping2Json);

        // additional test: here we test that the script environment has only white listed parts from
        // parent process (the variables were defined and exported to PDS on startup by integrationtest-pds.sh)
        String pdsStartedByScriptValue = TestAPI.getPDSServerEnvironmentVariableValue(INTEGRATIONTEST_PDS_STARTED_BY_SCRIPT);
        if (TRUE.equals(pdsStartedByScriptValue)) {
            Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);

            // precondition check
            assertEquals(ACCEPTED, TestAPI.getPDSServerEnvironmentVariableValue(INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED));
            assertEquals(FORBIDDEN, TestAPI.getPDSServerEnvironmentVariableValue(INTEGRATIONTEST_SCRIPT_ENV_FORBIDDEN));

            assertEquals(ACCEPTED, variables.get(INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED)); // defined + white listed
            assertEquals("", variables.get(INTEGRATIONTEST_SCRIPT_ENV_FORBIDDEN));// was defined, but not white listed, means dump returns empty

            assertNotNull(variables.get("PATH")); // one of the default PDS white list entries for script environments

        }else {
            LOG.error("#".repeat(120));
            LOG.error("### ERROR - local PDS server cannot be tested without environment variables set!");
            LOG.error("#".repeat(120));
            LOG.error("The integration test usese a PDS server which is running not from script but locally (from an IDE).");
            LOG.error("Means the environment variables are not set on PDS startup process and cannot be tested!");
            LOG.error("The test will just skip the pds script cleanup test part in this case because otherwise always failing");
            LOG.error("");
            LOG.error("If you want to test this locally,you have to set the env variables on PDS start locally:");
            LOG.error(" {}={} ",INTEGRATIONTEST_PDS_STARTED_BY_SCRIPT, TRUE);
            LOG.error(" {}={} ",INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED, ACCEPTED);
            LOG.error(" {}={} ",INTEGRATIONTEST_SCRIPT_ENV_FORBIDDEN, FORBIDDEN);
            LOG.error("#".repeat(120));
        }


        /* @formatter:on */
    }

}
