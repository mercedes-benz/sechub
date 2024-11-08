// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ProjectService {

    private final UserRepository userRepository;
    private final UserInputAssertion userInputAssertion;

    public ProjectService(UserRepository userRepository, UserInputAssertion userInputAssertion) {
        this.userRepository = userRepository;
        this.userInputAssertion = userInputAssertion;
    }

    public List<ProjectData> getAssignedProjectDataList(String userId) {
        userInputAssertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        return collectProjectDataForUser(user);
    }

    private List<ProjectData> collectProjectDataForUser(User user) {

        List<ProjectData> projectDataList = new ArrayList<>();
        for (Project project : user.getProjects()) {
            ProjectData projectData = createProjectDataForProject(project);

            boolean isOwner = user.equals(project.getOwner());

            projectData.setOwned(isOwner);

            if (user.isSuperAdmin() || isOwner) {
                addAssignedUsersToProjectData(project, projectData);
            }

            projectDataList.add(projectData);
        }

        return projectDataList;
    }

    private static void addAssignedUsersToProjectData(Project project, ProjectData projectData) {
        List<String> assignedUsers = new ArrayList<>(project.getUsers().size());
        project.getUsers().forEach(projectUser -> assignedUsers.add(projectUser.getEmailAddress()));
        projectData.setAssignedUsers(assignedUsers.toArray(new String[0]));
    }

    private static ProjectData createProjectDataForProject(Project project) {
        ProjectData projectData = new ProjectData();

        projectData.setProjectId(project.getId());
        projectData.setOwner(project.getOwner().getEmailAddress());
        return projectData;
    }
}
