package com.mercedesbenz.sechub.integrationtest.scenario22;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
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
    public void sechub_calls_prepare_pds_executes_script_and_user_message_is_returned() {

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
                approveJob(project, jobUUID);

        /* test */
        waitForJobDone(project, jobUUID, 30, true);

        /* check if PDS prepare was executed */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);
        assertEquals("true", variables.get("PDS_PREPARE_EXECUTED"));


        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
                enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
                hasTrafficLight(TrafficLight.OFF). // traffic light off, because the only report which was executed, but there was no result inside!
                hasMessage(SecHubMessageType.WARNING,"No results from a security product available for this job!").
                hasMessage(SecHubMessageType.ERROR,"Some preperation error message for user in report.").
                hasMessages(2).
                hasTrafficLight(TrafficLight.OFF).
                hasFindings(0); // no findings

        /* @formatter:on */
    }
}