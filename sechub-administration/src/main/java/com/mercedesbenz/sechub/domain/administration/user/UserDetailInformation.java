// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.domain.administration.project.Project;

/**
 * This represents an information object used for json output
 *
 * @author Albert Tregnaghi
 *
 */
public class UserDetailInformation {

    public static final String PROPERTY_USERNAME = "userId";
    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_PROJECTS = "projects";
    public static final String PROPERTY_OWNED_PROJECTS = "ownedProjects";
    public static final String PROPERTY_SUPERADMIN = "superAdmin";

    private String userId;

    private String email;

    private boolean superAdmin;

    private List<String> projects = new ArrayList<>();

    private List<String> ownedProjects = new ArrayList<>();

    public UserDetailInformation(User user) {
        this.userId = user.getName();
        this.email = user.getEmailAddress();

        for (Project project : user.getProjects()) {
            this.projects.add(project.getId());
        }

        for (Project project : user.getOwnedProjects()) {
            this.ownedProjects.add(project.getId());
        }
        this.superAdmin = user.isSuperAdmin();
    }

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
