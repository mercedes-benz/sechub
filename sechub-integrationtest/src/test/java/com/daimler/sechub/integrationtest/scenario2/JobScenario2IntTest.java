// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.AssertMail.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertJobScheduler.TestExecutionResult;
import com.daimler.sechub.integrationtest.api.AssertJobScheduler.TestExecutionState;
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

public class JobScenario2IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(10);

	@Test
	public void a_triggered_job_being_running_is_NOT_found_anymore_when_canceled_by_admin() {
		/* prepare*/
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		/* @formatter:off */

		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING);

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
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		/* @formatter:off */

		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING);

		assertUser(USER_1).
			canApproveJob(PROJECT_1, jobUUID);

		assertUser(SUPER_ADMIN).
			onJobAdministration().
				canFindRunningJob(jobUUID); // means events are triggered and handled */
		/* @formatter:on */

	}

	@Test
	public void a_triggered_job_is_NOT_found_in_running_jobs_list_by_admin__when_already_done() {
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		/* @formatter:off */

		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__FAST);
		assertUser(USER_1).
			onJobScheduling(PROJECT_1).canFindJob(jobUUID).havingExecutionState(TestExecutionState.INITIALIZING).
			and().
			canApproveJob(PROJECT_1, jobUUID).
			afterThis().
			onJobScheduling(PROJECT_1).canFindJob(jobUUID).havingOneOfExecutionStates(TestExecutionState.READY_TO_START, TestExecutionState.STARTED, TestExecutionState.ENDED);// either ready or already started, ended

		assertUser(SUPER_ADMIN).
			onJobScheduling(PROJECT_1).canFindJob(jobUUID).havingOneOfExecutionStates(TestExecutionState.READY_TO_START, TestExecutionState.STARTED).// either ready or already started
			and().
			onJobAdministration().canNotFindRunningJob(jobUUID); // means events are triggered and handled */
		/* @formatter:on */

	}




}
