// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario22;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario22.Scenario22.*;
import static com.mercedesbenz.sechub.test.TestConstants.SOURCECODE_ZIP;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.*;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.integrationtest.api.*;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor;

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
        project5_start_PDS_prepare_job_from_remote_code_scan_configuration_and_check_for_configuration();

        // project6_start_pds_prepare_with_pds_wrapper_application();

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

    // @Test
    public void project5_start_PDS_prepare_job_from_remote_code_scan_configuration_and_check_for_configuration() {
        /* @formatter:off */

        /* prepare */
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_REMOTE_SCAN_CONFIGURATION;
        TestProject project = PROJECT_5;

        /* execute */
        SecHubClientExecutor.ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        UUID jobUUID = result.getSechubJobUUID();

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("d", variables.get("PDS_TEST_KEY_VARIANTNAME"));
        assertEquals("true", variables.get("PDS_PREPARE_EXECUTED"));

        // testing if the returned configuration has the same values as the defined configuration
        String returnedPdsScanConfigurationJSON = variables.get("PDS_SCAN_CONFIGURATION");
        SecHubScanConfiguration returnedConfiguration = SecHubScanConfiguration.createFromJSON(returnedPdsScanConfigurationJSON);
        assertTrue(returnedConfiguration.getCodeScan().isPresent());
        assertFalse(returnedConfiguration.getInfraScan().isPresent());
        assertFalse(returnedConfiguration.getWebScan().isPresent());

        Set<String> usedDataConfigurations = returnedConfiguration.getCodeScan().get().getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, usedDataConfigurations.size());
        assertEquals("remote_example_name", usedDataConfigurations.iterator().next());

        Optional<SecHubDataConfiguration> data = returnedConfiguration.getData();
        assertTrue(data.isPresent());

        List<SecHubSourceDataConfiguration> sources = data.get().getSources();
        assertEquals(1, sources.size());

        SecHubSourceDataConfiguration dataConfiguration = sources.iterator().next();
        Optional<SecHubRemoteDataConfiguration> remote = dataConfiguration.getRemote();
        assertTrue(remote.isPresent());

        String remoteLocation = remote.get().getLocation();
        assertEquals("remote_example_location", remoteLocation);
        String type = remote.get().getType();
        assertEquals("docker", type);

        Optional<SecHubRemoteCredentialConfiguration> credentials = remote.get().getCredentials();
        assertTrue(credentials.isPresent());

        Optional<SecHubRemoteCredentialUserData> user = credentials.get().getUser();
        assertTrue(user.isPresent());
        assertEquals("remote_example_user", user.get().getName());
        assertEquals("remote_example_password", user.get().getPassword());

        /* @formatter:on */
    }

    public void project6_start_pds_prepare_with_pds_wrapper_application() {
        /* @formatter:off */

        /* prepare */
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_REMOTE_SCAN_CONFIGURATION;
        TestProject project = PROJECT_6;

        /* execute */
        SecHubClientExecutor.ExecutionResult result = as(USER_2).withSecHubClient().startSynchronScanFor(project, location);
        UUID jobUUID = result.getSechubJobUUID();

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("e", variables.get("PDS_TEST_KEY_VARIANTNAME"));

        /* check if the prepare-wrapper has uploaded the sources */
        File downloadedFile = TestAPI.getFileUploaded(project, jobUUID, SOURCECODE_ZIP);
        assertNotNull(downloadedFile);
        Assert.assertTrue(downloadedFile.exists());

        // TODO: 28.05.24 laura - check if the prepare-wrapper was executed correctly
        // PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR is not taken from PDS

        }
}