// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario4;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.StaticTestScenario;

/**
 * <h3>Scenario 4</h3>
 * <h4>Short description</h4> A static integration test scenario ready to use
 * for job execution.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link StaticTestScenario}.
 *
 * <ul>
 * <li><b>DO NOT CHANGE ANY DATA here</b></li>
 * <li>Use this scenario for doing scans etc.</li>
 * </ul>
 * In this scenario following is automatically <b>ONE TIME</b> initialized:
 *
 * <pre>
 * PROJECT_1_ is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_1 profile 1} assigned
 *
 * USER_1, is automatically registered, created and assigned to project1
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario4 extends AbstractSecHubServerTestScenario implements StaticTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario4.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario4.class, "project1");

    private static boolean initialized;

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
		initializer().
			createUser(USER_1).
			createProject(PROJECT_1, USER_1).
			assignUserToProject(PROJECT_1,USER_1).
			addProjectIdsToDefaultExecutionProfile(PROFILE_1, PROJECT_1)
			;
		/* @formatter:on */
    }

    public boolean isInitializationNecessary() {
        return !initialized;
    }

    @Override
    protected void waitForTestDataAvailable() {
        /* @formatter:off */
		initializer().
			waitUntilProjectExists(PROJECT_1).

			waitUntilUserExists(USER_1).

			waitUntilUserCanLogin(USER_1)

			;

		initialized=true;
		/* @formatter:on */
    }

}
