// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final ProjectRepository projectRepository;
    private final UserInputAssertion userInputAssertion;
    private final DomainMessageService eventBus;

    public ProjectService(UserRepository userRepository, ProjectRepository projectRepository, UserInputAssertion userInputAssertion,
            DomainMessageService eventBus) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.userInputAssertion = userInputAssertion;
        this.eventBus = eventBus;
    }

    public List<ProjectData> getProjectDataList(String userId) {
        userInputAssertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        return collectProjectDataForUser(user);
    }

    @IsSendingSyncMessage(MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS)
    private List<ProjectData> collectProjectDataForUser(User user) {

        /* @formatter:off */
        Set<String> projectIdsWhereUserIsAssigned = projectRepository.findAllProjectIdsWhereUserIsAssigned(user.getName());
        Set<String> projectIdsWhereUserIsOwner = projectRepository.findAllProjectIdsWhereUserIsOwner(user.getName());

        Set<String> relevantProjectids = new HashSet<>();
        relevantProjectids.addAll(projectIdsWhereUserIsAssigned);
        relevantProjectids.addAll(projectIdsWhereUserIsOwner);

        /* @formatter:on */
        DomainMessage message = new DomainMessage(MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        message.set(PROJECT_IDS, new ArrayList<>(relevantProjectids));
        DomainMessageSynchronousResult response = eventBus.sendSynchron(message);

        Map<String, List<String>> enabledProjectToProfileIds = response.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS);

        List<ProjectData> projectDataList = new ArrayList<>();

        for (String projectId : relevantProjectids) {
            ProjectData projectData = createProjectDataForProject(user, projectId, enabledProjectToProfileIds);
            projectDataList.add(projectData);

        }

        return projectDataList;
    }

    private ProjectData createProjectDataForProject(User user, String projectId, Map<String, List<String>> projectToProfileIds) {

        ProjectData projectData = new ProjectData();
        projectData.setProjectId(projectId);

        /* project ownership */
        ProjectUserData ownerUserData = projectRepository.fetchProjectUserDataForOwner(projectId);

        projectData.setOwner(ownerUserData);

        boolean isOwner = user.getName().equals(ownerUserData.getUserId());
        projectData.setOwned(isOwner);

        /* additional users - role shall have this information as well */
        if (user.isSuperAdmin() || isOwner) {
            addAssignedUsersToProjectData(projectId, projectData);
        }

        List<String> profileIds = projectToProfileIds.get(projectId);
        if (profileIds != null) {
            projectData.setEnabledProfileIds(new HashSet<>(profileIds));
        }
        return projectData;
    }

    private void addAssignedUsersToProjectData(String projectId, ProjectData projectData) {
        List<ProjectUserData> assignedUserData = projectRepository.fetchOrderedProjectUserDataForAssignedUsers(projectId);
        projectData.setAssignedUsers(assignedUserData);
    }

}
