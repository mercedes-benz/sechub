// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;

/**
 *
 * <h3>Scenario 1</h3>
 * <h4>Short description</h4> A very simple integration test scenario. Most
 * parts only available as constants but not really created.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * In this scenario nearly nothing of the constants is existing, except
 * "OWNER_1" which is necessary for project creation tests
 *
 * <pre>
 * USER_1, not in system. must self register him/herself.
 * PROJECT_1_ not created
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario1 extends AbstractGrowingSecHubServerTestScenario {

    /**
     * A test user which will LATER represents an owner. Its already created.
     */
    static final TestUser OWNER_1 = createTestUser(Scenario1.class, "owner1");

    /**
     * A test user - but must be created later. so not existing
     */
    static final TestUser USER_1 = createTestUser(Scenario1.class, "user1");

    /**
     * A test project - but must be created later. so not existing
     */
    static final TestProject PROJECT_1 = createTestProject(Scenario1.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
        initializer().
            createUser(OWNER_1).
            addProjectIdsToDefaultExecutionProfile(PROFILE_1, PROJECT_1);
        /* @formatter:on */

    }

    @Override
    protected void waitForTestDataAvailable() {
        initializer().waitUntilUserCanLogin(OWNER_1);
    }

}
