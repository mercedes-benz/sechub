// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;

public class ProjectDeleteScenario3IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(60);

	@Rule
	public ExpectedException expected = ExpectedException.none();


	/* @formatter:off */
	@Test
	public void super_admin_deletes_project__deletes_also_access_entries_other_domains_and_user_rolecalculation_is_done() throws Exception {
		/* check preconditions*/
		assertUser(USER_1).
			isAssignedToProject(PROJECT_1).
			hasOwnerRole().
			hasUserRole();

		assertProject(PROJECT_1).
			doesExist().
			hasAccessEntriesInDomainSchedule(1). // user 1 is assigned and so has access to
		    hasAccessEntriesInDomainScan(1); // user 1 is assigned and so has access to

		/* execute */
		as(SUPER_ADMIN).deleteProject(PROJECT_1);

		/* test */
		TestAPI.waitMilliSeconds(400); // we wait here to let the new (async) role calculation be done

		assertProject(PROJECT_1).
			doesNotExist().
			hasAccessEntriesInDomainSchedule(0).
			hasAccessEntriesInDomainScan(0); // no longer access

		assertUser(USER_1).
			doesExist().
			hasNotOwnerRole(). // no longer role owner - was only owner of project1
			hasUserRole(); // still user

	}
	/* @formatter:on */




}
