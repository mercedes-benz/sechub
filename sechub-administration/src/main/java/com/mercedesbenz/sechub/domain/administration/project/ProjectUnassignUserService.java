// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.AlreadyExistsException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUnassignsUserFromProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectUnassignUserService {

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    ProjectTransactionService transactionService;

    /* @formatter:off */
	@UseCaseAdminUnassignsUserFromProject(@Step(number = 2, name = "Unassign user", description = "The service will remove the user to the project. If users has no longer access to projects ROLE_USER will be removed"))
	/* @formatter:on */
    public void unassignUserFromProject(String userId, String projectId) {
        auditLogService.log("triggers unassignment of user:{} to project:{}", logSanitizer.sanitize(userId, 30), logSanitizer.sanitize(projectId, 30));

        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);
        User user = userRepository.findOrFailUser(userId);
        if (!project.getUsers().remove(user)) {
            throw new AlreadyExistsException("User already not assigned to this project!");
        }
        user.getProjects().remove(project);

        transactionService.saveInOwnTransaction(project, user);

        sendUserRemovedFromProjectEvent(projectId, user);
        sendRequestUserRoleRecalculation(user);
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void sendRequestUserRoleRecalculation(User user) {
        eventBus.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

    @IsSendingAsyncMessage(MessageID.USER_REMOVED_FROM_PROJECT)
    private void sendUserRemovedFromProjectEvent(String projectId, User user) {
        DomainMessage request = new DomainMessage(MessageID.USER_REMOVED_FROM_PROJECT);
        UserMessage projectToUserData = new UserMessage();
        projectToUserData.setUserId(user.getName());
        projectToUserData.setProjectIds(Arrays.asList(projectId));

        request.set(MessageDataKeys.PROJECT_TO_USER_DATA, projectToUserData);
        eventBus.sendAsynchron(request);
    }

}
