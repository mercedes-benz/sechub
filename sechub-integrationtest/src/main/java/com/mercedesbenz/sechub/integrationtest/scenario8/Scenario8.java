// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario8;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.GrowingScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;

/**
 * <h3>Scenario 8</h3>
 * <h4>Short description</h4>Playing around with execution behaviours by
 * changing profiles etc
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4> This is a {@link GrowingScenario}.<br>
 * At this scenario we mess up with duplicate product executor configurations
 * profiles etc. To have no side effects with existing integration tests we
 * introduced this scenario. Every test should create own custom profiles. <br>
 * <br>
 * In this scenario following is automatically initialized at start
 *
 * <pre>
 * PROJECT_1 is automatically created
 *    - has execution {@link IntegrationTestDefaultProfiles#PROFILE_1 profile 1} assigned
 *    - has USER_1 as owner
 *
 * USER_1, is automatically registered, created and owner of PROJECT_1
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario8 extends AbstractGrowingSecHubServerTestScenario {

    /**
     * User 1 is registered on startup, also owner and user of {@link #PROJECT_1}
     */
    public static final TestUser USER_1 = createTestUser(Scenario8.class, "user1");

    /**
     * Project 1 is created on startup, and has {@link #USER_1} assigned
     */
    public static final TestProject PROJECT_1 = createTestProject(Scenario8.class, "project1");

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

			waitUntilUserExists(USER_1).

			waitUntilUserCanLogin(USER_1)

			;
		/* @formatter:on */
    }

}
