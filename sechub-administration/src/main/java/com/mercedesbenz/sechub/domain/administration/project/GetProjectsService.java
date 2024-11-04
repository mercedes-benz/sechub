package com.mercedesbenz.sechub.domain.administration.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class GetProjectsService {

    private final UserRepository userRepository;
    private final UserInputAssertion userInputAssertion;
    private final ProjectRepository projectRepository;

    public GetProjectsService(UserRepository userRepository, UserInputAssertion userInputAssertion, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.userInputAssertion = userInputAssertion;
        this.projectRepository = projectRepository;
    }
   //Todo: Superadmin and tests!!!
    public GetProjectsDTO[] getProjects(String userId) {
        userInputAssertion.assertIsValidUserId(userId);
        User user = userRepository.findOrFailUser(userId);

        List<GetProjectsDTO> projects = new ArrayList<>();

        for (Project userProject : user.getProjects()) {
            GetProjectsDTO getProjectsDTO = new GetProjectsDTO();
            Project project = projectRepository.findOrFailProject(userProject.getId());

            getProjectsDTO.setProjectId(project.getId());
            getProjectsDTO.setOwner(project.getOwner().getName());

            if (project.getOwner().equals(user)) {
                getProjectsDTO.setOwned(true);
                List<String> assinedUsers = new ArrayList<>();
                project.getUsers().forEach(projectUser -> assinedUsers.add(projectUser.getName()));
                getProjectsDTO.setAssinedUsers(Optional.of(assinedUsers.toArray(new String[0])));
            }
        }

        return projects.toArray(new GetProjectsDTO[0]);
    }

    public String[] userListProjects(String userId) {
        userInputAssertion.assertIsValidUserId(userId);
        User user = userRepository.findOrFailUser(userId);
        return getProjects(user);
    }

    private static String[] getProjects(User user) {
        List<String> projectIDs = new ArrayList<>();

        for (Project project : user.getProjects()) {
            projectIDs.add(project.getId());
        }
        return projectIDs.toArray(new String[0]);
    }
}
