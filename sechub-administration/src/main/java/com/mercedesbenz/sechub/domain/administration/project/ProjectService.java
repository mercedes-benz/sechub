// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

            ProjectData projectData = createProjectDataForProject(user, project);
            projectDataList.add(projectData);

        }

        return projectDataList;
    }

    private static ProjectData createProjectDataForProject(User user, Project project) {

        ProjectData projectData = new ProjectData();
        projectData.setProjectId(project.getId());

        /* project ownership */
        ProjectUserData ownerUserData = new ProjectUserData();
        ownerUserData.setUserId(project.getOwner().getName());
        ownerUserData.setEmailAddress(project.getOwner().getEmailAddress());

        projectData.setOwner(ownerUserData);

        boolean isOwner = user.equals(project.getOwner());
        projectData.setOwned(isOwner);

        /* additional users - role shall have this information as well */
        if (user.isSuperAdmin() || isOwner) {
            addAssignedUsersToProjectData(project, projectData);
        }
        return projectData;
    }

    private static void addAssignedUsersToProjectData(Project project, ProjectData projectData) {
        SortedSet<ProjectUserData> assignedUsers = new TreeSet<>(); // we use a tree set to have it sorted

        project.getUsers().forEach(projectUser -> {
            ProjectUserData assignedUserData = new ProjectUserData();
            assignedUserData.setUserId(projectUser.getName());
            assignedUserData.setEmailAddress(projectUser.getEmailAddress());
            assignedUsers.add(assignedUserData);
        });
        projectData.setAssignedUsers(new ArrayList<>(assignedUsers));
    }

}
