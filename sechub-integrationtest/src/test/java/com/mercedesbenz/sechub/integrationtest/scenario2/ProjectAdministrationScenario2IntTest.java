// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class ProjectAdministrationScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Project 2 User ...........................+ */
    /* +-----------------------------------------------------------------------+ */
    @Test
    public void a_superadmin_can_assign_user_to_project() {
        assertUser(SUPER_ADMIN).canAssignUserToProject(USER_1, PROJECT_1);
    }

    @Test
    public void when_a_superadmin_assigns_a_user_to_a_project_the_user_is_assigned() {
        /* check precondition */
        assertUser(USER_1).isNotAssignedToProject(PROJECT_1);

        /* execute */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* test */
        assertUser(USER_1).isAssignedToProject(PROJECT_1);
    }

    @Test
    public void when_a_superadmin_unassigns_a_user_from_a_project_the_user_is_no_longer_assigned() {
        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);
        assertUser(USER_1).isAssignedToProject(PROJECT_1);

        /* execute */
        as(OWNER_1).unassignUserFromProject(USER_1, PROJECT_1);

        /* test */
        assertUser(USER_1).isNotAssignedToProject(PROJECT_1);
    }

    @Test
    public void when_a_project_owner_assigns_a_user_to_a_project_the_user_is_assigned() {
        /* check precondition */
        assertUser(USER_1).isNotAssignedToProject(PROJECT_1);

        /* execute */
        as(OWNER_1).assignUserToProject(USER_1, PROJECT_1);

        /* test */
        assertUser(USER_1).isAssignedToProject(PROJECT_1);
    }

    @Test
    public void when_a_project_owner_unassigns_a_user_from_a_project_the_user_is_no_longer_assigned() {
        /* prepare */
        as(OWNER_1).assignUserToProject(USER_1, PROJECT_1);
        assertUser(USER_1).isAssignedToProject(PROJECT_1);

        /* execute */
        as(OWNER_1).unassignUserFromProject(USER_1, PROJECT_1);

        /* test */
        assertUser(USER_1).isNotAssignedToProject(PROJECT_1);
    }

    @Test
    public void a_normal_user_can_NOT_assign_user_to_project() {
        assertUser(ONLY_USER).canNotAssignUserToProject(USER_1, PROJECT_1, HttpStatus.FORBIDDEN);
    }

    @Test
    public void anynmouse_can_NOT_assign_user_to_project() {
        assertUser(ANONYMOUS).canNotAssignUserToProject(USER_1, PROJECT_1, HttpStatus.UNAUTHORIZED);
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Project list . ...........................+ */
    /* +-----------------------------------------------------------------------+ */
    @Test
    public void a_superadmin_is_able_to_list_a_project() {
        assertUser(SUPER_ADMIN).canAccessProjectInfo(PROJECT_1);
    }

    @Test
    public void a_normal_user_can_NOT_list_a_project() {
        assertUser(ONLY_USER).canNotListProject(PROJECT_1, HttpStatus.FORBIDDEN);
    }

    @Test
    public void anynmouse_can_NOT_list_a_project() {
        assertUser(ANONYMOUS).canNotListProject(PROJECT_1, HttpStatus.UNAUTHORIZED);
    }

}
