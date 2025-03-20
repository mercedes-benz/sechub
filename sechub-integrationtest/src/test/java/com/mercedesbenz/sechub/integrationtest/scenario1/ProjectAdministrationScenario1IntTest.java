// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario1.Scenario1.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario1.class)
public class ProjectAdministrationScenario1IntTest {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Project create ...........................+ */
    /* +-----------------------------------------------------------------------+ */

    @Test
    void a_superadmin_is_able_to_create_a_project() {
        assertUser(SUPER_ADMIN).canCreateProject(PROJECT_1, OWNER_1);
    }

    @Test
    void on_project_creation_and_owner_gains_role_and_also_automatically_access_to_project() {
        /* @formatter:off */
		/* check preconditions */
		assertUser(OWNER_1).
			hasUserRole().// every created user has role user - except when deactivated
			hasNotOwnerRole();// at this time the user is NOT assigned to a project nor owner

		/* execute */
		assertUser(SUPER_ADMIN).
			canCreateProject(PROJECT_1,OWNER_1);

		/* test */
		assertUser(OWNER_1).
			hasUserRole().
			hasOwnerRole().
			isOwnerOf(PROJECT_1).
			isAssignedToProject(PROJECT_1); // means has directly access to project without any extra action by administrator
		/* @formatter:on */
    }

    @Test
    void a_user_is_not_able_to_create_a_project() {
        assertUser(ONLY_USER).cannotCreateProject(PROJECT_1, OWNER_1.getUserId(), HttpStatus.FORBIDDEN);
    }

    @Test
    void anynomous_is_not_able_to_create_a_project() {
        assertUser(ANONYMOUS).cannotCreateProject(PROJECT_1, OWNER_1.getUserId(), HttpStatus.UNAUTHORIZED);
    }

}
