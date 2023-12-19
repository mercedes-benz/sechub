// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario13;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 13</h3>
 *
 * <h4>Short description</h4> PDS license scan scenario
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * PDS integration test SPDX license scan scenario. Reuse of SecHub storage is
 * enabled.<br>
 *
 * The scenario does setup SecHub and PDS to a state which allows testing the
 * SPDX license scan.
 *
 * In this scenario, the following is automatically initialized at start (old
 * data removed): <br>
 * <br>
 * a) <b> PDS integration test configuration is done automatically.</b> All
 * configurations from
 * <code>sechub-integrationtest/src/main/resources/pds-config-integrationtest.json</code>
 * are used to setup the PDS automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_9_PDS_LICENSESCAN_SPDX profile 9} assigned
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1
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
