// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario22;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 22</h3>
 * <h4>Short description</h4> A PDS integration test scenario for testing PDS
 * prepare integration.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
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
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCKED_SUCCESS profile 28}
 *
 * PROJECT_2
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCKED_SUCCESS profile 28}, {@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST profile 12} assigned
 *
 * PROJECT_3
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_29_PDS_PREPARE_FAILING profile 29 (failing preparation)}, {@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST profile 12} assigned
 *
 * PROJECT_4
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_30_PDS_PREPARE_SCRIPT_EXIT_5 profile 30 (exit 5 preparation)}, {@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST profile 12} assigned
 *
 * PROJECT_5
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_31_PDS_PREPARE_SCAN_CONFIG_SUCCESS profile 31} assigned
 *
 * PROJECT_6
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_32_PDS_PREPARE_SCAN_CONFIG_SUCCESS profile 32}, {@link IntegrationTestDefaultProfiles#PROFILE_2_PDS_CODESCAN profile 2} assigned
 *  - Info: The executor configuration has `pds.add.scriptlog.to.pdslog.enabled=true` here, so log output in PDS script is given back to PDS log automatically
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1, PROJECT_2 and PROJECT_3
 *
 * </pre>
 *
 * @author Laura Bottner
 * @author Albert Tregnaghi
 *
 */
public class Scenario22 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario22.class, "user1");

    /**
     * User 2 is registered on startup, also owner and user of {@link #PROJECT_6}
     */
    public static final TestUser USER_2 = createTestUser(Scenario22.class, "user2");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCKED_SUCCESS
     * profile 28 (succesful preparation)} prepare assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario22.class, "project1");

    /**
     * Project 2 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCKED_SUCCESS
     * profile 28 (succesful preparation)}} and
     * {@link IntegrationTestDefaultProfiles#PROFILE_1 profile 12 (checkmarx)}
     * prepare assigned
     */
    public static final TestProject PROJECT_2 = createTestProject(Scenario22.class, "project2");

    /**
     * Project 3 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_29_PDS_PREPARE_FAILING profile
     * 29 (failing preparation)} and {@link IntegrationTestDefaultProfiles#PROFILE_1
     * profile 12 (checkmarx)} prepare assigned
     */
    public static final TestProject PROJECT_3 = createTestProject(Scenario22.class, "project3");

    /**
     * Project 4 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_30_PDS_PREPARE_EXIT_5 profile
     * 30 (exit 5 preparation)} and {@link IntegrationTestDefaultProfiles#PROFILE_1
     * profile 12 (checkmarx)} prepare assigned
     */
    public static final TestProject PROJECT_4 = createTestProject(Scenario22.class, "project4");

    //
    /**
     * Project 5 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_31_PDS_PREPARE_SCAN_CONFIG_SUCCESS
     * profile 30 (exit 5 preparation)} assigned
     */
    public static final TestProject PROJECT_5 = createTestProject(Scenario22.class, "project5");

    //
    /**
     * Project 6 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_32_PDS_PREPARE_SCAN_CONFIG_SUCCESS
     * profile 32 (preparation with prepare wrapper)} and also
     * {@link IntegrationTestDefaultProfiles#PROFILE_2_PDS_CODESCAN profile2 (pds
     * codescan) assigned
     */
    public static final TestProject PROJECT_6 = createTestProject(Scenario22.class, "project6");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
                createUser(USER_1).
                createUser(USER_2).

                createProject(PROJECT_1, USER_1).
                addProjectIdsToDefaultExecutionProfile(PROFILE_28_PDS_PREPARE_MOCKED_SUCCESS, PROJECT_1).
                assignUserToProject(PROJECT_1,USER_1).

                createProject(PROJECT_2, USER_1).
                addProjectIdsToDefaultExecutionProfile(PROFILE_28_PDS_PREPARE_MOCKED_SUCCESS, PROJECT_2).
                addProjectIdsToDefaultExecutionProfile(PROFILE_1, PROJECT_2).
                assignUserToProject(PROJECT_2, USER_1).

                createProject(PROJECT_3, USER_1).
                addProjectIdsToDefaultExecutionProfile(PROFILE_29_PDS_PREPARE_FAILING, PROJECT_3).
                addProjectIdsToDefaultExecutionProfile(PROFILE_1, PROJECT_3).
                assignUserToProject(PROJECT_3, USER_1).

                createProject(PROJECT_4, USER_1).
                addProjectIdsToDefaultExecutionProfile(PROFILE_30_PDS_PREPARE_EXIT_5, PROJECT_4).
                addProjectIdsToDefaultExecutionProfile(PROFILE_1, PROJECT_4).
                assignUserToProject(PROJECT_4, USER_1).

                createProject(PROJECT_5, USER_1).
                addProjectIdsToDefaultExecutionProfile(PROFILE_31_PDS_PREPARE_SCAN_CONFIG_SUCCESS, PROJECT_5).
                assignUserToProject(PROJECT_5, USER_1).

                createProject(PROJECT_6, USER_2).
                addProjectIdsToDefaultExecutionProfile(PROFILE_32_PDS_PREPARE_SCAN_CONFIG_SUCCESS, PROJECT_6).
                addProjectIdsToDefaultExecutionProfile(PROFILE_2_PDS_CODESCAN, PROJECT_6).
                assignUserToProject(PROJECT_6, USER_2);
        ;
        /* @formatter:on */
    }

    @Override
    protected void waitForTestDataAvailable() {
        /* @formatter:off */
        initializer().
                waitUntilProjectExists(PROJECT_1).
                waitUntilProjectExists(PROJECT_2).
                waitUntilProjectExists(PROJECT_3).

                waitUntilUserExists(USER_1).

                waitUntilUserCanLogin(USER_1)
        ;
        /* @formatter:on */
    }
}
