// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.sharedkernel.AbstractListPage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubJobInfoForUserListPage extends AbstractListPage<SecHubJobInfoForUser> {

    private List<SecHubJobInfoForUser> jobs = new ArrayList<>();

    private String projectId;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<SecHubJobInfoForUser> getContent() {
        return jobs;
    }

}
