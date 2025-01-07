// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectTemplateData;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;
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
        changeTemplateAssignment(templateId, projectId, (t, p) -> fetchAssignRequestResult(t, p), "assigned to");
    }

    /* @formatter:off */
    @UseCaseAdminUnassignsTemplateFromProject(
            @Step(
                    number = 2,
                    name = "service unassigns template from project",
                    description = "The service will request the template unassignment in domain 'scan' via synchronous event and updates mapping in domain 'administration' afterwards"))
    /* @formatter:on */
    public void unassignTemplateFromProject(String templateId, String projectId) {
        changeTemplateAssignment(templateId, projectId, (t, p) -> fetchUnassignmentRequestResult(t, p), "unassigned from");
    }

    private void changeTemplateAssignment(String templateId, String projectId, TemplateChangeResultFetcher fetcher, String assignOrUnassignInfo) {
        assertion.assertIsValidTemplateId(templateId);
        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);
        Set<String> templateIds = project.getTemplateIds();
        LOG.debug("Project '{}' has following template ids: {}", projectId, templateIds);

        SecHubProjectTemplateData result = fetcher.fetchTemplateAssignmentChangeResult(templateId, projectId);
        List<String> newTemplateIds = result.getTemplateIds();
        templateIds.clear();
        templateIds.addAll(newTemplateIds);

        projectTansactionService.saveInOwnTransaction(project);
        LOG.info("Template '{}' has been {} project '{}'", templateId, assignOrUnassignInfo, projectId);

        LOG.debug("Project '{}' has following template ids: {}", projectId, templateIds);
    }

    @IsSendingSyncMessage(MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT)
    private SecHubProjectTemplateData fetchAssignRequestResult(String templateId, String projectId) {
        return sendSynchronousProjectTemplateChangeEvent(templateId, projectId, MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT,
                MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT);

    }

    @IsSendingSyncMessage(MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT)
    private SecHubProjectTemplateData fetchUnassignmentRequestResult(String templateId, String projectId) {
        return sendSynchronousProjectTemplateChangeEvent(templateId, projectId, MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT,
                MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT);
    }

    /*
     * This method sends a synchronous event to event bus and waits that the
     * assignment is done inside other domain (in this case we know it is inside the
     * scan domain). When this is done, the change event was successful and the
     * event result contains SecHubProjectTemplateData which can be used inside
     * administration domain further.
     */
    private SecHubProjectTemplateData sendSynchronousProjectTemplateChangeEvent(String templateId, String projectId, MessageID requestMessageId,
            MessageID acceptedResultMessageId) {

        DomainMessage message = new DomainMessage(requestMessageId);

        SecHubProjectToTemplate mapping = new SecHubProjectToTemplate();
        mapping.setProjectId(projectId);
        mapping.setTemplateId(templateId);
        message.set(MessageDataKeys.PROJECT_TO_TEMPLATE, mapping);

        DomainMessageSynchronousResult result = eventBus.sendSynchron(message);

        if (result.hasFailed()) {
            throw new NotAcceptableException("Was not able to change template to project assignment.\nReason: " + result.getErrorMessage());
        }
        MessageID messageID = result.getMessageId();
        if (!(acceptedResultMessageId.equals(messageID))) {
            throw new IllegalStateException("Result message id not supported: " + messageID);
        }
        return result.get(MessageDataKeys.PROJECT_TEMPLATES);
    }

    private interface TemplateChangeResultFetcher {
        public SecHubProjectTemplateData fetchTemplateAssignmentChangeResult(String templateId, String projectId);
    }
}
