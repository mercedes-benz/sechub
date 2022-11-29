// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario19;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario19.Scenario19.*;

import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestSecHubJobInfoForUser;

/**
 * Integration tests to check operations for user job information fetching
 *
 * @author Albert Tregnaghi
 *
 */
public class FetchJobInfoForUserIntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario19.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    /**
     * We test here multiple situations inside ONE test. Reason: just one
     * preparation necessary and faster to test
     */
    public void created_jobs_are_returned_in_expected_order_and_behavior() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD1 = as(USER_1).createWebScan(project);
        UUID sechubJobUUD2 = as(USER_1).createWebScan(project);
        UUID sechubJobUUD3 = as(USER_1).createWebScan(project);

        /* execute (A) - default limit (1) */
        TestSecHubJobInfoForUser jobInfoA = as(USER_1).fetchUserJobInfoListOneEntryOrNull(project);

        /* test (A) */
        assertUserJobInfo(jobInfoA).hasJobInfoFor(sechubJobUUD3);

        /* execute (B) - use limit 2 */
        List<TestSecHubJobInfoForUser> jobInfoListB = as(USER_1).fetchUserJobInfoList(project, 2);

        /* test (B) */
        assertUserJobInfo(jobInfoListB).
            hasEntries(2).
            hasJobInfoFor(sechubJobUUD3).
            and().
            hasJobInfoFor(sechubJobUUD2);


        /* execute (C) - use limit 10 */
        List<TestSecHubJobInfoForUser> jobInfoListC = as(USER_1).fetchUserJobInfoList(project, 10);

        /* test (C) */
        assertUserJobInfo(jobInfoListC).
            hasEntries(3).
            hasJobInfoFor(sechubJobUUD3).
            and().
            hasJobInfoFor(sechubJobUUD2).
            and().
            hasJobInfoFor(sechubJobUUD1);

        /* prepare (D) */
        UUID sechubJobUUD4 = as(USER_1).createWebScan(project);

        /* execute (D) - use limit 2  */
        List<TestSecHubJobInfoForUser> jobInfoListD = as(USER_1).fetchUserJobInfoList(project, 2);

        /* test (D) */
        assertUserJobInfo(jobInfoListD).
            hasEntries(2).
            hasJobInfoFor(sechubJobUUD3).
                withExecutionResult("NONE").
                withOneOfAllolowedExecutionState("INITIALIZING").
            and().
            hasJobInfoFor(sechubJobUUD4);

        /* execute (E) - use limit 0 - will do fallback to one  */
        List<TestSecHubJobInfoForUser> jobInfoListE = as(USER_1).fetchUserJobInfoList(project, 0);

        /* test (E) */
        assertUserJobInfo(jobInfoListE).
            hasEntries(1).
            hasJobInfoFor(sechubJobUUD4).
                withExecutionResult("NONE").
                withOneOfAllolowedExecutionState("INITIALIZING");

        /* prepare (F) */
        as(SUPER_ADMIN).cancelJob(sechubJobUUD4);
        waitForJobStatusCancelRequestedOrCanceled(project, sechubJobUUD4);

        /* execute (F) - use limit 0 - will do fallback to one  */
        List<TestSecHubJobInfoForUser> jobInfoListF = as(USER_1).fetchUserJobInfoList(project, 0);

        /* test (F) */
        assertUserJobInfo(jobInfoListF).
            hasEntries(1).
            hasJobInfoFor(sechubJobUUD4).
                withExecutionResult("FAILED").
                withOneOfAllolowedExecutionState("CANCELED", "CANCEL_REQUESTED");

    }
    /* @formatter:on */

}