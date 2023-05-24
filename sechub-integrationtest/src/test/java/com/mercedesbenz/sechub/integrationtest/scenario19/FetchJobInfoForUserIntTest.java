// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario19;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario19.Scenario19.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestSecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

/**
 * Integration tests to check operations for user job information fetching
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
        UUID sechubJobUUD2 = as(USER_1).createWebScan(project, IntegrationTestTemplateFile.WEBSCAN_2); // this job has meta data inside
        UUID sechubJobUUD3 = as(USER_1).createWebScan(project, IntegrationTestTemplateFile.WEBSCAN_3); // this job has meta data inside);

        /* execute (A) - default size (1) */
        TestSecHubJobInfoForUserListPage jobInfoListA = as(USER_1).fetchUserJobInfoListOneEntryOrNull(project);

        /* test (A) */
        assertUserJobInfo(jobInfoListA).
            hasPage(0).
            hasTotalPages(3).
            hasJobInfoFor(sechubJobUUD3,0).
            and().
            hasProjectId(project.getProjectId());

        /* execute (B) - use size 2 */
        TestSecHubJobInfoForUserListPage jobInfoListB = as(USER_1).fetchUserJobInfoList(project, 2);

        /* test (B) */
        assertUserJobInfo(jobInfoListB).
            hasEntries(2).
            hasJobInfoFor(sechubJobUUD3,0).withoutMetaData().
            and().
            hasJobInfoFor(sechubJobUUD2,1).withoutMetaData();


        /* execute (C) - use size 10 */
        TestSecHubJobInfoForUserListPage jobInfoListC = as(USER_1).fetchUserJobInfoList(project, 10, 0, true);

        /* test (C) */
        assertUserJobInfo(jobInfoListC).
            hasEntries(3).
            hasPage(0).
            hasTotalPages(1).
            hasJobInfoFor(sechubJobUUD3,0).withLabel("stage","testing").withLabel("purpose", "security assurance").withLabel("reviewer","senior-security-expert-A").
            and().
            hasJobInfoFor(sechubJobUUD2,1).withLabel("stage","testing").withLabel("purpose", "quality assurance").
            and().
            hasJobInfoFor(sechubJobUUD1,2).withoutMetaData()
            ;

        /* prepare (D) */
        UUID sechubJobUUD4 = as(USER_1).createWebScan(project);

        /* execute (D) - use size 2  */
        TestSecHubJobInfoForUserListPage jobInfoListD = as(USER_1).fetchUserJobInfoList(project, 2);

        /* test (D) */
        assertUserJobInfo(jobInfoListD).
            hasPage(0).
            hasTotalPages(2).
            hasEntries(2).
            hasJobInfoFor(sechubJobUUD3,1).
                withExecutionResult("NONE").
                withOneOfAllowedExecutionStates("INITIALIZING").
            and().
            hasJobInfoFor(sechubJobUUD4,0); // is newer, so on top

        /* execute (E) - use size 0 - will do fallback to one  */
        TestSecHubJobInfoForUserListPage jobInfoListE = as(USER_1).fetchUserJobInfoList(project, 0);

        /* test (E) */
        assertUserJobInfo(jobInfoListE).
            hasEntries(1).
            hasJobInfoFor(sechubJobUUD4,0).
                withExecutionResult("NONE").
                withOneOfAllowedExecutionStates("INITIALIZING");

        /* prepare (F) */
        as(SUPER_ADMIN).cancelJob(sechubJobUUD4);
        waitForJobStatusCancelRequestedOrCanceled(project, sechubJobUUD4);

        /* execute (F) - use size 0 and page -1 - will do fallback to one for size and page zero  */
        TestSecHubJobInfoForUserListPage jobInfoListF = as(USER_1).fetchUserJobInfoList(project, 0, -1);

        /* test (F) */
        assertUserJobInfo(jobInfoListF).
            hasEntries(1).
            hasJobInfoFor(sechubJobUUD4,0).
                withExecutionResult("FAILED").
                withOneOfAllowedExecutionStates("CANCELED", "CANCEL_REQUESTED");

        /* execute (G) - use size 1 - page 1 */
        TestSecHubJobInfoForUserListPage jobInfoListG = as(USER_1).fetchUserJobInfoList(project, 1, 1);

        /* test (H) */
        assertUserJobInfo(jobInfoListG).
            hasEntries(1).
            hasJobInfoFor(sechubJobUUD3,0).
                withExecutionResult("NONE").
                withOneOfAllowedExecutionStates("INITIALIZING");

        /* execute (H) - use size 1 - page 2 */
        TestSecHubJobInfoForUserListPage jobInfoListH = as(USER_1).fetchUserJobInfoList(project, 1, 2);

        /* test (H) */
        assertUserJobInfo(jobInfoListH).
            hasEntries(1).
            hasTotalPages(4).
            hasJobInfoFor(sechubJobUUD2,0);


        /* execute (I) - use size 1 - page 200 (will be reduced to default max: 100, but still not existing) */
        TestSecHubJobInfoForUserListPage jobInfoListI = as(USER_1).fetchUserJobInfoList(project, 1, 200);

        /* test (H) */
        assertUserJobInfo(jobInfoListI).
            hasPage(100). // reduced to default max value
            hasTotalPages(4).
            hasEntries(0);

        /* execute (J) - use size 10, but filter for labels with stage:testing */
        TestSecHubJobInfoForUserListPage jobInfoListJ = as(USER_1).fetchUserJobInfoList(project, 10, 0, true, Collections.singletonMap("metadata.labels.stage","testing"));

        /* test (C) */
        assertUserJobInfo(jobInfoListJ).
            hasEntries(2).
            hasPage(0).
            hasTotalPages(1).
            hasJobInfoFor(sechubJobUUD3,0).withLabel("stage","testing").withLabel("purpose", "security assurance").withLabel("reviewer","senior-security-expert-A").
            and().
            hasJobInfoFor(sechubJobUUD2,1).withLabel("stage","testing").withLabel("purpose", "quality assurance")
            ;
    }
    /* @formatter:on */

}