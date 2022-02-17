// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.PDSTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.StaticTestScenario;

/**
 * <b><u>Scenario6 - the PDS ONLY integration test scenario (no special sechub
 * storage setings necessary)</u></b><br>
 *
 * In this scenario no sechub server instance is necessary. No special
 * preparations are done.
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class Scenario6 implements PDSTestScenario, StaticTestScenario {

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