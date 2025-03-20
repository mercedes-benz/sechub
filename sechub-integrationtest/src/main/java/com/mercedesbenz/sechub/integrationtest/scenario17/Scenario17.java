// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario17;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;

/**
 * <h3>Scenario 17</h3>
 * <h4>Short description</h4> A PDS integration test scenario for testing
 * checkmarx PDS integration.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
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
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST profile 12}, {@link IntegrationTestDefaultProfiles#PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT profile 16} assigned
 *  - has USER_1 as owner
 *
 * PROJECT_2
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_14_PDS_CHECKMARX_INTEGRATIONTEST_WRONG_WITH_SOURCE_AND_BINARY profile 14}, {@link IntegrationTestDefaultProfiles#PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT profile 16} assigned
 *  - has USER_1 as owner
 *
 * PROJECT_3
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_15_PDS_CHECKMARX_INTEGRATIONTEST_FILTERING_TEXTFILES profile 15} assigned (we do NOT use {@link IntegrationTestDefaultProfiles#PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT profile 16} here, because the scripts inside DOES NEED sources uploaded!)
 *  - has USER_1 as owner
 *
 * USER_1, is automatically registered, created and owner of PROJECT_1, PROJECT_2, PROJECT_3
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario17 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario17.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST
     * profile 12} (PDS script, no SARIF) and
     * {@link IntegrationTestDefaultProfiles#PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT
     * profile 16} (analytics cloc) assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario17.class, "project1");

    /**
     * Project 2 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_14_PDS_CHECKMARX_INTEGRATIONTEST_WRONG_WITH_SOURCE_AND_BINARY
     * profile 14} (PDS script, no SARIF, wrong custom data type setup =
     * "source,binary") and
     * {@link IntegrationTestDefaultProfiles#PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT
     * profile 16} (analytics cloc) assigned
     */
    public static final TestProject PROJECT_2 = createTestProject(Scenario17.class, "project2");

    /**
     * Project 3 is created on startup, and has {@link #USER_1} + Profile 15 (PDS
     * script, no SARIF, no custom data type setup, but with a custom file filter
     * excludes applied which will filter every text file (*.txt)
     */
    public static final TestProject PROJECT_3 = createTestProject(Scenario17.class, "project3");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(USER_1).

            createProject(PROJECT_1, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST,PROJECT_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT,PROJECT_1).

            createProject(PROJECT_2, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_14_PDS_CHECKMARX_INTEGRATIONTEST_WRONG_WITH_SOURCE_AND_BINARY, PROJECT_2).
            addProjectIdsToDefaultExecutionProfile(PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT, PROJECT_2).

            createProject(PROJECT_3, USER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_15_PDS_CHECKMARX_INTEGRATIONTEST_FILTERING_TEXTFILES, PROJECT_3)
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