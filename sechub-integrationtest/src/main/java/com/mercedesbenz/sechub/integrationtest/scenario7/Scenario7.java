// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario7;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;

/**
 * <h3>Scenario 7</h3>
 * <h4>Short description</h4> Only for product executor runtime configuration
 * tests.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4>This is a {@link GrowingScenario}.<br>
 * We use this scenario just for product executor runtime configuration testing
 * and no other tests. In this scenario following is automatically initialized.
 *
 * <pre>
 * USER_1, is automatically registered, created and ready to go... but not assigned to any project
 *
 * OWNER_1
 * - is automatically registrated, created and ready to go... and owner of PROJECT_1
 *
 * PROJECT_1
 * - is automatically created
 *
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario7 extends AbstractGrowingSecHubServerTestScenario {

    /**
     * Owner 1 is registered on startup and is default owner for created projects
     */
    static final TestUser OWNER_1 = createTestUser(Scenario7.class, "owner1");

    /**
     * User 1 is registered on startup
     */
    static final TestUser USER_1 = createTestUser(Scenario7.class, "user1");

    /**
     * Project 1 is created on startup, but has no users
     */
    static final TestProject PROJECT_1 = createTestProject(Scenario7.class, "project1");

    @Override
    protected void initializeTestData() {
        /* @formatter:off */
		initializer().
			createUser(OWNER_1).
			createProject(PROJECT_1, OWNER_1).
			createUser(USER_1);
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
