package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Optional;

import jakarta.annotation.Nullable;

public class GetProjectsDTO {
    private String projectId;
    private String owner;
    private boolean isOwned;
    @Nullable
    private String[] assignedUsers;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setOwned(boolean owned) {
        isOwned = owned;
    }

    public void setAssignedUsers(String[] assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public boolean isOwned() {
        return isOwned;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getOwner() {
        return owner;
    }

    public Optional<String[]> getAssignedUsers() {
        return Optional.ofNullable(assignedUsers);
    }
}
