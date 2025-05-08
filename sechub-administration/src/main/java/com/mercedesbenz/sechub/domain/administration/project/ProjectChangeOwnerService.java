// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerChangesProjectOwner;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed({ RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
public class ProjectChangeOwnerService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectChangeOwnerService.class);

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
	@UseCaseAdminOrOwnerChangesProjectOwner(
			@Step(
					number = 2,
					name = "Change project owner",
					description = "The service will set the given user as the owner of the project. If the user does not already has the the role ROLE_OWNER it will obtain it. The old owner will loose the project ownership."))
	/* @formatter:on */
    public void changeProjectOwner(String newOnwerUserId, String projectId) {
        LOG.info("User {} triggers project owner change - user {} shall become owner of project {}", userContextService.getUserId(),
                logSanitizer.sanitize(newOnwerUserId, 30), logSanitizer.sanitize(projectId, 30));

        assertion.assertIsValidUserId(newOnwerUserId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);

        assertAllowedToChangeProjectOwnership(project);

        User newOwner = userRepository.findOrFailUser(newOnwerUserId);

        if (project.owner.equals(newOwner)) {
            throw new AlreadyExistsException("User is already assigned as owner to this project!");
        }

        User previousOwner = changeProjectOwnerAndReturnPreviousOwner(project, newOwner);

        transactionService.saveInOwnTransaction(project, newOwner, previousOwner);

        sendOwnerChangedForProjectEvent(project, previousOwner, newOwner);
        sendRequestOwnerRoleRecalculation(previousOwner);
        sendAssignOwnerAsUserToProject(newOwner, project);
    }

    private void assertAllowedToChangeProjectOwnership(Project project) {
        if (userContextService.isSuperAdmin()) {
            /* super admin is always allowed... */
            return;
        }
        String currentUserId = userContextService.getUserId();
        String projectOwnerId = project.getOwner().getName();

        if (projectOwnerId.equals(currentUserId)) {
            /* current project owner is also allowed */
            return;
        }

        throw new AccessDeniedException("You are not allowed to change ownership for project " + project.getId() + " !");
    }

    private User changeProjectOwnerAndReturnPreviousOwner(Project project, User newOwner) {
        User previousOwner = project.owner;
        project.owner = newOwner;

        return previousOwner;
    }

    @IsSendingAsyncMessage(MessageID.ASSIGN_OWNER_AS_USER_TO_PROJECT)
    private void sendAssignOwnerAsUserToProject(User owner, Project project) {
        DomainMessage request = DomainMessageFactory.createAssignOwnerAsUserToProject(owner.getName(), project.getId());
        eventBus.sendAsynchron(request);
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
        projectData.setPreviousProjectOwnerEmailAddress(previousOwner.getEmailAddress());
        projectData.setProjectOwnerEmailAddress(newOwner.getEmailAddress());

        project.getUsers().forEach(user -> {
            projectData.addUserEmailAddress(user.getEmailAddress());
        });

        request.set(MessageDataKeys.PROJECT_OWNER_CHANGE_DATA, projectData);
        eventBus.sendAsynchron(request);
    }

}
