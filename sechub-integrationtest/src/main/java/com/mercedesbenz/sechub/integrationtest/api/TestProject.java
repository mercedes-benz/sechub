// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.integrationtest.internal.TestScenario;

public class TestProject {

    private String projectIdPart;
    private String description;
    private List<String> whiteListUrls;
    private String prefix = "";
    private boolean withWhiteList;

    private static final List<TestProject> all = new ArrayList<>();

    TestProject() {
        all.add(this);
    }

    /**
     * Creates a test project without a whitelist. Description is automatically
     * generated and contains the project id as well
     *
     * @param projectIdPart name of the project if {@link #prepare(TestScenario)} is
     *                      not called, otherwise it will be the part after the
     *                      scenario prefix
     */
    public TestProject(String projectIdPart) {
        this(projectIdPart, false);
    }

    public TestProject(String projectIdPart, boolean withWhiteList) {
        this.description = "description of " + projectIdPart;
        this.projectIdPart = projectIdPart;
        this.whiteListUrls = new ArrayList<>();
        this.withWhiteList = withWhiteList;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Prepare the test project for the given scenario. Calculates the prefix for
     * the project id from the scenario name.
     *
     * @param scenario
     */
    public void prepare(TestScenario scenario) {
        this.prefix = scenario.getName().toLowerCase();
        whiteListUrls.clear();
        if (withWhiteList) {

            String whiteListURL = "http://locahost/" + getProjectId();
            whiteListUrls.add(whiteListURL);

            for (IntegrationTestMockMode mode : IntegrationTestMockMode.values()) {
                if (mode.isTargetUsableAsWhitelistEntry()) {
                    whiteListUrls.add(mode.getMockDataIdentifier());
                }
            }
        }
    }

    public String getProjectId() {
        return prefix + projectIdPart;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getWhiteListUrls() {
        return whiteListUrls;
    }
}
