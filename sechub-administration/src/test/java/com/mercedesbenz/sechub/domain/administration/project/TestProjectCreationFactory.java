// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import com.mercedesbenz.sechub.domain.administration.user.User;

public class TestProjectCreationFactory {

    /**
     * Creates a project instance, sets owner and assigns owner as user to project
     *
     * @param projectId
     * @param owner
     * @return project instance
     */
    public static Project createProject(String projectId, User owner) {
        Project project = new Project();
        project.id = projectId;
        project.owner = owner;
        project.users.add(owner); // we simulate here same behavior as done when a project is new created by
                                  // service
        return project;
    }
}
