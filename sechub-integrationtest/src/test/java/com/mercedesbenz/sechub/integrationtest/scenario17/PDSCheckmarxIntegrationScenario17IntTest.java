// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario17;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario17.Scenario17.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

public class PDSCheckmarxIntegrationScenario17IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario17.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    public void pds_calls_checkmarx_wrapper_which_uses_mocked_adapter_and_returns_yellow_checkmarx_result() {
        /* @formatter:off */
        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).
                createCodeScanWithTemplate(
                        IntegrationTestTemplateFile.CODE_SCAN_3_SOURCES_DATA_ONE_REFERENCE,
                        PROJECT_1,
                        NOT_MOCKED,
                        TemplateData.builder().
                            setVariable("__folder__",
                                    CODE_SCAN__CHECKMARX__MULTI__ZERO_WAIT.getMockDataIdentifier()).
                            addReferenceId("files-b").
                            build());

        /* execute */
        as(USER_1).
            uploadSourcecode(PROJECT_1, jobUUID, PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS).
            approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);
        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasTrafficLight(TrafficLight.YELLOW).
            hasFindings(109)
            ;
        /* @formatter:on */
    }

}
