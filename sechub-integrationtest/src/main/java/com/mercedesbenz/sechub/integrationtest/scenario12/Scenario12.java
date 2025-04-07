// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario12;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 12</h3>
 * <h4>Short description</h4> A PDS integration test scenario.<br>
 * <br>
 * Reuses storage from Sechub.
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview} <br>
 * <be>
 * <h4>Details</h4>This is a {@link GrowingScenario}.<br>
 * PDS integration test SARIF scenario. Reuse of SecHub storage is enabled.<br>
 *
 * In this scenario following is automatically initialized at start (old data
 * removed as well): <br>
 * <br>
 * a) <b> PDS integration test configuration is done automatically!</b> All
 * configurations from
 * <code>sechub-integrationtest/src/main/resources/pds-config-integrationtest.json</code>
 * will be configured automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1_ is automatically created
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_7_PDS_WEBSCAN profile 7} assigned
 *  - has USER_1 as owner
 *
 * USER_1, is automatically registered, created and owner of PROJECT_1
 * </pre>
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario12 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario12.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario12.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_7_PDS_WEBSCAN, PROJECT_1)
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