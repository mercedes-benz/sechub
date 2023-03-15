// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.sharedkernel.AbstractListPage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSecHubJobInfoForUserListPage extends AbstractListPage<TestSecHubJobInfoForUser> {

    private List<TestSecHubJobInfoForUser> list = new ArrayList<>();

    private String projectId;

    public String getProjectId() {
        return projectId;
    }

    @Override
    public List<TestSecHubJobInfoForUser> getContent() {
        return list;
    }

}