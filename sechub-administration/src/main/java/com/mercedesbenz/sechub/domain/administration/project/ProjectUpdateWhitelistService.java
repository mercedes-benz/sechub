// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectWhitelist;
import com.mercedesbenz.sechub.sharedkernel.validation.URIValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectUpdateWhitelistService {

    @Autowired
    AuditLogService auditLog;

    @Autowired
    ProjectRepository repository;

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    URIValidation uriValidation;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@UseCaseUpdateProjectWhitelist(
			@Step(number = 2,
			name = "Update project",
			description = "The service will update the <<section-shared-project, Project whitelist>>."))
	/* @formatter:on */
    public void updateProjectWhitelist(String projectId, @NotNull List<URI> whitelist) {
        auditLog.log("triggers update of whitelist for project {}. Allowed URIs shall be {}", logSanitizer.sanitize(projectId, 30), whitelist);

        assertion.assertIsValidProjectId(projectId);

        Optional<Project> found = repository.findById(projectId);
        if (!found.isPresent()) {
            throw new NotFoundException("Project '" + projectId + "' does not exist.");
        }
        /*
         * TODO Albert Tregnaghi, 2018-09-06: currently we check only role SUPER_ADMIN.
         * Because super admin is the only role having access. But when we got a project
         * owner, the access to this project must be checked too! Here we should use
         * permissions instead of roles then
         */
        Project project = found.get();
        Set<URI> oldWhiteList = project.getWhiteList();
        oldWhiteList.clear();
        whitelist.stream().filter(uri -> uriValidation.validate(uri).isValid()).forEach(oldWhiteList::add);

        repository.save(project);

        sendProjectCreatedEvent(project.getId(), project.getWhiteList());

    }

    @IsSendingAsyncMessage(MessageID.PROJECT_WHITELIST_UPDATED)
    private void sendProjectCreatedEvent(String projectId, Set<URI> whitelist) {
        DomainMessage request = new DomainMessage(MessageID.PROJECT_WHITELIST_UPDATED);
        ProjectMessage message = new ProjectMessage();
        message.setProjectId(projectId);
        message.setWhitelist(whitelist);
        request.set(MessageDataKeys.PROJECT_WHITELIST_UPDATE_DATA, message);

        eventBus.sendAsynchron(request);
    }
}
