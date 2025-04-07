// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_ASSIGNED_PROFILE_IDS;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_IDS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ProjectService {

    private final UserRepository userRepository;
    private final UserInputAssertion userInputAssertion;
    private final DomainMessageService eventBus;

    public ProjectService(UserRepository userRepository, UserInputAssertion userInputAssertion, DomainMessageService eventBus) {
        this.userRepository = userRepository;
        this.userInputAssertion = userInputAssertion;
        this.eventBus = eventBus;
    }

    public List<ProjectData> getAssignedProjectDataList(String userId) {
        userInputAssertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        return collectProjectDataForUser(user);
    }

    @IsSendingSyncMessage(MessageID.REQUEST_PROFILE_IDS_FOR_PROJECT)
    private List<ProjectData> collectProjectDataForUser(User user) {

        /* @formatter:off */
        List<String> projectIdsForUser = user.getProjects()
                                            .stream()
                                            .map(Project::getId)
                                            .collect(Collectors.toList());
        /* @formatter:on */
        DomainMessage message = new DomainMessage(MessageID.REQUEST_PROFILE_IDS_FOR_PROJECT);
        message.set(PROJECT_IDS, projectIdsForUser);
        DomainMessageSynchronousResult response = eventBus.sendSynchron(message);

        Map<String, List<String>> projectToProfileIds = response.get(PROJECT_ASSIGNED_PROFILE_IDS);

        List<ProjectData> projectDataList = new ArrayList<>();
        for (Project project : user.getProjects()) {

            ProjectData projectData = createProjectDataForProject(user, project);
            List<String> profileIds = projectToProfileIds.get(project.getId());
            if (profileIds != null) {
                projectData.setAssignedProfileIds(new HashSet<>(profileIds));
            }
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
