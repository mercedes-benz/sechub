// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

public class ProjectAdministrationScenario1IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Project create ...........................+ */
	/* +-----------------------------------------------------------------------+ */

	@Test
	public void a_superadmin_is_able_to_create_a_project() {
		assertUser(SUPER_ADMIN).canCreateProject(Scenario1.PROJECT_1, Scenario1.OWNER_1.getUserId());
	}

	@Test
	public void a_superadmin_is_able_to_create_a_project_and_owner_can_be_added_as_user_and_is_user_and_owner_role() {
		/* @formatter:off */
		/* check preconditions */
		assertUser(Scenario1.OWNER_1).
			hasUserRole().// every created user has role user - except when deactivated
			hasNotOwnerRole();// at this time the user is NOT assigned to a project nor owner

		/* execute */
		assertUser(SUPER_ADMIN).
			canCreateProject(Scenario1.PROJECT_1,Scenario1.OWNER_1.getUserId()).
			canAssignUserToProject(Scenario1.OWNER_1, Scenario1.PROJECT_1);
		/* test */
		assertUser(Scenario1.OWNER_1).
			hasUserRole().
			hasOwnerRole().
			isOwnerOf(Scenario1.PROJECT_1);
		/* @formatter:on */
	}

	@Test
	public void a_user_is_not_able_to_create_a_project() {
		assertUser(ONLY_USER).cannotCreateProject(Scenario1.PROJECT_1, Scenario1.OWNER_1.getUserId(), HttpStatus.FORBIDDEN);
	}

	@Test
	public void anynomous_is_not_able_to_create_a_project() {
		assertUser(ANONYMOUS).cannotCreateProject(Scenario1.PROJECT_1, Scenario1.OWNER_1.getUserId(), HttpStatus.UNAUTHORIZED);
	}

}
