// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario18;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario18.Scenario18.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

public class PDSCancellationScenario18IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario18.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    public void sechub_starts_job_and_triggers_cancel_cancel_event_must_be_handled_by_script() {
        /* @formatter:off */
        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(PROJECT_1,NOT_MOCKED);

        as(USER_1).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            approveJob(project, jobUUID);

        waitForJobRunning(PROJECT_1, jobUUID);

        /* execute */
        as(SUPER_ADMIN).cancelJob(jobUUID);

        /* test */
        waitForJobStatusCanceled(project, jobUUID, true);
        assertJobStatus(PROJECT_1,jobUUID).
            enablePDSAutoDumpOnErrorsForSecHubJob().
            hasMessage(SecHubMessageType.INFO, "cancel request event handled by script for variant k");

        /* @formatter:on */
    }

}
