// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;

import org.junit.Rule;
import org.junit.Test;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

public class ProjectAdministrationScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);
    
    @Test
    public void a_superadmin_can_assign_owner_to_project() {
        assertUser(SUPER_ADMIN).
            canAssignOwnerToProject(USER_2, PROJECT_1);
    }

    @Test
	public void when_a_superadmin_assigns_an_owner_to_a_project_the_owner_is_assigned() {
		/* check precondition */
        assertUser(USER_2).
            isNotOwnerOf(PROJECT_1);

		/* execute */
		as(SUPER_ADMIN).
		    assignOwnerToProject(USER_2, PROJECT_1);

		/* test */
		assertUser(USER_2).
		    isOwnerOf(PROJECT_1);
	}

    @Test
    public void when_a_superadmin_assigns_an_owner_to_a_project_the_previous_owner_is_no_longer_assigned() {
        /* prepare */
        assertUser(USER_1).
            isOwnerOf(PROJECT_1);

        /* execute */
        as(SUPER_ADMIN).
            assignOwnerToProject(USER_2, PROJECT_1);

        /* test */
        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1);
        assertUser(USER_2).
            isOwnerOf(PROJECT_1);
    }

    @Test
    public void when_a_superadmin_assigns_an_owner_to_a_project_the_previous_owner_is_still_owner_of_another_project() {
        /* prepare */
        assertUser(USER_1).
            isOwnerOf(PROJECT_1).
            isOwnerOf(PROJECT_2).
            hasOwnerRole();

        /* execute */
        as(SUPER_ADMIN).
            assignOwnerToProject(USER_2, PROJECT_1);

        /* test */
        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1).
            isOwnerOf(PROJECT_2).
            hasOwnerRole();
    }
    
    @Test
    public void when_a_superadmin_assigns_another_owner_to_all_projects_the_previous_owner_loses_owner_role() {
        /* prepare */
        assertUser(USER_1).
            isOwnerOf(PROJECT_1).
            isOwnerOf(PROJECT_2).
            hasOwnerRole();

        /* execute */
        as(SUPER_ADMIN).
            assignOwnerToProject(USER_2, PROJECT_1).
            assignOwnerToProject(USER_2, PROJECT_2);

        /* test */
        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1);
        assertUser(USER_1).
            hasNotOwnerRole();
    }
    
    @Test
    public void when_a_superadmin_assigns_another_owner_to_a_project_all_associated_users_are_mailed() {
        /* prepare */
        assertUser(USER_1).
            isOwnerOf(PROJECT_1).
            hasOwnerRole();
        
        /* execute */
        as(SUPER_ADMIN).
            assignUserToProject(USER_3, PROJECT_1).
            assignOwnerToProject(USER_2, PROJECT_1);

        /* test */
        String subject = "Owner of project .* changed";
        
        assertUser(USER_3).
            isAssignedToProject(PROJECT_1);
        assertUser(USER_1).
            isNotOwnerOf(PROJECT_1);
        
        assertUser(USER_3).
            hasReceivedEmail(subject, true);
        assertUser(USER_2).
            hasReceivedEmail(subject, true);
        assertUser(USER_1).
            hasReceivedEmail(subject, true);
    }
}
