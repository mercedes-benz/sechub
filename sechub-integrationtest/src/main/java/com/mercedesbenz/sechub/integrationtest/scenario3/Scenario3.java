// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;

/**
 * <h3>Scenario 3</h3>
 * <h4>Short description</h4> An integration test scenario with many users,
 * projects, assigned etc.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * In this scenario following is automatically initialized at start.
 *
 * <pre>
 * PROJECT_1 is automatically created
 *    - has execution {@link IntegrationTestDefaultProfiles#PROFILE_1 profile 1} assigned
 *
 * PROJECT_2 is automatically created
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1
 * USER_2, is automatically registered, and created
 * USER_3, is automatically registered, and created
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
			createUser(USER_1).
			createUser(USER_2).
			createUser(USER_3).
			createProject(PROJECT_1, USER_1).
			createProject(PROJECT_2, USER_1).
			assignUserToProject(PROJECT_1, USER_1).
			addProjectIdsToDefaultExecutionProfile(PROFILE_1, PROJECT_1)
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

}
