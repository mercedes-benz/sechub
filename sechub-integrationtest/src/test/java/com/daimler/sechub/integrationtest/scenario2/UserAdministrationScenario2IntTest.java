// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.AssertMail.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestUser;
public class UserAdministrationScenario2IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);
	/* +-----------------------------------------------------------------------+ */
	/* +............................ User ......... ...........................+ */
	/* +-----------------------------------------------------------------------+ */
	/* @formatter:off */
	@Test
	public void superadmin_can_grant_user_admin_rights_mails_are_sent_and_user_appears_as_expected_then() {
		TestUser adminUser = USER_1;

		/* execute grant +test */
		assertUser(SUPER_ADMIN).canGrantSuperAdminRightsTo(adminUser);
		/* test behavior*/
		assertUser(adminUser).
			isSuperAdmin().
			isInSuperAdminList();
		/* test notifications */
		assertUser(adminUser).hasReceivedEmail("Sechub administrator privileges granted");
		assertMailExists("int-test_superadmins_npm@example.org", "SecHub: Granted administrator rights to.*");
	}
	/* @formatter:on */


	@Test
	public void anynmouse_can_NOT_grant_user_admin_rights() {
		assertUser(ANONYMOUS).canNotGrantSuperAdminRightsTo(USER_2, HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void user1_can_NOT_grant_user_admin_rights() {
		assertUser(USER_1).canNotGrantSuperAdminRightsTo(USER_2, HttpStatus.FORBIDDEN);
	}

	@Test
	public void anynmouse_can_NOT_revoke_user_admin_rights() {
		/* prepare */
		TestUser adminUser = USER_2;
		as(SUPER_ADMIN).grantSuperAdminRightsTo(adminUser);
		assertUser(adminUser).isSuperAdmin();

		/* execute + test*/
		assertUser(ANONYMOUS).canNotRevokeSuperAdminRightsFrom(adminUser, HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void user1_can_NOT_revoke_user_admin_rights() {
		/* prepare */
		TestUser adminUser = USER_2;
		as(SUPER_ADMIN).grantSuperAdminRightsTo(adminUser);
		assertUser(adminUser).isSuperAdmin();

		/* execute + test*/
		assertUser(USER_1).canNotRevokeSuperAdminRightsFrom(adminUser, HttpStatus.FORBIDDEN);
	}

	/* @formatter:off */
	@Test
	public void superadmin_can_revoke_user_admin_rights() {
		/* prepare */
		TestUser adminUser = USER_2;
		as(SUPER_ADMIN).grantSuperAdminRightsTo(adminUser);
		assertUser(adminUser).isSuperAdmin();

		/* execute + test */
		assertUser(SUPER_ADMIN).canRevokeSuperAdminRightsTo(adminUser);
		/* test behavior*/
		assertUser(adminUser).
			isNotSuperAdmin().
			isNotInSuperAdminList();

		/* test notifications */
		assertUser(adminUser).hasReceivedEmail("Sechub administrator privileges revoked");
		assertMailExists("int-test_superadmins_npm@example.org", "SecHub: Revoked administrator rights from.*");
	}
	/* @formatter:on */


}
