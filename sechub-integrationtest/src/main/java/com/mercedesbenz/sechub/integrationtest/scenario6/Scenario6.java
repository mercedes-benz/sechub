// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.NoSecHubSuperAdminNecessaryScenario;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.StaticTestScenario;

/**
 * <h3>Scenario 6</h3>
 * <h4>Short description</h4> PDS ONLY integration test scenario.
 *
 * <h4>Overview</h4> For an overview over all scenarios, look at
 * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
 * Overview}
 *
 * <h4>Details</h4>This is a {@link StaticTestScenario}. The PDS ONLY
 * integration test scenario (no special sechub storage settings
 * necessary)</u></b><br>
 *
 * In this scenario no sechub server instance is necessary. No special
 * preparations are done. Tests do only communicate directly with PDS.
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario6 implements PDSTestScenario, StaticTestScenario, NoSecHubSuperAdminNecessaryScenario {

    private int tempCounter;

    @Override
    public boolean isInitializationNecessary() {
        return false;
    }

    @Override
    public void prepare(String testClass, String testMethod) {
        /* no preparations necessary - just use PDS as is */
    }

    @Override
    public TestProject newTestProject() {
        tempCounter++;
        return newTestProject("tmp_" + tempCounter);
    }

    @Override
    public TestProject newTestProject(String projectIdPart) {
        TestProject project = new TestProject(projectIdPart);
        project.prepare(this);
        return project;
    }
}