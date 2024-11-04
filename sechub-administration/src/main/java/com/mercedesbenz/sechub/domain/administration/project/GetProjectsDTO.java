package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Optional;

public class GetProjectsDTO {
    private String projectId;
    private String owner;
    private boolean isOwned;
    private Optional<String[]> assinedUsers;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isOwned() {
        return isOwned;
    }

    public void setOwned(boolean owned) {
        isOwned = owned;
    }

    public void setAssinedUsers(Optional<String[]> assinedUsers) {
        this.assinedUsers = assinedUsers;
    }
}
