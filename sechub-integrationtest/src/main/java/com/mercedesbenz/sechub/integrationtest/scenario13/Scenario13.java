// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario13;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.PROFILE_9_PDS_LICENSESCAN_SPDX;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.CleanScenario;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

public class Scenario13 extends AbstractSecHubServerTestScenario implements PDSTestScenario, CleanScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario13.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario13.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_9_PDS_LICENSESCAN_SPDX, PROJECT_1).
            assignUserToProject(PROJECT_1, USER_1)
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
