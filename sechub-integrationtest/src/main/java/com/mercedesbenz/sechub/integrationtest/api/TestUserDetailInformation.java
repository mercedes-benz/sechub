// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

public class TestUserDetailInformation {

    private String userId;

    private String email;

    private boolean superAdmin;

    private List<String> projects = new ArrayList<>();

    private List<String> ownedProjects = new ArrayList<>();

    public List<String> getProjects() {
        return projects;
    }

    public List<String> getOwnedProjects() {
        return ownedProjects;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

}
