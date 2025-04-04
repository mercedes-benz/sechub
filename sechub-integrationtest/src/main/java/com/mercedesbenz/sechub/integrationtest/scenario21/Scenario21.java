// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario21;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 21</h3>
 *
 * <h4>Short description</h4> PDS solutions mock scenario for multiple PDS
 * solutions
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * PDS integration test scan scenario. Reuse of SecHub storage is enabled.<br>
 *
 * The scenario does setup SecHub and PDS to a state which allows testing
 * different pds solution mock data results.
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
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_18_PDS_SOLUTION_GOSEC_MOCKED profile 18} assigned
 * - has USER_1 as owner
 *
 * PROJECT_2 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_19_PDS_SOLUTION_CHECKMARX_MOCK_MODE profile 19} assigned
 *- has USER_1 as owner
 *
 * PROJECT_3 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_20_PDS_SOLUTION_MULTI_BANDIT_MOCKED profile 20} assigned
 *- has USER_1 as owner
 *
 * PROJECT_4 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_21_PDS_SOLUTION_ZAP_MOCKED profile 21} assigned
 *- has USER_1 as owner
 *
 * PROJECT_5 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_22_PDS_SOLUTION_SCANCODE_SPDX_JSON_MOCKED profile 22} assigned
 *- has USER_1 as owner
 *
 * PROJECT_6 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_23_PDS_SOLUTION_GITLEAKS_MOCKED profile 23} assigned
 * - has USER_1 as owner
 *
 * PROJECT_7 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_24_PDS_SOLUTION_TERN_MOCKED profile 24} assigned
 * - has USER_1 as owner
 *
 * PROJECT_8 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_25_PDS_SOLUTION_XRAY_SPDX_MOCKED profile 25} assigned
 * - has USER_1 as owner
 *
 * PROJECT_9 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_26_PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED profile 26} assigned
 * - has USER_1 as owner
 *
 * PROJECT_10 is automatically created
 * - has execution {@link IntegrationTestDefaultProfiles#PROFILE_27_PDS_SOLUTION_FINDSECURITYBUGS_MOCKED profile 27} assigned
 * - has USER_1 as owner
 *
 * USER_1, is automatically registered, created and owner of all projects inside this scenario
 * </pre>
 *
 */
public class Scenario21 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario21.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_18_PDS_SOLUTION_GOSEC_MOCK_MODE
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario21.class, "project1");
    /**
     * Project 2 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_19_PDS_SOLUTION_CHECKMARX_MOCK_MODE
     */
    public static final TestProject PROJECT_2 = createTestProject(Scenario21.class, "project2");
    /**
     * Project 3 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_20_PDS_SOLUTION_MULTI_BANDIT_MOCK_MODE
     */
    public static final TestProject PROJECT_3 = createTestProject(Scenario21.class, "project3");
    /**
     * Project 4 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_21_PDS_SOLUTION_ZAP_MOCK_MODE
     */
    public static final TestProject PROJECT_4 = createTestProject(Scenario21.class, "project4");
    /**
     * Project 5 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_22_PDS_SOLUTION_SCANCODE_MOCK_MODE
     */
    public static final TestProject PROJECT_5 = createTestProject(Scenario21.class, "project5");
    /**
     * Project 6 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_23_PDS_SOLUTION_GITLEAKS_MOCK_MODE
     */
    public static final TestProject PROJECT_6 = createTestProject(Scenario21.class, "project6");

    /**
     * Project 7 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_24_PDS_SOLUTION_TERN_MOCKED
     */
    public static final TestProject PROJECT_7 = createTestProject(Scenario21.class, "project7");

    /**
     * Project 8 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_25_PDS_SOLUTION_XRAY_SPDX_MOCKED
     */
    public static final TestProject PROJECT_8 = createTestProject(Scenario21.class, "project8");

    /**
     * Project 9 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_26_PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED
     */
    public static final TestProject PROJECT_9 = createTestProject(Scenario21.class, "project9");

    /**
     * Project 10 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_27_PDS_SOLUTION_FINDSECURITYBUGS_MOCKED
     */
    public static final TestProject PROJECT_10 = createTestProject(Scenario21.class, "project10");

    /**
     * Project 11 is created on startup, and has {@link #USER_1} assigned. Profile
     * used = PROFILE_33_PDS_SOLUTION_KICS_MOCKED
     */
    public static final TestProject PROJECT_11 = createTestProject(Scenario21.class, "project11");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).
            createProject(PROJECT_1, USER_1).
            createProject(PROJECT_2, USER_1).
            createProject(PROJECT_3, USER_1).
            createProject(PROJECT_4, USER_1).
            createProject(PROJECT_5, USER_1).
            createProject(PROJECT_6, USER_1).
            createProject(PROJECT_7, USER_1).
            createProject(PROJECT_8, USER_1).
            createProject(PROJECT_9, USER_1).
            createProject(PROJECT_10, USER_1).
            createProject(PROJECT_11, USER_1).

            addProjectIdsToDefaultExecutionProfile(PROFILE_18_PDS_SOLUTION_GOSEC_MOCKED, PROJECT_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_19_PDS_SOLUTION_CHECKMARX_MOCK_MODE, PROJECT_2).
            addProjectIdsToDefaultExecutionProfile(PROFILE_20_PDS_SOLUTION_MULTI_BANDIT_MOCKED, PROJECT_3).
            addProjectIdsToDefaultExecutionProfile(PROFILE_21_PDS_SOLUTION_ZAP_MOCKED, PROJECT_4).
            addProjectIdsToDefaultExecutionProfile(PROFILE_22_PDS_SOLUTION_SCANCODE_SPDX_JSON_MOCKED, PROJECT_5).
            addProjectIdsToDefaultExecutionProfile(PROFILE_23_PDS_SOLUTION_GITLEAKS_MOCKED, PROJECT_6).
            addProjectIdsToDefaultExecutionProfile(PROFILE_24_PDS_SOLUTION_TERN_MOCKED, PROJECT_7).
            addProjectIdsToDefaultExecutionProfile(PROFILE_25_PDS_SOLUTION_XRAY_SPDX_MOCKED, PROJECT_8).
            addProjectIdsToDefaultExecutionProfile(PROFILE_26_PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED, PROJECT_9).
            addProjectIdsToDefaultExecutionProfile(PROFILE_27_PDS_SOLUTION_FINDSECURITYBUGS_MOCKED, PROJECT_10).
            addProjectIdsToDefaultExecutionProfile(PROFILE_33_PDS_SOLUTION_KICS_MOCKED, PROJECT_11)

            ;
        /* @formatter:on */
    }

    @Override
    protected void waitForTestDataAvailable() {
        /* @formatter:off */
        initializer().
            waitUntilProjectExists(PROJECT_10).

            waitUntilUserExists(USER_1).

            waitUntilUserCanLogin(USER_1)

            ;
        /* @formatter:on */
    }

}
