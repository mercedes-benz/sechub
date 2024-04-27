// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario4;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario4.Scenario4.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestSecHubJobInfoForUserListPage;

/**
 * Integration tests to check cancel operations works
 *
 * @author Albert Tregnaghi
 *
 */
public class CancelJobScenario4IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario4.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    /**
     * We start a long running job and start a cancel operation here
     */
    public void cancel_a_long_running_webscan_job() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncWebScanGreenLongRunningAndGetJobUUID(project);
        waitForJobRunning(project, sechubJobUUD);

        /* execute */
        as(SUPER_ADMIN).cancelJob(sechubJobUUD);

        /* test */
        waitForJobStatusCancelRequestedOrCanceled(project, sechubJobUUD);
        waitForJobStatusFailed(project, sechubJobUUD);

        assertJobStatus(project, sechubJobUUD).hasMessage(SecHubMessageType.INFO,"Job execution was canceled by user");

        // fetch last user job - must be the one we have created here...
        TestSecHubJobInfoForUserListPage jobInfo = as(USER_1).fetchUserJobInfoListOneEntryOrNull(project);
        assertUserJobInfo(jobInfo).hasJobInfoFor(sechubJobUUD).withExecutionResult("FAILED");

        /* @formatter:on */
    }

    @Test
    /**
     * We start a long running job and start a cancel operation here
     */
    public void cancel_a_long_running_codescan_job() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(project,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__4_SECONDS_WAITING);
        waitForJobRunning(project, sechubJobUUD);

        /* execute */
        as(SUPER_ADMIN).cancelJob(sechubJobUUD);

        /* test */
        waitForJobStatusCancelRequestedOrCanceled(project, sechubJobUUD);
        waitForJobStatusFailed(project, sechubJobUUD);

        assertJobStatus(project, sechubJobUUD).hasMessage(SecHubMessageType.INFO,"Job execution was canceled by user");


        /* @formatter:on */
    }

}