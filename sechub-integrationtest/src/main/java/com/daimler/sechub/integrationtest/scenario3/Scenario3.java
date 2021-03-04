// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;

/**
 * In this scenario following is automatically initialized at start (old data
 * removed as well)
 *
 * <pre>
 * PROJECT_1_ is automatically created
 * USER_1, is automatically registered, created and assigned to project1
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario3 extends AbstractGrowingSecHubServerTestScenario {

    public static final String PREFIX_MAIN_ID = "s03";

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario3.class, "user1");
    public static final TestUser USER_2 = createTestUser(Scenario3.class, "user2");
    public static final TestUser USER_3 = createTestUser(Scenario3.class, "user3");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario3.class, "project1");
    public static final TestProject PROJECT_2 = createTestProject(Scenario3.class, "project2");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
		initializer().
		    ensureDefaultExecutionProfile_1().
			createUser(USER_1).
			createUser(USER_2).
			createUser(USER_3).
			createProject(PROJECT_1, USER_1).
			createProject(PROJECT_2, USER_1).
			assignUserToProject(PROJECT_1, USER_1).
			addProjectIdsToDefaultExecutionProfile_1(PROJECT_1)
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
        return PREFIX_MAIN_ID;
    }

}
