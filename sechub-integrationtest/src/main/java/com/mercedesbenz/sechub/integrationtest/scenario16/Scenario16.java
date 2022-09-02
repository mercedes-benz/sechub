// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario16;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 16</h3>
 * <h4>Short description</h4> A PDS integration test scenario for testing
 * mappingt transfer from SecHub central mapping to PDS jobs at runtime
 *
 * <h4>Overview</h4> For a glance over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * The tests inside the scenario use following default mappings:
 *
 * <ul>
 * <li>{@value IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1}</li>
 * <li>{@value IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB}
 * (referenced, but not existing)</li>
 * </ul>
 *
 * In this scenario following is automatically initialized at start: <br>
 * <br>
 * a) <b>PDS integration test configuration is done automatically!</b> All
 * configurations from
 * <code>sechub-integrationtest/src/main/resources/pds-config-integrationtest.json</code>
 * will be configured automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_11_PDS_CODESCAN_MAPPING profile 11} assigned
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario16 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario16.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} + Profile 2 (PDS
     * script, no SARIF) assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario16.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_11_PDS_CODESCAN_MAPPING,PROJECT_1).
            assignUserToProject(PROJECT_1,USER_1)
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