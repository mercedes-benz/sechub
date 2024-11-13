// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Set;

import com.mercedesbenz.sechub.domain.administration.project.Project;

public class TestUserCreationFactory {

    public static User createUser(String userId) {
        User user = new User();
        user.name = userId;
        user.emailAddress = userId + "@example.org";
        user.hashedApiToken = "12345678";
        return user;
    }

    public static User createProjectUser(String userId, Set<Project> projects, boolean superAdmin) {
        User user = new User();
        user.name = userId;
        user.emailAddress = userId + "@example.org";
        user.hashedApiToken = "12345678";
        user.projects = projects;
        user.superAdmin = superAdmin;
        return user;
    }
}
