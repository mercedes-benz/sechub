// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario17;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario17.Scenario17.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

public class PDSCheckmarxIntegrationScenario17IntTest {

    private static final String TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256 = "TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256";

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


        // check RECOMPRESSED ZIP file content
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);

        String sha256 = variables.get(TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256);

        if (sha256!=null) {
            int firstSpace = sha256.indexOf(" ");
            if (firstSpace!=-1) {
                sha256 = sha256.substring(0,firstSpace);
            }
        }
        String expectedSha256 = IntegrationTestExampleConstants.SHA256SUM_FOR_DATA_TXT_FILE_IN_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS;

        if (! Objects.equals(expectedSha256, sha256)) {
            String sha256VariableWithPath = variables.get(TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256);
            System.out.println("TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256="+sha256VariableWithPath);

            assertEquals(expectedSha256, sha256);
        }
        /* check pds debug enabled variable available - we have enabled it inside the executor configuration */
        assertEquals("true", variables.get("PDS_DEBUG_ENABLED"));
        /* @formatter:on */
    }

}
