// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario5;

import static com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractSecHubServerTestScenario;
import com.daimler.sechub.integrationtest.internal.CleanScenario;
import com.daimler.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <b><u>Scenario5 - the PDS integration test standard scenario.(REUSE SECHUB DATA enabled!)</u></b><br>
 * 
 * In this scenario following is automatically initialized at start (old data
 * removed as well): <br> <br>
 * a) <b> PDS integration test configuration is done automatically!</b> 
 * All configurations from
 * 'sechub-integrationtest/src/main/resources/pds-config-integrationtest.json' will be
 * configured automatically!<br><br>
 * b) User and project data:
 *
 * <pre>
 * PROJECT_1_ is automatically created (assigned to profile 2)
 * PROJECT_2_ is automatically created (assigned to profile 6)
 * USER_1, is automatically registered, created and assigned to project1 and project2
 * </pre>
 * 
 * 
 * @author Albert Tregnaghi
 *
 */
public class Scenario5 extends AbstractSecHubServerTestScenario implements PDSTestScenario, CleanScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario5.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} + Profile 2 (PDS script, no SARIF) assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario5.class, "project1");
    
    /**
     * Project 2 is created on startup, and has {@link #USER_1} + Profile 6 (always failing PDS bash script) assigned
     */
    public static final TestProject PROJECT_2 = createTestProject(Scenario5.class, "project2");

    
    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            ensureDefaultExecutionProfile(PROFILE_2_PDS_CODESCAN).
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_2_PDS_CODESCAN,PROJECT_1).
            assignUserToProject(PROJECT_1,USER_1).
            
            ensureDefaultExecutionProfile(PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1).
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