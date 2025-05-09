// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Arrays;

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
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerAssignsUserToProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed({ RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
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
	@UseCaseAdminOrOwnerAssignsUserToProject(
			@Step(
					number = 2,
					name = "Assign user",
					description = "The service will add the user to the project. If user does not have ROLE_USER it will obtain it"))
	/* @formatter:on */
    public void assignUserToProjectAsUser(String userId, String projectId, boolean failOnExistingAssignment) {
        assignUserToProject(userId, projectId, failOnExistingAssignment, false);
    }

    /**
     * Assigns a user to a project as system. This method is used by the system when
     * receiving DomainMessages
     *
     * @param userId                   the user id to assign
     * @param projectId                the project id to assign the user to
     * @param failOnExistingAssignment if true, an exception will be thrown if the
     *                                 user is already assigned to the project
     */
    public void assignUserToProjectAsSystem(String userId, String projectId, boolean failOnExistingAssignment) {
        assignUserToProject(userId, projectId, failOnExistingAssignment, true);
    }

    private void assignUserToProject(String userId, String projectId, boolean failOnExistingAssignment, boolean isSystem) {
        LOG.info("User {} triggers assignment of user:{} to project:{}", userContextService.getUserId(), logSanitizer.sanitize(userId, 30),
                logSanitizer.sanitize(projectId, 30));

        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);

        if (!isSystem) {
            assertAllowedToAddProjectMembers(project);
        }

        User user = userRepository.findOrFailUser(userId);
        if (project.getUsers().add(user)) {
            transactionService.saveInOwnTransaction(project, user);

            sendUserAddedToProjectEvent(projectId, user);
        } else {
            if (failOnExistingAssignment) {
                throw new AlreadyExistsException("User already assigned to this project!");
            }
            LOG.info("User {} is already assigned to project {} - but not handled as failure", user.getName(), project.getId());
        }

        /* in any case which does not lead to a failure we request a recalculation */
        sendRequestUserRoleRecalculation(user);
    }

    private void assertAllowedToAddProjectMembers(Project project) {
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

        throw new AccessDeniedException("You are not allowed to add members to project " + project.getId() + " !");
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
