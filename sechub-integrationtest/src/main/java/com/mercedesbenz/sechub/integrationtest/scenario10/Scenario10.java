// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario10;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 10</h3>
 * <h4>Short description</h4> PDS integration test SARIF
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4>This is a {@link GrowingScenario}.<br>
 * PDS integrationtest SARIF scenario <b>NO reuse of sechub storage)</b><br>
 *
 * In this scenario following is automatically initialized at start (old data
 * removed as well): <br>
 * <br>
 * a) <b> PDS integrationtest configuration is done automatically - <b>but we DO
 * NOT use sechub storage here - so PDS will store at its own location pathes
 * and secub will upload sources!</b></b> All configurations from
 * <code>sechub-integrationtest/src/main/resources/pds-config-integrationtest.json</code>
 * will be configured automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1 is automatically created
 *    - has execution {@link IntegrationTestDefaultProfiles#PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF profile 4} assigned
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1
 * </pre>
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario10 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario10.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario10.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF,PROJECT_1).
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