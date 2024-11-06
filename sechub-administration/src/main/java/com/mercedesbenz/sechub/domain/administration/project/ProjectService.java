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

    public ProjectData[] getProjectData(String userId) {
        userInputAssertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);
        List<ProjectData> projects = new ArrayList<>();

        collectProjectDataForUser(user, projects);

        return projects.toArray(new ProjectData[0]);
    }

    private void collectProjectDataForUser(User user, List<ProjectData> projects) {
        for (Project project : user.getProjects()) {
            ProjectData projectData = createProjectDataForProject(project);

            if (user.equals(project.getOwner()) || user.isSuperAdmin()) {
                projectData.setOwned(true);
                addAssignedUsersToProjectData(project, projectData);
            }else{
                projectData.setOwned(false);
            }

            projects.add(projectData);
        }
    }

    private static void addAssignedUsersToProjectData(Project project, ProjectData projectData) {
        List<String> assignedUsers = new ArrayList<>();
        project.getUsers().forEach(projectUser -> assignedUsers.add(projectUser.getName()));
        projectData.setAssignedUsers(assignedUsers.toArray(new String[0]));
    }

    private static ProjectData createProjectDataForProject(Project project) {
        ProjectData projectData = new ProjectData();

        projectData.setProjectId(project.getId());
        projectData.setOwner(project.getOwner().getName());
        return projectData;
    }
}
