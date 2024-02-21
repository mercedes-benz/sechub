package com.mercedesbenz.sechub.integrationtest.scenario22;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario22.Scenario22.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

public class PDSPrepareIntegrationScenario22IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario22.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void project1_sechub_calls_prepare_pds_executes_script_and_user_message_is_returned() {

        TestProject project = PROJECT_1;
        /* @formatter:off */
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
                /* no upload - okay, because prepare is currently always executed */
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
                hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
                hasMessage(SecHubMessageType.INFO,"Some preperation info message for user in report.").
                hasMessages(2).
                hasFindings(0); // no findings

        /* @formatter:on */
    }

    @Test
    public void project2_sechub_calls_prepare_and_checkmarx() {

        TestProject project = PROJECT_2;
        /* @formatter:off */
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
                /* upload - otherwise checkmarx PDS is currently not executed (our prepare does only simulate here)*/
                uploadSourcecode(project, jobUUID, PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS).
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
                hasMessage(SecHubMessageType.INFO,"Some preperation info message for user in report.").
                hasMessages(1).
                hasTrafficLight(TrafficLight.YELLOW). // traffic light not off, because project2 has checkmarx configured as well and prepare did not fail (we used the error message only as additional info for testing)
                hasFindings(109); // some findings

        /* @formatter:on */
    }

    @Test
    public void project3_sechub_calls_prepare_which_fails_will_not_start_checkmarx() {

        TestProject project = PROJECT_3;
        /* @formatter:off */
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
        /* no upload - okay, because prepare is currently always executed */
        approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("b", variables.get("PDS_TEST_KEY_VARIANTNAME"));
        assertEquals("true", variables.get("PDS_PREPARE_FAILED"));


        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasTrafficLight(TrafficLight.OFF). // traffic light off, because preparation failed and no other product (in this case checkmarx) executed
            hasMessage(SecHubMessageType.ERROR,"Some preperation error message for user in report.").
            hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
            hasMessages(2).
            hasFindings(0); // no findings

        /* @formatter:on */
    }

    @Test
    public void project4_sechub_calls_prepare_which_fails_because_internal_failure_will_not_start_checkmarx() {

        TestProject project = PROJECT_4;
        /* @formatter:off */
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
        /* no upload - okay, because prepare is currently always executed */
        approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("c", variables.get("PDS_TEST_KEY_VARIANTNAME"));

        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasTrafficLight(TrafficLight.OFF). // traffic light off, because preparation failed and no other product (in this case checkmarx) executed
            hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
            hasMessages(1). // we have an error message, but because of script failure the message is not returned to user
            hasFindings(0); // no findings

        /* @formatter:on */
    }
}