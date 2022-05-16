// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario13;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <b><u>Scenario13 - the PDS integration test SPDX license scan scenario (REUSE
 * SECHUB DATA enabled)</u></b><br>
 *
 * The scenario13 does setup SecHub and PDS to a state which allows testing the
 * SPDX license scan.
 *
 * In this scenario, the following is automatically initialized at start (old
 * data removed): <br>
 * <br>
 * a) <b> PDS integration test configuration is done automatically.</b> All
 * configurations from
 * 'sechub-integrationtest/src/main/resources/pds-config-integrationtest.json'
 * are used to setup the PDS automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1_ is automatically created
 * USER_1, is automatically registered, created and assigned to project1
 * </pre>
 *
 */
public class Scenario13 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

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
