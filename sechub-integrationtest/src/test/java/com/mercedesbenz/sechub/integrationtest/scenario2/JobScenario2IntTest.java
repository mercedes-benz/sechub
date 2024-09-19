// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.AssertMail.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.integrationtest.api.AssertJobScheduler.TestExecutionResult;
import com.mercedesbenz.sechub.integrationtest.api.AssertJobScheduler.TestExecutionState;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;

public class JobScenario2IntTest {

    private static final Logger LOG = LoggerFactory.getLogger(JobScenario2IntTest.class);

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    @Test
    public void a_triggered_job_being_running_is_NOT_found_anymore_when_canceled_by_admin() {
        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* @formatter:off */

		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__10_SECONDS_WAITING);

		assertUser(USER_1).
			onJobScheduling(PROJECT_1).
				canFindJob(jobUUID).
					havingExecutionResult(TestExecutionResult.NONE).
					havingExecutionState(TestExecutionState.INITIALIZING).
			and().

			now().
			canApproveJob(PROJECT_1, jobUUID).
			afterThis().

			onJobScheduling(PROJECT_1).
				canFindJob(jobUUID).
					havingExecutionResult(TestExecutionResult.NONE).
					havingOneOfExecutionStates(TestExecutionState.READY_TO_START, TestExecutionState.STARTED);// either ready or already started

		assertUser(SUPER_ADMIN).
			onJobAdministration().
				canFindRunningJob(jobUUID); // means events are triggered and handled - job is running (long time)

		/* execute */
		as(SUPER_ADMIN).
			cancelJob(jobUUID);

		/* test*/
		assertUser(SUPER_ADMIN). // no longer found in administration
			onJobAdministration().
			canNotFindRunningJob(jobUUID);

		assertUser(USER_1).
			onJobScheduling(PROJECT_1). // found, but marked as failed inside scheduling
				canFindJob(jobUUID).
					havingExecutionResult(TestExecutionResult.FAILED).
					havingExecutionState(TestExecutionState.CANCEL_REQUESTED);

		/// check notification
		assertMailExists(USER_1.getEmail(), "Your SecHub Job has been canceled"); // notification layer done as well
		/* @formatter:on */

    }

    @Test
    public void a_triggered_job_is_found_in_running_jobs_list_by_admin__when_not_already_done() {
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* @formatter:off */

		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__10_SECONDS_WAITING);

		assertUser(USER_1).
			canApproveJob(PROJECT_1, jobUUID);

		assertUser(SUPER_ADMIN).
			onJobAdministration().
				canFindRunningJob(jobUUID); // means events are triggered and handled */
		/* @formatter:on */

    }

    @Test
    public void job_list_for_done_job__and_encryption_and_cleanup_are_working() {
        /* step 1 : job list entries */
        UUID doneJobUUID = assertAlreadyDoneJobIsNotListedInAdminJobList();
        int scheduleDataSizeBeforeRotate = assertEncryptionStatus().domain("schedule").hasData().getDataSize();

        /* step 2 : rotate encryption for job */
        triggerEncryptionRotationAndAssertEncryptionIsDone(doneJobUUID);

        /* step3: check status must have now one more data */
        int scheduleDataSizeAfterRotate = assertEncryptionStatus()./* dump(). */domain("schedule").hasData().getDataSize();
        assertThat(scheduleDataSizeAfterRotate).isEqualTo(scheduleDataSizeBeforeRotate + 1); // must be one more...

        /*
         * step4: rotate encryption for job again - means we ensure former job uses no
         * longer the older cipher pool data and next cleanup will at least remove this
         * one
         */
        triggerEncryptionRotationAndAssertEncryptionIsDone(doneJobUUID);

        int scheduleDataSizeAfterRotate2 = assertEncryptionStatus()/* .dump() */.domain("schedule").hasData().getDataSize();
        assertThat(scheduleDataSizeAfterRotate2).isEqualTo(scheduleDataSizeBeforeRotate + 2); // must be one more...

        /*
         * now cleanup cipher pool data (we do not want to wait for auto cleanup...
         * takes too long time
         */
        startScheduleCipherPoolDataCleanup();

        /*
         * wait until auto cleanup is done and encryption pool is cleaned
         */
        executeRunnableAndAcceptAssertionsMaximumTimes(20, () -> {
            int scheduleDataSize3 = assertEncryptionStatus()./* dump(). */domain("schedule").hasData().getDataSize();
            LOG.info("Fetched schedule encryption pool size(3): {}", scheduleDataSize3);
            assertThat(scheduleDataSize3).isLessThan(scheduleDataSizeAfterRotate2); // must be less

        }, 500);
    }

    private void triggerEncryptionRotationAndAssertEncryptionIsDone(UUID doneJobUUID) {
        /* @formatter:off */
		/* prepare 3 */
		Long formerEncryptionPoolid = fetchScheduleEncryptionPoolIdForJob(doneJobUUID);
		LOG.info("Job: {} had encryption pool id: {}", doneJobUUID, formerEncryptionPoolid);

		SecHubEncryptionData data = new SecHubEncryptionData();
		data.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_256);
		data.setPasswordSourceType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
		data.setPasswordSourceData("INTEGRATION_TEST_SECRET_1_AES_256"); // see IntegrationTestEncryptionEnvironmentEntryProvider

		/* execution 3 - change encryption */
		as(SUPER_ADMIN).rotateEncryption(data);

		/* test 3 */
		executeRunnableAndAcceptAssertionsMaximumTimes(10, ()->{

		    Long newEncryptionPoolid = fetchScheduleEncryptionPoolIdForJob(doneJobUUID);
		    assertThat(newEncryptionPoolid).isNotEqualTo(formerEncryptionPoolid);
		    LOG.info("Job: {} has now encryption pool id: {}", doneJobUUID, newEncryptionPoolid);

		}, 500);
		/* @formatter:on */
    }

    private UUID assertAlreadyDoneJobIsNotListedInAdminJobList() {
        /* @formatter:off */
        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* @formatter:off */

        /* execute 1 - start job */
		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__ZERO_WAIT);

		/* test 1 - start job and wait job to be done. After this not in running jobs any more*/
        assertUser(USER_1).
			onJobScheduling(PROJECT_1).
			    canFindJob(jobUUID).
			    havingExecutionState(TestExecutionState.INITIALIZING).
			and().
			canApproveJob(PROJECT_1, jobUUID);

		assertUser(SUPER_ADMIN).
		    waitForJobDone(PROJECT_1, jobUUID).

		    afterThis().

			onJobScheduling(PROJECT_1).
			    canFindJob(jobUUID).
			    havingOneOfExecutionStates(TestExecutionState.ENDED).
			and().
			onJobAdministration().
			    canNotFindRunningJob(jobUUID); // means events are triggered and handled */
        return jobUUID;
        /* @formatter:on */
    }

}
