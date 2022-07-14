// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario16;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario16.Scenario16.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.mapping.NamePatternToIdEntry;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.pds.PDSMappingJobParameterData;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;

public class PDSUseSecHubCentralMappingInJobScenario16IntTest {

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

        PDSMappingJobParameterData expectedData1 = new PDSMappingJobParameterData();
        expectedData1.setMappingId(IntegrationTestExampleConstants.MAPPING_ID_1_REPLACE_ANY_PROJECT1);
        expectedData1.getEntries().add(new NamePatternToIdEntry(IntegrationTestExampleConstants.MAPPING_PATTERN_ANY_PROJECT1, IntegrationTestExampleConstants.MAPPING_REPLACEMENT_FOR_PROJECT1));
        String expectedMapping1Json = JSONConverter.get().toJSON(expectedData1);

        PDSMappingJobParameterData expectedData2 = new PDSMappingJobParameterData();
        expectedData2.setMappingId(IntegrationTestExampleConstants.MAPPING_ID_2_NOT_EXISTING_IN_SECHUB);
        String expectedMapping2Json = JSONConverter.get().toJSON(expectedData2,false);


        // check the script has the mappings injected :
        assertPDSJob(TestAPI.assertAndFetchPDSJobUUIDForSecHubJob(jobUUID)).
            containsVariableTestOutput(IntegrationTestExampleConstants.PDS_ENV_NAME_MAPPING_ID_1_REPLACE_ANY_PROJECT1,expectedMapping1Json).
            containsVariableTestOutput(IntegrationTestExampleConstants.PDS_ENV_NAME_MAPPING_ID_2_NOT_EXISTING_IN_SECHUB,expectedMapping2Json);
        /* @formatter:on */
    }

}
