// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.util.Arrays;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.AlreadyExistsException;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorAssignsUserToProject;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectAssignOwnerService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectAssignOwnerService.class);

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
	@UseCaseAdministratorAssignsUserToProject(
			@Step(
					number = 2,
					name = "Assign owner",
					description = "The service will add the user as an owner to the project. If user does not have ROLE_USER it will obtain it"))
	/* @formatter:on */
    public void assignOwnerToProject(String userId, String projectId) {
        LOG.info("User {} triggers assignment of user:{} to project:{}", userContextService.getUserId(), logSanitizer.sanitize(userId, 30),
                logSanitizer.sanitize(projectId, 30));

        assertion.isValidUserId(userId);
        assertion.isValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);
        User owner = userRepository.findOrFailUser(userId);
        
        if (project.owner == owner) {
            throw new AlreadyExistsException("User already assigned in the role as owner to this project!");
        }
        
        project.owner = owner;
        
        owner.getProjects().add(project);
        
        transactionService.saveInOwnTransaction(project, owner);

        sendOwnerAddedToProjectEvent(projectId, owner);
        sendRequestOwnerRoleRecalculation(owner);

    }

	// TODO: check if this needs a distinct REQUEST_OWNER_ROLE_RECALCULATION
    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void sendRequestOwnerRoleRecalculation(User user) {
        eventBus.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

    // TODO: check if this needs a distinct OWNER_CHANGED_ON_PROJECT
    @IsSendingAsyncMessage(MessageID.USER_ADDED_TO_PROJECT)
    private void sendOwnerAddedToProjectEvent(String projectId, User user) {
        DomainMessage request = new DomainMessage(MessageID.USER_ADDED_TO_PROJECT);
        UserMessage projectToUserData = new UserMessage();
        projectToUserData.setUserId(user.getName());
        projectToUserData.setProjectIds(Arrays.asList(projectId));

        request.set(MessageDataKeys.PROJECT_TO_USER_DATA, projectToUserData);
        eventBus.sendAsynchron(request);
    }

}
