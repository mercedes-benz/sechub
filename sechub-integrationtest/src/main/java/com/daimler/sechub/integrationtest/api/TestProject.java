// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.integrationtest.internal.TestScenario;

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
    
    public TestProject(String projectIdPart) {
        this(projectIdPart,false);
    }

    public TestProject(String projectIdPart, boolean withWhiteList) {
        this.description = "description of " + projectIdPart;
        this.projectIdPart = projectIdPart;
        this.whiteListUrls = new ArrayList<>();
        this.withWhiteList = withWhiteList;
    }

    public void prepare(TestScenario scenario) {
        this.prefix = scenario.getName().toLowerCase();
        whiteListUrls.clear();
        if (withWhiteList) {

            String whiteListURL = "http://locahost/" + getProjectId();
            whiteListUrls.add(whiteListURL);

            for (IntegrationTestMockMode mode : IntegrationTestMockMode.values()) {
                if (mode.isTargetUsableAsWhitelistEntry()) {
                    whiteListUrls.add(mode.getTarget());
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
