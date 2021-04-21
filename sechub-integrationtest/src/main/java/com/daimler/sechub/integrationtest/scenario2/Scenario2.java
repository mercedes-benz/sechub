// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;

/**
 * In this scenario following is automatically initialized:
 *
 * <pre>
 * USER_1, is automatically registered, created and ready to go... but not assigned to any project
 * PROJECT_1_ is automatically created
 * USER_2, is automatically registered, created and ready to go... but not assigned to any project
 * PROJECT_2_ is automatically created
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario2 extends AbstractGrowingSecHubServerTestScenario {

	/**
	 * Owner 1 is registered on startup and is default owner for created projects
	 */
	static final TestUser OWNER_1 = createTestUser(Scenario2.class, "owner1");

	/**
	 * User 1 is registered on startup
	 */
	static final TestUser USER_1 = createTestUser(Scenario2.class, "user1");

	/**
	 * User 2 is registered on startup
	 */
	static final TestUser USER_2 = createTestUser(Scenario2.class, "user2");

	/**
	 * Project 1 is created on startup, but has no users
	 */
	static final TestProject PROJECT_1 = createTestProject(Scenario2.class, "project1");



	/**
	 * Project 2 is created on startup, but has no users
	 */
	public static final TestProject PROJECT_2 = createTestProject(Scenario2.class, "project2");

	/**
	 * Project 3 is created on startup, but has no users and also NO whitelist!
	 */
	public static final TestProject PROJECT_3 = createTestProject(Scenario2.class, "project3",false);

	@Override
	protected void initializeTestData() {
		/* @formatter:off */
		initializer().
		    ensureDefaultExecutionProfile_1().
			createUser(OWNER_1).
			createProject(PROJECT_1, OWNER_1).
			createProject(PROJECT_2, OWNER_1).
			createProject(PROJECT_3, OWNER_1).
			addProjectIdsToDefaultExecutionProfile_1(PROJECT_1,PROJECT_2,PROJECT_3).
			createUser(USER_1).
			createUser(USER_2)
			;
		/* @formatter:on */
	}

	@Override
	protected void waitForTestDataAvailable() {
		/* @formatter:off */
		initializer().
			waitUntilProjectExists(PROJECT_1).
			waitUntilProjectExists(PROJECT_2).

			waitUntilUserExists(USER_1).
			waitUntilUserExists(USER_2).

			waitUntilUserCanLogin(USER_1).
			waitUntilUserCanLogin(USER_2);
			;
		/* @formatter:on */
	}

    @Override
    public String getPrefixMainId() {
        return "s02";
    }
	
	

}
