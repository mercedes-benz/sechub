// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario17;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario17.Scenario17.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

public class PDSCheckmarxIntegrationScenario17IntTest {

    private static final String TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256 = "TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario17.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_only_which_is_supported_by_checkmarx_PDS_setup_results_yellow() {
        /* @formatter:off */
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_1);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_only_accepted_is_binary_and_source_via_job_parameter_results_yellow() {
        /* @formatter:off */
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_2);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_which_would_be_accepted_but_everything_is_filtered_results_in_job_done_without_result() {
        /* @formatter:off */
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_3);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_but_uploads_binary_only__which_is_not_supported_by_checkmarx_PDS_setup_results_in_job_done_without_result() {
        testCheckmarxPDSjobWithBinaryContentUploaded(PROJECT_1);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_but_uploads_binary_only__overriden_with_binary_accepted_results_in_failing_job() {
        testCheckmarxPDSjobWithBinaryContentUploaded(PROJECT_2);
    }

    private void testCheckmarxPDSJobWithSourceContentUploaded(TestProject project) {
        /* prepare */
        UUID jobUUID = as(USER_1).
                createCodeScanWithTemplate(
                        IntegrationTestTemplateFile.CODE_SCAN_3_SOURCES_DATA_ONE_REFERENCE,
                        project,
                        NOT_MOCKED,
                        TemplateData.builder().
                            setVariable("__folder__",
                                    CODE_SCAN__CHECKMARX__MULTI__ZERO_WAIT.getMockDataIdentifier()).
                            addReferenceId("files-b").
                            build());

        /* execute */
        as(USER_1).
            uploadSourcecode(project, jobUUID, PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS).
            approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);
        String report = as(USER_1).getJobReport(project, jobUUID);

        if (project.equals(PROJECT_3)) {
            assertReport(report).
                enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
                hasTrafficLight(TrafficLight.GREEN).
                hasFindings(0);

            // this okay here - all text files are filtered

            List<UUID> pdsJobUUIDs = TestAPI.fetchAllPDSJobUUIDsForSecHubJob(jobUUID);
            assertEquals(1,pdsJobUUIDs.size());
            UUID pdsJobUUID = pdsJobUUIDs.iterator().next();
            String jobReport = asPDSUser(PDS_TECH_USER).getJobReport(pdsJobUUID);
            assertNull(jobReport);  // report can be fetched, but is null because no launcher script executed
            return;
        }

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

    private void testCheckmarxPDSjobWithBinaryContentUploaded(TestProject project) {
        /* @formatter:off */
        boolean profileHasBinariesEnabledInExecutor = project.equals(PROJECT_2);

        UUID jobUUID = as(USER_1).
                createCodeScanWithTemplate(
                        IntegrationTestTemplateFile.CODE_SCAN_2_BINARIES_DATA_ONE_REFERENCE,
                        project,
                        NOT_MOCKED,
                        TemplateData.builder().
                            setVariable("__folder__",
                                    CODE_SCAN__CHECKMARX__MULTI__ZERO_WAIT.getMockDataIdentifier()).
                            addReferenceId("files-b").
                            build());

        /* execute */
        as(USER_1).
            uploadBinaries(project, jobUUID, PATH_TO_TARFILE_WITH_DIFFERENT_DATA_SECTIONS).
            approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);
        String report = as(USER_1).getJobReport(project, jobUUID);

        // When binaries are not enabled traffic light is green - the execution was gracefully skipped
        if (!profileHasBinariesEnabledInExecutor) {
            assertReport(report).
                enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
                hasTrafficLight(TrafficLight.GREEN).
                hasFindings(0); // no finding, because not executed
        }

        // Inspect PDS result
        List<UUID> pdsJobUUIDs = TestAPI.fetchAllPDSJobUUIDsForSecHubJob(jobUUID);
        assertEquals(1,pdsJobUUIDs.size());
        UUID pdsJobUUID = pdsJobUUIDs.iterator().next();

        PDSJobStatusState pdsJobStatusState = asPDSUser(PDS_ADMIN).getJobStatusState(pdsJobUUID);
        if (profileHasBinariesEnabledInExecutor) {
            // when binaries are enabled, the test product script fails, because no sources are available
            // (which is pretty much the same as when calling the origin produt without sources)
            assertEquals(PDSJobStatusState.FAILED,pdsJobStatusState);
        }else {
            assertEquals(PDSJobStatusState.DONE,pdsJobStatusState);
            String jobReport = asPDSUser(PDS_TECH_USER).getJobReport(pdsJobUUID);

            assertNull(jobReport);  // report can be fetched, but is null because no launcher script executed
        }
        /* @formatter:on */
    }

}
