// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectDeleteScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(60);

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

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
        as(SUPER_ADMIN).deleteProject(PROJECT_2);

        /* test */
        wait2SecondsSoAsyncDeleteEventsDone();

        assertProject(PROJECT_1).
            doesNotExist().
            hasAccessEntriesInDomainSchedule(0).
            hasAccessEntriesInDomainScan(0); // no longer access

        assertProject(PROJECT_2).
            doesNotExist().
            hasAccessEntriesInDomainSchedule(0).
            hasAccessEntriesInDomainScan(0); // no longer access

        assertUser(USER_1).
            doesExist().
            hasNotOwnerRole(). // no longer role owner - was only owner of project1
            hasUserRole(); // still user

	}
	/* @formatter:on */

    /* @formatter:off */
	@Test
	public void super_admin_deletes_project__deletes_also_all_scan_and_product_results() throws Exception {
		/* check preconditions*/
		assertUser(USER_1).
			isAssignedToProject(PROJECT_1);

		/* prepare - just execute two jobs */
		ExecutionResult result1 = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT);
		UUID sechubJobUUID1 = result1.getSechubJobUUID();

		ExecutionResult result2 = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_YELLOW_ZERO_WAIT);
		UUID sechubJobUUID2 = result2.getSechubJobUUID();

		/* check preconditions */
		assertNotNull(sechubJobUUID1);
		assertNotNull(sechubJobUUID2);

		assertProject(PROJECT_1).
			doesExist().
			hasProductResultsInDomainScan(4). // 2 x 2(means SERECO + SOURCSCAN RESULT for each job))
			hasScanReportsInDomainScan(2); // 2 x 1 result

		/* execute */
		as(SUPER_ADMIN).deleteProject(PROJECT_1);

		/* test */
		wait2SecondsSoAsyncDeleteEventsDone();

		assertProject(PROJECT_1).
			doesNotExist().
			hasProductResultsInDomainScan(0).
			hasScanReportsInDomainScan(0);

	}
	/* @formatter:on */

    private void wait2SecondsSoAsyncDeleteEventsDone() {
        // We wait here to let the new (async) access change happen
        // Unfortunately this depends on the environment where tests are
        // executed! On a dedicated build server
        // values between 500-1000 millis are more than enough to have no flaky
        // tests, but on slower machines (like GitHub Actions) we must wait longer
        TestAPI.waitMilliSeconds(2000);
    }

}
