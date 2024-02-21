package com.mercedesbenz.sechub.integrationtest.scenario22;

// TODO prepare
// this scenario needs to be refactored either to 29 or to a scenario before

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.PROFILE_28_PDS_PREPARE_MOCK;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.*;

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
 *  - has execution {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCK profile 100}, {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCK profile 100} assigned
 *
 * USER_1, is automatically registered, created and assigned to PROJECT_1
 * </pre>
 *
 * @author Laura Bottner
 *
 */
public class Scenario22 extends AbstractGrowingSecHubServerTestScenario implements PDSTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario22.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} +
     * {@link IntegrationTestDefaultProfiles#PROFILE_28_PDS_PREPARE_MOCK profile 28}
     * prepare assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario22.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
                createUser(USER_1).

                createProject(PROJECT_1, USER_1).
                addProjectIdsToDefaultExecutionProfile(PROFILE_28_PDS_PREPARE_MOCK,PROJECT_1).
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
