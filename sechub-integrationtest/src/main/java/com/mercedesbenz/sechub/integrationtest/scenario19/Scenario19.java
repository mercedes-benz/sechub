// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario19;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;

/**
 * <h3>Scenario 19</h3>
 *
 * <h4>Short description</h4> A simple integration test scenario to test user
 * job information list fetching, one user, one project, but no execution
 * profiles.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * In this scenario following is automatically initialized:
 *
 * <pre>
 * USER_1, is automatically registered, created and owner of PROJECT_1
 *
 * PROJECT_1
 *      - is automatically created
 *      - has USER_1 as owner
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario19 extends AbstractGrowingSecHubServerTestScenario {

    /**
     * User 1 is registered on startup
     */
    static final TestUser USER_1 = createTestUser(Scenario19.class, "user1");

    /**
     * Project 1 is created on startup, but has no users
     */
    static final TestProject PROJECT_1 = createTestProject(Scenario19.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
		initializer().
			createUser(USER_1).
			createProject(PROJECT_1, USER_1)
			;
		/* @formatter:on */
    }

    @Override
    protected void waitForTestDataAvailable() {
        /* @formatter:off */
		initializer().
			waitUntilProjectExists(PROJECT_1).
			waitUntilUserExists(USER_1)
			;
		/* @formatter:on */
    }

}
