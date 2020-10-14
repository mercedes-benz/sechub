// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario7;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;

/**
 *  We use this scenario just for product executor runtime configuration testing. 
 * In this scenario following is automatically initialized.
 *
 * <pre>
 * USER_1, is automatically registrated, created and ready to go... but not assigned to any project
 * PROJECT_1_ is automatically created
 * OWNER_1, is automatically registrated, created and ready to go... and owner of project1
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario7 extends AbstractGrowingSecHubServerTestScenario {

	/**
	 * Owner 1 is registered on startup and is default owner for created projects
	 */
	static final TestUser OWNER_1 = createTestUser(Scenario7.class, "owner1");

	/**
	 * User 1 is registered on startup
	 */
	static final TestUser USER_1 = createTestUser(Scenario7.class, "user1");

	/**
	 * Project 1 is created on startup, but has no users
	 */
	static final TestProject PROJECT_1 = createTestProject(Scenario7.class, "project1");

	@Override
	protected void initializeTestData() {
		/* @formatter:off */
		initializer().
			createUser(OWNER_1).
			createProject(PROJECT_1, OWNER_1).
			createUser(USER_1);
			;
		/* @formatter:on */
	}

	@Override
	protected void waitForTestDataAvailable() {
		/* @formatter:off */
		initializer().
			waitUntilProjectExists(PROJECT_1).

			waitUntilUserExists(USER_1).

			waitUntilUserCanLogin(USER_1)
			;
		/* @formatter:on */
	}

    @Override
    public String getPrefixMainId() {
        return "s07";
    }

}
