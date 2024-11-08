// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectData {
    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_IS_OWNED = "isOwned";
    public static final String PROPERTY_ASSIGNED_USERS = "assignedUsers";

    private String projectId;
    private String owner;
    private Boolean isOwned;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] assignedUsers;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public void setAssignedUsers(String[] assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    @JsonProperty(PROPERTY_IS_OWNED)
    public boolean isOwned() {
        return isOwned;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getOwner() {
        return owner;
    }

    public String[] getAssignedUsers() {
        return assignedUsers;
    }
}
