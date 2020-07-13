// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractSecHubServerTestScenario;

/**
 * In this scenario nearly nothing of the constants is existing, except "OWNER_1" which is necessary for project creation tests
 *
 * <pre>
 * USER_1, not in system. must self register him/herself.
 * PROJECT_1_ not created
 * </pre>
 * @author Albert Tregnaghi
 *
 */
public class Scenario1 extends AbstractSecHubServerTestScenario{

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
		initializer().createUser(OWNER_1);

	}
	@Override
	protected void waitForTestDataAvailable() {
		initializer().waitUntilUserCanLogin(OWNER_1);
	}



}
