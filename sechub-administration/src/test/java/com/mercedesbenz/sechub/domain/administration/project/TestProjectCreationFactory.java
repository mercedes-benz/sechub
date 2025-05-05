// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import com.mercedesbenz.sechub.domain.administration.user.User;

public class TestProjectCreationFactory {

    public static Project createProject(String projectId, User owner) {
        Project project = new Project();
        project.id = projectId;
        project.owner = owner;
        project.users.add(owner);
        return project;
    }
}
