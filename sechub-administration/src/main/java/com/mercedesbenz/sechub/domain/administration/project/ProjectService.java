// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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

    public List<ProjectData> getProjectDataList(String userId) {
        userInputAssertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        return collectProjectDataForUser(user);
    }

    private List<ProjectData> collectProjectDataForUser(User user) {
        // TODO: 11/8/24 Sets to stream, to set or version two?
        Set<Project> projects = Stream.of(user.getProjects(), user.getOwnedProjects()).flatMap(Set::stream).collect(toSet());

        /*
         * List<Project> projects = new
         * ArrayList<>(user.getProjects().stream().toList()); for (Project project :
         * user.getOwnedProjects()) { if (!projects.contains(project)) {
         * projects.add(project); } }
         */

        List<ProjectData> projectDataList = new ArrayList<>();
        for (Project project : projects.stream().toList()) {
            ProjectData projectData = createProjectDataForProject(project);

            if (user.isSuperAdmin() || user.equals(project.getOwner())) {
                projectData.setOwned(true);
                addAssignedUsersToProjectData(project, projectData);
            } else {
                projectData.setOwned(false);
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
