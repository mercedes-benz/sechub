// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario8;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;

/**
 * <pre>
 * ---------------------------------------------------------------------------------
 * Scenario 8 -  playing around with execution behaviours by changing profiles etc 
 * ---------------------------------------------------------------------------------
 * </pre>
 * 
 * This scenario is like scenario3 but here we mess up with duplicate product
 * executor configurations profiles etc. To have no side effects we introduce
 * this scenario
 * 
 * In this scenario following is automatically initialized at start (old data
 * removed as well)
 *
 * <pre>
 * PROJECT_1_ is automatically created
 * USER_1, is automatically registered, created and assigned to project1
 * </pre>
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario8 extends AbstractGrowingSecHubServerTestScenario {

    public static final String PREFIX_MAIN_ID = "s08";

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
			createProject(PROJECT_1, USER_1).
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

    @Override
    public String getPrefixMainId() {
        return PREFIX_MAIN_ID;
    }

}
