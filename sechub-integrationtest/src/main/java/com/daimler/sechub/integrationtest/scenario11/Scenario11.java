// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario11;

import static com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractSecHubServerTestScenario;
import com.daimler.sechub.integrationtest.internal.CleanScenario;
import com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.daimler.sechub.integrationtest.internal.PDSTestScenario;
import com.daimler.sechub.integrationtest.scenario5.Scenario5;

/**
 * <b><u>Scenario5 - the PDS integration test standard scenario. Scenario9
 * (REUSE SECHUB DATA enabled!)</u></b><br>
 *
 * In this scenario following is automatically initialized at start (old data
 * removed as well): <br>
 * <br>
 * a) <b> PDS integration test configuration is done automatically!</b> All
 * configurations from
 * 'sechub-integrationtest/src/main/resources/pds-config-integrationtest.json'
 * will be configured automatically!<br>
 * <br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1_ is automatically created
 * USER_1, is automatically registered, created and assigned to project1
 * </pre>
 *
 * c) The error and input streams will contain additional data. Those
 * information will be laziliy available. See
 * {@link IntegrationTestDefaultProfiles#PROFILE_5_PDS_CODESCAN_LAZY_STREAMS}
 * for details.
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario11 extends AbstractSecHubServerTestScenario implements PDSTestScenario, CleanScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario5.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario5.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_5_PDS_CODESCAN_LAZY_STREAMS,PROJECT_1).
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