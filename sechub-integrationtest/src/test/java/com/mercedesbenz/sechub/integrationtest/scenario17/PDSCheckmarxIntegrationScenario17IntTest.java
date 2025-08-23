// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario17;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario17.Scenario17.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.api.internal.gen.model.Reference;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.api.internal.gen.model.TextBlock;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
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

    private static final boolean WITH_ANALYTICS = true;
    private static final boolean WITHOUT_ANALYTICS = false;

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario17.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_only_which_is_supported_by_checkmarx_PDS_setup_results_yellow_and_explain_first_finding() {
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_1, WITH_ANALYTICS, false, true);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_only_which_is_supported_by_checkmarx_PDS_setup_results_yellow__file_is_with_umlauts() {
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_1, WITH_ANALYTICS, true);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_only_accepted_is_binary_and_source_via_job_parameter_results_yellow() {
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_2, WITH_ANALYTICS, false);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_and_uploads_sources_which_would_be_accepted_but_everything_is_filtered_results_in_job_done_without_result() {
        testCheckmarxPDSJobWithSourceContentUploaded(PROJECT_3, WITHOUT_ANALYTICS, false);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_but_uploads_binary_only__which_is_not_supported_by_checkmarx_PDS_setup_results_in_job_done_without_result() {
        testCheckmarxPDSjobWithBinaryContentUploaded(PROJECT_1, WITH_ANALYTICS);
    }

    @Test
    public void pds_calls_checkmarx_wrapper_but_uploads_binary_only__overriden_with_binary_accepted_results_in_failing_job() {
        testCheckmarxPDSjobWithBinaryContentUploaded(PROJECT_2, WITH_ANALYTICS);
    }

    private void testCheckmarxPDSJobWithSourceContentUploaded(TestProject project, boolean withAnalytics, boolean dataFileWithUmlauts) {
        testCheckmarxPDSJobWithSourceContentUploaded(project, withAnalytics, dataFileWithUmlauts, false);
    }

    /* @formatter:off */
    private void testCheckmarxPDSJobWithSourceContentUploaded(TestProject project, boolean withAnalytics, boolean dataFileWithUmlauts, boolean triggerExplainFirstCWEId) {
        /* @formatter:off */
        String pathToUploadZipFile = dataFileWithUmlauts ? PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS_BUT_FILENAME_WITH_UMLAUTS : PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS;
        long expectedUploadSizeInBytes = dataFileWithUmlauts ? 212 : 198L;
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
            uploadSourcecode(project, jobUUID, pathToUploadZipFile).
            approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        if (triggerExplainFirstCWEId) {
            /* in integration tests we have no real AI integration and the fake ai integration does not provide information for CWE 36.
             * Because of this, the fallback mechanism will be used and we can check here that the output from fallback is as wanted:
             */
            SecHubExplanationResponse explain = as(USER_1).explainFinding(project.getProjectId(), jobUUID, 1);
            TextBlock findingExplanation = explain.getFindingExplanation();
            assertThat(findingExplanation).isNotNull();
            assertThat(findingExplanation.getTitle()).isNotNull().contains("Absolute Path Traversal");
            assertThat(findingExplanation.getContent()).isNotNull().contains("No description available for CWE-36");

            assertThat(explain.getReferences()).hasSize(1);
            Reference firstReference = explain.getReferences().iterator().next();
            assertThat(firstReference.getTitle()).isEqualTo("CWE-36 - Absolute Path Traversal");
            assertThat(firstReference.getUrl()).isEqualTo("https://cwe.mitre.org/data/definitions/36.html");
        }

        String report = as(USER_1).getJobReport(project, jobUUID);

        // check statistics
        assertStatistic(jobUUID).
            isForProject(project).
            hasData("UPLOAD_SOURCES", "SIZE_IN_BYTES", expectedUploadSizeInBytes);

        if (withAnalytics) {
            assertStatistic(jobUUID).
                firstExecution().
                    hasRunData("FILES", "ALL", 3474).
                    hasRunData("FILES_LANG", "java", 2337).

                    hasRunData("LOC", "ALL", 584644).
                    hasRunData("LOC_LANG", "java", 126190).
                    hasRunData("LOC_LANG", "go", 4151);
        }

        if (project.equals(PROJECT_3)) {
            assertReport(report).
                enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
                hasTrafficLight(TrafficLight.OFF). // no result because of filtering at PDS + graceful fall through, so traffic light must be off
                hasMessages(1).
                hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
                hasFindings(0);

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
        int recompressPDSJobIndex = 0;
        if (withAnalytics) {
            recompressPDSJobIndex=1; // when analytics enabled, we use the second PDS job (which does recompress the file)
        }
        UUID pdsJobUUID = waitForPDSJobOfSecHubJobAtGivenPositionAndReturnPDSJobUUID(jobUUID,recompressPDSJobIndex);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);


        assertEquals(Boolean.valueOf(dataFileWithUmlauts).toString(),variables.get("TEST_RECOMPRESSED_ZIP_DATA_FILENAME_WITH_UMLAUTS"));

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
        /* check PDS debug enabled variable available - we have enabled it inside the executor configuration */
        assertEquals("true", variables.get("PDS_DEBUG_ENABLED"));
        /* @formatter:on */
    }

    private void testCheckmarxPDSjobWithBinaryContentUploaded(TestProject project, boolean withAnalytics) {
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

        if (!profileHasBinariesEnabledInExecutor) {
            assertReport(report).
                enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
                hasTrafficLight(TrafficLight.OFF). // traffic light off, because the only report which was executed, but there was no result inside!
                hasMessages(1).
                hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
                hasFindings(0); // no finding, because not executed
        }else {
            assertReport(report).
                enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
                hasTrafficLight(TrafficLight.OFF). // traffic light off, because failed
                hasMessages(2).
                hasMessage(SecHubMessageType.ERROR,"Job execution failed because of an internal problem!").
                hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
                hasFindings(0); // no finding, because failed
        }

        // check statistics
        assertStatistic(jobUUID).
            isForProject(project).
            hasData("UPLOAD_BINARIES", "SIZE_IN_BYTES", 9728);

        if (withAnalytics) {
            assertStatistic(jobUUID).
                firstExecution().
                    hasNoRunData(); // our test setup does not scan binaries. So there is no statistic data!
        }

        int amountOfPdsJobs = 1;
        if (withAnalytics) {
            amountOfPdsJobs++;
        }
        // Inspect PDS result
        List<UUID> pdsJobUUIDs = TestAPI.fetchAllPDSJobUUIDsForSecHubJob(jobUUID);
        assertEquals(amountOfPdsJobs,pdsJobUUIDs.size());
        UUID pdsJobUUID = pdsJobUUIDs.get(amountOfPdsJobs-1);

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
