// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private final String userId;

    private final String email;

    private final boolean superAdmin;

    private final List<String> projects = new ArrayList<>();

    private final List<String> ownedProjects = new ArrayList<>();

    public UserDetailInformation(User user, Set<String> assignedProjects, Set<String> ownedProjects) {
        this.userId = user.getName();
        this.email = user.getEmailAddress();

        if (assignedProjects != null) {
            this.projects.addAll(assignedProjects);

        }
        if (ownedProjects != null) {
            this.ownedProjects.addAll(ownedProjects);
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
