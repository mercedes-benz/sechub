// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.TestScenario;

/**
 * A special test project variant - the project id will not be changed by
 * scenarios. Only necessary for some special purposes - e.g. inside DAUI where
 * test framework methods are used having TestProject arguments.
 * 
 * @author Albert Tregnaghi
 *
 */
public class FixedTestProject extends TestProject {

    private String fixedProjectId;

    public FixedTestProject(String projectId) {
        super("fixed project with id:" + projectId);
        this.fixedProjectId = projectId;
    }

    @Override
    public String getProjectId() {
        return fixedProjectId;
    }

    @Override
    public void prepare(TestScenario scenario) {
        /* we do not prepare fixed ones */
    }
}
