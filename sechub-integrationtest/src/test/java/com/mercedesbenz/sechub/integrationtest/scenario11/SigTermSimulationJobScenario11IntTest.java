// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario11;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.scenario11.Scenario11.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestSecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;

/**
 * Integration tests to check SIGTERM handling operations work
 *
 * @author Albert Tregnaghi
 *
 */
public class SigTermSimulationJobScenario11IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario11.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    /**
     * We start a long running job (scenario11 has lazy streams and takes more than
     * 3 seconds) and start a suspend operation here.
     *
     * The PDS job will run in background without stop. When new SecHub server comes
     * up (we simulate by turning off termination flag in integration test mode...)
     * the result from PDS is reused (if the PDS job is not already done SecHub will
     * wait and reuse - so we have no race condition problems here)
     */
    public void simulate_SecHub_SIGTERM_handling_leads_to_restart_of_SecHub_job_and_reuse_of_pds_job() {

        TestUser user = USER_1;
        boolean resetCalled = false;
        try {

            /* @formatter:off */
            /* check preconditions */
            assertThat(isSecHubTerminating()).
                        describedAs("Ensure the server is not already terminating").
                        isFalse();

            /* prepare 1 */
            UUID sechubJobUUD = as(user).triggerAsyncPDSCodeScanWithWantedTrafficLightResult(project, TrafficLight.YELLOW);
            waitForJobRunning(project, sechubJobUUD);
            UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(sechubJobUUD);
            waitForPDSJobInState(PDSJobStatusState.RUNNING, 2000,100,pdsJobUUID,true);

            /* execute 1 - simulate SIGTERM server stop - e.g. by K8s deployment */
            triggerSecHubTerminationService(); // We directly call SchedulerTerminationService (in "real live" the PreDestroy event from spring boot triggers this when SIGTERM event received)

            /* test 1 */
            waitForJobStatusSuspended(project, sechubJobUUD);
            assertPDSJobStatus(pdsJobUUID).isInState(PDSJobStatusState.RUNNING); // pds job is still running

            // fetch last user job - must be the one we have created here...
            TestSecHubJobInfoForUserListPage jobInfo = as(user).
                fetchUserJobInfoListOneEntryOrNull(project);

            assertUserJobInfo(jobInfo).
                hasJobInfoFor(sechubJobUUD).
                withEndedTimeStampNotNull().
                withExecutionResult("NONE"); // no result

            /* prepare 2 - we simulate here a new server start - means without termination
             *             flag set -> job processing will start again and processes old job
             *             like a new server would do
             */
            resetSecHubTerminationService();
            resetCalled=true;

            /* test 2 - check suspended report will resume and be done automatically */
            waitForJobDone(project, sechubJobUUD, 15, true);

            /* test 3 - check report is as expected */
            String report = as(user).getJobReport(project, sechubJobUUD);
            assertReportUnordered(report).
                hasTrafficLight(TrafficLight.YELLOW).
                finding().description("i am a medium error").isContained();

            /* test 4 - check only ONE PDS job was used */
            List<UUID> relatedPSjobUUIDs = fetchAllPDSJobUUIDsForSecHubJob(sechubJobUUD);
            assertThat(relatedPSjobUUIDs).
                describedAs("Check that ONE PDS job has been reused by SecHub job which was suspended and now restarted").
                hasSize(1).
            contains(pdsJobUUID);


            /* @formatter:on */
        } finally {
            if (!resetCalled) {
                // IMPORTANT:
                // -----------
                // we MUST ensure termination service reset is done to avoid cross site effects
                // on other tests when reset was not called in former try block!
                resetSecHubTerminationService();
            }
        }
    }
}