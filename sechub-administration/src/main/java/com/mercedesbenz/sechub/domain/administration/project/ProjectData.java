// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectData {
    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_IS_OWNED = "isOwned";
    public static final String PROPERTY_ASSIGNED_USERS = "assignedUsers";

    private String projectId;
    private ProjectUserData owner;
    private Boolean isOwned;
    private List<ProjectUserData> assignedUsers;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setOwner(ProjectUserData owner) {
        this.owner = owner;
    }

    public void setOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public void setAssignedUsers(List<ProjectUserData> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    @JsonProperty(PROPERTY_IS_OWNED)
    public Boolean isOwned() {
        return isOwned;
    }

    @JsonProperty(PROPERTY_PROJECT_ID)
    public String getProjectId() {
        return projectId;
    }

    @JsonProperty(PROPERTY_OWNER)
    public ProjectUserData getOwner() {
        return owner;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(PROPERTY_ASSIGNED_USERS)
    public List<ProjectUserData> getAssignedUsers() {
        return assignedUsers;
    }
}
