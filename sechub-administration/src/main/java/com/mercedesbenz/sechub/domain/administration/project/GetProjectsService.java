package com.mercedesbenz.sechub.domain.administration.project;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
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

    public GetProjectsDTO[] getProjects(String userId) {
        userInputAssertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);
        List<GetProjectsDTO> projects = new ArrayList<>();

        getProjectsForUser(user, projects);

        return projects.toArray(new GetProjectsDTO[0]);
    }

    private void getProjectsForUser(User user, List<GetProjectsDTO> projects) {
        if (user.isSuperAdmin()) {
            for (Project project : projectRepository.findAll()) {
                GetProjectsDTO getProjectsDTO = createGetProjectsDTO(project);

                // TODO: is equals the correct method to compare the user?
                if (user.equals(project.getOwner())) {
                    getProjectsDTO.setOwned(true);
                }

                addAssignedUsers(project, getProjectsDTO);
                projects.add(getProjectsDTO);
            }
        } else {
            for (Project project : user.getProjects()) {
                GetProjectsDTO getProjectsDTO = createGetProjectsDTO(project);

                // TODO: is equals the correct method to compare the user?
                if (user.equals(project.getOwner())) {
                    getProjectsDTO.setOwned(true);
                    addAssignedUsers(project, getProjectsDTO);
                }
                projects.add(getProjectsDTO);
            }
        }
    }

    private static void addAssignedUsers(Project project, GetProjectsDTO getProjectsDTO) {
        List<String> assignedUsers = new ArrayList<>();
        project.getUsers().forEach(projectUser -> assignedUsers.add(projectUser.getName()));
        getProjectsDTO.setAssignedUsers(assignedUsers.toArray(new String[0]));
    }

    private static GetProjectsDTO createGetProjectsDTO(Project project) {
        GetProjectsDTO getProjectsDTO = new GetProjectsDTO();

        getProjectsDTO.setProjectId(project.getId());
        getProjectsDTO.setOwner(project.getOwner().getName());
        return getProjectsDTO;
    }
}
