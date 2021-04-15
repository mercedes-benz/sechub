// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

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
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
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
        User newOwner = userRepository.findOrFailUser(userId);

        if (project.owner.equals(newOwner)) {
            throw new AlreadyExistsException("User already assigned in the role as owner to this project!");
        }

        User previousOwner = project.owner;
        project.owner = newOwner;

        newOwner.getProjects().add(project);
        previousOwner.getProjects().remove(project);

        transactionService.saveInOwnTransaction(project, newOwner);

        sendOwnerChangedForProjectEvent(project, previousOwner, newOwner);
        sendRequestOwnerRoleRecalculation(newOwner);
        sendRequestOwnerRoleRecalculation(previousOwner);
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void sendRequestOwnerRoleRecalculation(User user) {
        eventBus.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

    @IsSendingAsyncMessage(MessageID.PROJECT_OWNER_CHANGED)
    private void sendOwnerChangedForProjectEvent(Project project, User previousOwner, User newOwner) {
        
        DomainMessage request = new DomainMessage(MessageID.PROJECT_OWNER_CHANGED);
        ProjectMessage projectData = new ProjectMessage();
        projectData.setProjectId(project.id);
        projectData.setPreviousProjectOwnerEmailAddress(previousOwner.getEmailAdress());
        projectData.setProjectOwnerEmailAddress(newOwner.getEmailAdress());
        
        project.users.forEach(user -> {
            projectData.addUserEmailAddress(user.getEmailAdress());
        });
        
        request.set(MessageDataKeys.PROJECT_OWNER_CHANGE_DATA, projectData);
        eventBus.sendAsynchron(request);
    }

}
