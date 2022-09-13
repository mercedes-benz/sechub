// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario18;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario18.Scenario18.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.adapter.pds.data.PDSJobStatus.PDSAdapterJobStatusState;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

public class PDSCancellationScenario18IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario18.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
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
        waitForPDSJobInState(PDSAdapterJobStatusState.RUNNING, 5, 300, pdsJobUUID,true);

        /* execute */
        as(SUPER_ADMIN).cancelJob(jobUUID);

        /* test */
        waitForJobStatusCanceled(project, jobUUID, true);

        assertJobStatus(PROJECT_1,jobUUID).
            enablePDSAutoDumpOnErrorsForSecHubJob().
            hasMessage(SecHubMessageType.INFO, "Job execution was canceled by user");

        waitForPDSJobInState(PDSAdapterJobStatusState.CANCELED, 25, 300, pdsJobUUID,true);

        SecHubMessagesList messages = asPDSUser(PDS_ADMIN).getJobMessages(pdsJobUUID);
        List<SecHubMessage> sechubMessages = messages.getSecHubMessages();
        assertEquals(2,sechubMessages.size());


        /* @formatter:on */
    }

}
