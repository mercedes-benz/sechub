// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectDeleteService {

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserContextService userContext;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    SecHubEnvironment sechubEnvironment;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    ProjectTransactionService transactionService;

    private static final Logger LOG = LoggerFactory.getLogger(ProjectDeleteService.class);

    @UseCaseAdminDeleteProject(@Step(number = 2, name = "Service deletes projects.", next = { 3, 4, 5, 6,
            7 }, description = "The service will delete the project with dependencies and triggers asynchronous events"))
    public void deleteProject(String projectId) {
        auditLogService.log("triggers delete of project {}", logSanitizer.sanitize(projectId, 30));

        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);

        /* create message containing data before project is deleted */
        ProjectMessage message = new ProjectMessage();
        message.setProjectId(project.getId());
        message.setProjectActionTriggeredBy(userContext.getUserId());

        User owner = project.getOwner();
        if (owner == null) {
            LOG.warn("No owner found for project {} while deleting", project.getId());
        } else {
            message.setProjectOwnerEmailAddress(owner.getEmailAddress());
            owner.getOwnedProjects().remove(project); // handle ORM mapping. Avoid cache conflicts
        }

        for (User user : project.getUsers()) {
            message.addUserEmailAddress(user.getEmailAddress());
            user.getProjects().remove(project); // handle ORM mapping. Avoid cache conflicts
        }

        transactionService.deleteWithAssociationsInOwnTransaction(projectId);

        informProjectDeleted(message);
        if (owner != null) {
            sendRefreshUserAuth(owner);
        }

    }

    @IsSendingAsyncMessage(MessageID.PROJECT_DELETED)
    private void informProjectDeleted(ProjectMessage message) {
        DomainMessage infoRequest = new DomainMessage(MessageID.PROJECT_DELETED);
        infoRequest.set(MessageDataKeys.PROJECT_DELETE_DATA, message);
        infoRequest.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());

        eventBusService.sendAsynchron(infoRequest);
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void sendRefreshUserAuth(User ownerUser) {
        eventBusService.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(ownerUser.getName()));
    }
}
