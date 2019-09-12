// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.AssertMail.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;

import java.util.UUID;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.RunMode;

public class SwitchSchedulerJobProcessingScenario2IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(30);

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Start scan job ...........................+ */
	/* +-----------------------------------------------------------------------+ */

	@After
	public void enableSchedulingAfterTest() {
		/* ensure scheduler job processing is enabled again after every of these tests*/
		as(SUPER_ADMIN).enableSchedulerJobProcessing();
	}

	@Test
	public void when_scheduler_job_processing_is_disabled_a_job_can_be_posted_but_is_only_executed_after_enabled_again() {
		/* @formatter:off */

		/* prepare */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);
		/* execute */
		as(SUPER_ADMIN).
			disableSchedulerJobProcessing();
		/* prepare */
		waitSeconds(1); // give event handling a chance...
		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,RunMode.LONG_RUNNING_BUT_GREEN);// we use long running job (10seconds) - necessary, see comment beyond

		assertUser(USER_1).canApproveJob(PROJECT_1, jobUUID);

		waitSeconds(1); // give event handling a chance...

		/* test */
		assertUser(SUPER_ADMIN).
			onJobAdministration().canNotFindRunningJob(jobUUID); // means here, the job is not executed at all.. */
		assertMailToAdminsExists("Scheduler job processing disabled");

		/* execute */
		as(SUPER_ADMIN).enableSchedulerJobProcessing();

		waitSeconds(1); // give event handling a chance...

		/* test */
		assertUser(SUPER_ADMIN).
			onJobAdministration().canFindRunningJob(jobUUID); // means here, the job is not executed at all.. we know this, because we used long running job (10s) */
		assertMailToAdminsExists("Scheduler job processing disabled");

		/* @formatter:on */

	}

}
