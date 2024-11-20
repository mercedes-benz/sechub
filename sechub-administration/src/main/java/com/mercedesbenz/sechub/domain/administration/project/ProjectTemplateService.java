// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubProjectTemplates;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubProjectToTemplate;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsTemplateToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsTemplateFromProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectTemplateService.class);

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectTransactionService projectTansactionService;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
    @UseCaseAdminAssignsTemplateToProject(
			@Step(
					number = 2,
					name = "service assigns template to project",
					description = "The service will request the template assignment in domain 'scan' via synchronous event and updates mapping in domain 'administration' afterwards"))
	/* @formatter:on */
    public void assignTemplateToProject(String templateId, String projectId) {
        assertion.assertIsValidTemplateId(templateId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);
        Set<String> templates = project.getTemplates();
        LOG.debug("Start assigning template '{}' to project '{}'. Formerly assgined templates : {}", templateId, projectId, templates);

        SecHubProjectTemplates result = sendAssignRequestAndFetchResult(templateId, projectId);
        List<String> newTemplates = result.getTemplateIds();
        templates.clear();
        templates.addAll(newTemplates);

        projectTansactionService.saveInOwnTransaction(project);
        LOG.info("Assigned template '{}' to project '{}'", templateId, projectId);

        LOG.debug("Project '{}' has now following templates: {}", templateId, projectId, templates);

    }

    /* @formatter:off */
    @UseCaseAdminUnassignsTemplateFromProject(
            @Step(
                    number = 2,
                    name = "service unassigns template from project",
                    description = "The service will request the template unassignment in domain 'scan' via synchronous event and updates mapping in domain 'administration' afterwards"))
    /* @formatter:on */
    public void unassignTemplateFromProject(String templateId, String projectId) {
        assertion.assertIsValidTemplateId(templateId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);
        Set<String> templates = project.getTemplates();
        LOG.debug("Start unassigning template '{}' from project '{}'. Formerly assgined templates : {}", templateId, projectId, templates);

        SecHubProjectTemplates result = sendUnassignRequestAndFetchResult(templateId, projectId);
        List<String> newTemplates = result.getTemplateIds();
        templates.clear();
        templates.addAll(newTemplates);

        projectTansactionService.saveInOwnTransaction(project);
        LOG.info("Unassigned template '{}' from project '{}'", templateId, projectId);

        LOG.debug("Project '{}' has now following templates: {}", templateId, projectId, templates);

    }

    @IsSendingSyncMessage(MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT)
    private SecHubProjectTemplates sendAssignRequestAndFetchResult(String templateId, String projectId) {

        DomainMessage message = new DomainMessage(MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT);
        SecHubProjectToTemplate mapping = new SecHubProjectToTemplate();
        mapping.setProjectId(projectId);
        mapping.setTemplateId(templateId);
        message.set(MessageDataKeys.PROJECT_TO_TEMPLATE, mapping);

        DomainMessageSynchronousResult result = eventBus.sendSynchron(message);
        if (result.hasFailed()) {
            throw new NotAcceptableException("Was not able to assign template to project.\nReason:" + result.getErrorMessage());
        }

        MessageID messageID = result.getMessageId();
        if (!(MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT.equals(messageID))) {
            throw new IllegalStateException("Result message id not supported: " + messageID);
        }

        return result.get(MessageDataKeys.PROJECT_TEMPLATES);

    }

    @IsSendingSyncMessage(MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT)
    private SecHubProjectTemplates sendUnassignRequestAndFetchResult(String templateId, String projectId) {

        DomainMessage message = new DomainMessage(MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT);
        SecHubProjectToTemplate mapping = new SecHubProjectToTemplate();
        mapping.setProjectId(projectId);
        mapping.setTemplateId(templateId);
        message.set(MessageDataKeys.PROJECT_TO_TEMPLATE, mapping);

        DomainMessageSynchronousResult result = eventBus.sendSynchron(message);
        if (result.hasFailed()) {
            throw new NotAcceptableException("Was not able to assign template to project.\nReason:" + result.getErrorMessage());
        }

        MessageID messageID = result.getMessageId();
        if (!(MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT.equals(messageID))) {
            throw new IllegalStateException("Result message id not supported: " + messageID);
        }

        return result.get(MessageDataKeys.PROJECT_TEMPLATES);

    }

}
