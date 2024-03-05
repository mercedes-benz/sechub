// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario22;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario22.Scenario22.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;
import com.mercedesbenz.sechub.test.TestFileReader;

public class PDSPrepareIntegrationScenario22IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario22.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    /**
     * This is a "multi" test. Why? Because it is faster than executing different
     * tests. When we execute the 4 tests separately, the tests run 22 seconds on
     * machine X. On same machine the "multi" test does the same in 11 seconds!
     *
     * If you have a failing test and you want to debug faster, you can remove the
     * comments from the test methods and start the test directly. But after fixing
     * etc. please dont forget to comment the direct test afterwards again.
     *
     */
    @Test
    public void multi_prepare_test() {
        project1_sechub_calls_prepare_pds_executes_script_and_user_message_is_returned();
        project2_sechub_calls_prepare_and_checkmarx();
        project3_sechub_calls_prepare_which_fails_will_not_start_checkmarx();
        project4_sechub_calls_prepare_which_fails_because_internal_failure_will_not_start_checkmarx();
    }

    // @Test
    public void project1_sechub_calls_prepare_pds_executes_script_and_user_message_is_returned() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        TestAPI.logInfoOnServer(">>>>>INFO: Test with PROJECT 1");
        UUID jobUUID = createCodeScanJob(project);

        /* execute */
        as(USER_1).
                /*
                 * no simulated source code here necessary - we test here only preparation
                 * phase, no additional products
                 */
                approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("a", variables.get("PDS_TEST_KEY_VARIANTNAME"));
        assertEquals("true", variables.get("PDS_PREPARE_EXECUTED"));

        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasTrafficLight(TrafficLight.OFF). // traffic light off, because the only report which was executed, but there was no result inside!
            hasMessage(SecHubMessageType.INFO, "Some preperation info message for user in report (always).").
            hasMessage(SecHubMessageType.WARNING, "No results from a security product available for this job!").
            hasMessages(2).
            hasFindings(0);

        /* @formatter:on */
    }

    // @Test
    public void project2_sechub_calls_prepare_and_checkmarx() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_2;
        TestAPI.logInfoOnServer(">>>>>INFO: Test with PROJECT 2");
        UUID jobUUID = createCodeScanJob(project);

        approveJobAndSimulateSourceCodeAvailable(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("a", variables.get("PDS_TEST_KEY_VARIANTNAME"));
        assertEquals("true", variables.get("PDS_PREPARE_EXECUTED"));


        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasMessage(SecHubMessageType.INFO,"Some preperation info message for user in report (always).").
            hasMessages(1).
            hasTrafficLight(TrafficLight.YELLOW). // traffic light yellow, because project2 has checkmarx configured as well and prepare did not fail (we used the error message only as additional info for testing)
            hasFindings(109); // some findings

        /* @formatter:on */
    }

    // @Test
    public void project3_sechub_calls_prepare_which_fails_will_not_start_checkmarx() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_3;
        TestAPI.logInfoOnServer(">>>>>INFO: Test with PROJECT 3");
        UUID jobUUID = createCodeScanJob(project);

        /* execute */
        approveJobAndSimulateSourceCodeAvailable(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("b", variables.get("PDS_TEST_KEY_VARIANTNAME"));
        assertEquals("true", variables.get("PDS_PREPARE_FAILED_NO_RESULT_FILE_BUT_EXIT_0"));

        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasTrafficLight(TrafficLight.OFF). // traffic light off, because preparation failed and no other product (in this case checkmarx) may be executed
            hasMessage(SecHubMessageType.INFO, "Some preperation info message for user in report (always).").
            hasMessage(SecHubMessageType.WARNING, "No results from a security product available for this job!").
            hasMessage(SecHubMessageType.ERROR, "Some preperation error message for user in report.").
            hasMessages(3).
            hasFindings(0);

        /* @formatter:on */
    }

    // @Test
    public void project4_sechub_calls_prepare_which_fails_because_internal_failure_will_not_start_checkmarx() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_4;
        TestAPI.logInfoOnServer(">>>>>INFO: Test with PROJECT 4");
        UUID jobUUID = createCodeScanJob(project);

        approveJobAndSimulateSourceCodeAvailable(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("c", variables.get("PDS_TEST_KEY_VARIANTNAME"));
        assertEquals("true", variables.get("PDS_PREPARE_FAILED_NO_RESULT_FILE_AND_EXIT_5"));

        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasTrafficLight(TrafficLight.OFF). // traffic light off, because preparation failed and no other product (in this case checkmarx) may be executed!
            hasMessage(SecHubMessageType.WARNING, "No results from a security product available for this job!").
            hasMessage(SecHubMessageType.ERROR, "Job execution failed because of an internal problem!").
            hasMessages(2).
            hasFindings(0);

        /* @formatter:on */
    }

    private UUID createCodeScanJob(TestProject project) {
        /* @formatter:off */
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
          return jobUUID;
          /* @formatter:on */
    }

    private void approveJobAndSimulateSourceCodeAvailable(TestProject project, UUID jobUUID) {
        /* @formatter:off */

          /**
           * Here it is only a pseudo upload - why? The product is mocked and the mock
           * implementation will return not parts from upload, but only by the paths
           * defined inside the configuration file. But we need the upload to have the
           * checkmarx adapter working correctly.
           */
          String pseudoUpload = PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS;

          as(USER_1).
              /* Next line is necessary to simulate preparation done - we just upload "normal" way,
               * so data is available and checkmarx adapter may not fail because of missing sources...
               */
              uploadSourcecode(project, jobUUID, pseudoUpload).

              approveJob(project, jobUUID);
          /* @formatter:on */
    }

    @Test
    public void startPDSPrepareJobFromRemoteCodeScanConfigurationwithoutClient() {

        /* prepare */
        String configurationAsJson = TestFileReader.loadTextFile(new File("./src/test/resources/sechub-integrationtest-remote-scan-configuration.json"));
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);
        configuration.setProjectId("project1");
        TestProject project = PROJECT_1;

        /* execute */
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);
        as(USER_1).approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);
        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).hasTrafficLight(TrafficLight.OFF). // traffic light off, because the only report
                                                                                                               // which was executed, but there was no result
                                                                                                               // inside!
                hasMessage(SecHubMessageType.INFO, "Some preperation info message for user in report (always).")
                .hasMessage(SecHubMessageType.WARNING, "No results from a security product available for this job!").hasMessages(2).hasFindings(0);

    }

}