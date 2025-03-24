// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TextSearchMode;

public class ProjectAdministrationScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    /**
     * Why is this test method testing so much? It was settled inside 4 different
     * tests which do nearly execute the same action, but with the necessary
     * execution and test time the test did run with 28 seconds! Now this single
     * test does test the same but in 12 seconds.
     */
    @Test
    /* @formatter:off */
    public void change_project_ownership_by_admin_and_owners() {
        /* check precondition */
        assertUser(USER_1).
            isOwnerOf(PROJECT_1);

        assertUser(USER_2).
            isNotOwnerOf(PROJECT_1).
            hasNotOwnerRole();

        /* execute 1.1 - change project 1*/
        as(SUPER_ADMIN).
            assignUserToProject(USER_3, PROJECT_1).
            assignOwnerToProject(USER_2, PROJECT_1);

        /* test 1.1*/
        assertUser(USER_2).
            isOwnerOf(PROJECT_1).
            hasOwnerRole();

        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1).
            isOwnerOf(PROJECT_2).
            hasOwnerRole();

        String subject = "Owner of project .* changed";

        assertUser(USER_3).hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON);
        assertUser(USER_2).hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON);
        assertUser(USER_1).hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON);

        /* execute + test 1.2*/

        /* USER_1 is no longer the owner of project 1 - so not allowed to change owner ship!*/
        expectHttpFailure(()->as(USER_1).assignOwnerToProject(USER_3, PROJECT_1), HttpStatus.FORBIDDEN);

        /* execute 1.3*/
        executeResilient(()->as(USER_2).
            assignOwnerToProject(USER_3, PROJECT_1));

        /* test 1.3 - ownership has been transfered by former owner (USER_2) itself */
        assertUser(USER_3).
            isOwnerOf(PROJECT_1).
            hasOwnerRole();

        /* execute 2 - change project 2 ownership as well - so User1 looses owner role*/
        as(SUPER_ADMIN).
            assignOwnerToProject(USER_2, PROJECT_2);

        /* test 2*/
        assertUser(USER_2).
            isOwnerOf(PROJECT_2).
            isNotOwnerOf(PROJECT_1).
            hasOwnerRole();

        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1).
            isNotOwnerOf(PROJECT_2).
            hasNotOwnerRole();

    }
    /* @formatter:on */

}
