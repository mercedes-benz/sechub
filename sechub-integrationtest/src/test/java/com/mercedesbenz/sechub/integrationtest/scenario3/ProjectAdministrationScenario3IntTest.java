// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TextSearchMode;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario3.class)
public class ProjectAdministrationScenario3IntTest {

    /**
     * Why is this test method testing so much? It was settled inside 4 different
     * tests which do nearly execute the same action, but with the necessary
     * execution and test time the test did run with 28 seconds! Now this single
     * test does test the same but in 12 seconds.
     */
    @Test
    /* @formatter:off */
    void super_admin_changes_owner_of_a_project() {
        /* check precondition */
        assertUser(USER_1).
            isAssignedToProject(PROJECT_1).
            isOwnerOf(PROJECT_1);

        assertUser(USER_2).
            isNotOwnerOf(PROJECT_1).
            hasNotOwnerRole();

        /* execute 1 - change project 1*/
        as(SUPER_ADMIN).
            assignUserToProject(USER_3, PROJECT_1).
            assignOwnerToProject(USER_2, PROJECT_1);

        /* test 1*/
        assertUser(USER_2).
            isOwnerOf(PROJECT_1).
            hasOwnerRole();

        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1).
            isOwnerOf(PROJECT_2).
            isAssignedToProject(PROJECT_1).
            hasOwnerRole();

        String subject = "Owner of project .* changed";

        assertUser(USER_3).hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON);
        assertUser(USER_2).hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON);
        assertUser(USER_1).hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON);

        /* execute 2 - change project 2 ownership as well - so User1 looses owner role*/
        as(SUPER_ADMIN).
            assignOwnerToProject(USER_2, PROJECT_2);

        /* test 2*/
        assertUser(USER_2).
            isOwnerOf(PROJECT_2).
            hasOwnerRole();

        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1).
            isNotOwnerOf(PROJECT_2).
            hasNotOwnerRole();

    }
    /* @formatter:on */

}
