// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario5;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 5</h3>
 * <h4>Short description</h4> A PDS integration test scenario with multiple
 * profiles
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * In this scenario following is automatically initialized at start: <br>
 * <br>
 * a) <b> PDS integration test configuration is done automatically!</b> All
 * configurations from
 * <code>sechub-integrationtest/src/main/resources/pds-config-integrationtest.json</code>
 * will be configured automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_2_PDS_CODESCAN profile 2} assigned
 *
 * PROJECT_2
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1 profile 6} assigned
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1 and PROJECT_2
 * </pre>
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario5 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario5.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} + Profile 2 (PDS
     * script, no SARIF) assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario5.class, "project1");

    /**
     * Project 2 is created on startup, and has {@link #USER_1} + Profile 6 (always
     * failing PDS bash script) assigned
     */
    public static final TestProject PROJECT_2 = createTestProject(Scenario5.class, "project2");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_2_PDS_CODESCAN,PROJECT_1).
            assignUserToProject(PROJECT_1,USER_1).

            createProject(PROJECT_2, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1, PROJECT_2).
            assignUserToProject(PROJECT_2, USER_1)
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

            waitUntilUserCanLogin(USER_1)

            ;
        /* @formatter:on */
    }
}