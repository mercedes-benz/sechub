// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario18;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario18.Scenario18.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;

public class PDSCancellationScenario18IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario18.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    @UseCaseAdminCancelsJob(@Step(number = 0, name = "integration test"))
    public void sechub_starts_job_and_triggers_cancel_must_be_handled_by_PDS_script() {
        /* @formatter:off */
        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(PROJECT_1,NOT_MOCKED);

        as(USER_1).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            approveJob(project, jobUUID);

        /* wait until PDS has started the job */
        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        waitForPDSJobInState(PDSJobStatusState.RUNNING, 5, 300, pdsJobUUID,true);

        /* execute */
        as(SUPER_ADMIN).cancelJob(jobUUID);

        /* test */
        waitForJobStatusCanceled(project, jobUUID, true);

        assertJobStatus(PROJECT_1,jobUUID).
            enablePDSAutoDumpOnErrorsForSecHubJob().
            hasMessage(SecHubMessageType.INFO, "Job execution was canceled by user");

        waitForPDSJobInState(PDSJobStatusState.CANCELED, 25, 300, pdsJobUUID,true);

        // test messages
        SecHubMessagesList messages = asPDSUser(PDS_ADMIN).getJobMessages(pdsJobUUID);
        List<SecHubMessage> sechubMessages = messages.getSecHubMessages();
        assertEquals(2,sechubMessages.size());

        Set<SecHubMessageType> messageTypesFound = new LinkedHashSet<>();
        List<String> messageTextsFound = new ArrayList<>();
        for (SecHubMessage sechubMessage: sechubMessages) {
            messageTypesFound.add(sechubMessage.getType());
            messageTextsFound.add(sechubMessage.getText());
        }

        // We have two special info messages here. Each has info type
        assertTrue(messageTypesFound.contains(SecHubMessageType.INFO));
        assertEquals(1, messageTypesFound.size());

        // Now check for the final cancel successful message:
        assertTrue(messageTextsFound.contains("Event type:cancel_requested was received and handled by script"));

        /* @formatter:on */
    }

}
