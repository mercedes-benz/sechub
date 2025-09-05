// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario23;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.PROFILE_34_PDS_SOLUTION_INFRALIGHT_MOCKED;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

public class Scenario23 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario23.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario23.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_34_PDS_SOLUTION_INFRALIGHT_MOCKED, PROJECT_1)
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
