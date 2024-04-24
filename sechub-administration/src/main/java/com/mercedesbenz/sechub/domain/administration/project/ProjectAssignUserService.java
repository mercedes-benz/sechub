// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminAssignsUserToProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectAssignUserService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectAssignUserService.class);

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserContextService userContextService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    ProjectTransactionService transactionService;

    /* @formatter:off */
	@UseCaseAdminAssignsUserToProject(
			@Step(
					number = 2,
					name = "Assign user",
					description = "The service will add the user to the project. If user does not have ROLE_USER it will obtain it"))
	/* @formatter:on */
    public void assignUserToProject(String userId, String projectId) {
        LOG.info("User {} triggers assignment of user:{} to project:{}", userContextService.getUserId(), logSanitizer.sanitize(userId, 30),
                logSanitizer.sanitize(projectId, 30));

        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);
        User user = userRepository.findOrFailUser(userId);
        if (!project.getUsers().add(user)) {
            throw new AlreadyExistsException("User already assigned to this project!");
        }
        user.getProjects().add(project);
        project.getUsers().add(user);

        transactionService.saveInOwnTransaction(project, user);

        sendUserAddedToProjectEvent(projectId, user);
        sendRequestUserRoleRecalculation(user);

    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void sendRequestUserRoleRecalculation(User user) {
        eventBus.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

    @IsSendingAsyncMessage(MessageID.USER_ADDED_TO_PROJECT)
    private void sendUserAddedToProjectEvent(String projectId, User user) {
        DomainMessage request = new DomainMessage(MessageID.USER_ADDED_TO_PROJECT);
        UserMessage projectToUserData = new UserMessage();
        projectToUserData.setUserId(user.getName());
        projectToUserData.setProjectIds(Arrays.asList(projectId));

        request.set(MessageDataKeys.PROJECT_TO_USER_DATA, projectToUserData);
        eventBus.sendAsynchron(request);
    }

}
