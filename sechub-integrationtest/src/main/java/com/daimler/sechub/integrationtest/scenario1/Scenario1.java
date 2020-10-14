// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.integrationtest.internal.AbstractGrowingSecHubServerTestScenario;

/**
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
            ensureDefaultExecutionProfile_1().
            createUser(OWNER_1).
            addProjectIdsToDefaultExecutionProfile_1(PROJECT_1);
        /* @formatter:on */

    }

    @Override
    protected void waitForTestDataAvailable() {
        initializer().waitUntilUserCanLogin(OWNER_1);
    }

    @Override
    public String getPrefixMainId() {
        return "s01";
    }
    

}
