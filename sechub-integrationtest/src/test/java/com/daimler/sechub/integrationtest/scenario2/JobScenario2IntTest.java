// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.RunMode;

public class JobScenario2IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(10);

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Start scan job ...........................+ */
	/* +-----------------------------------------------------------------------+ */

	@Test
	public void a_triggered_job_is_found_in_running_jobs_list_by_admin__when_not_already_done() {
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		/* @formatter:off */

		UUID jobUUID = assertUser(USER_1).
			canCreateWebScan(PROJECT_1,RunMode.LONG_RUNNING_BUT_GREEN);

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
			canCreateWebScan(PROJECT_1,RunMode.NORMAL);

		assertUser(USER_1).canApproveJob(PROJECT_1, jobUUID);

		waitSeconds(1); // give event handling a chance...

		assertUser(SUPER_ADMIN).
			onJobAdministration().canNotFindRunningJob(jobUUID); // means events are triggered and handled */
		/* @formatter:on */

	}

}
