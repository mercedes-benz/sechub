// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectWhitelist;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectUpdateWhitelistService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectUpdateWhitelistService.class);
	@Autowired
	UserContextService userContext;

	@Autowired
	ProjectRepository repository;

	@Autowired
	DomainMessageService eventBus;

	@Validated
	/* @formatter:off */
	@UseCaseUpdateProjectWhitelist(
			@Step(number = 2,
			name = "Update project",
			description = "The service will update the <<section-shared-project, Project whitelist>>."))
	/* @formatter:on */
	public void updateProjectWhitelist(@NotNull String projectId, @NotNull List<URI> whitelist) {
		LOG.info("User {} triggers update of whitelist for project {}. Allowed URIs shall be {}",
				userContext.getUserId(), projectId, whitelist);

		Optional<Project> found = repository.findById(projectId);
		if (!found.isPresent()) {
			throw new NotFoundException("Project '" + projectId + "' does not exist or you have now ");
		}
		/*
		 * TODO Albert Tregnaghi, 2018-09-06: currently we check only role SUPER_ADMIN. Because
		 * super admin is the only role having access. But when we got a project owner,
		 * the access to this project must be checked too!
		 */
		Project project = found.get();
		Set<URI> oldWhiteList = project.getWhiteList();
		oldWhiteList.clear();
		oldWhiteList.addAll(whitelist);

		repository.save(project);

		LOG.debug("Updated whitelist for project {}", project.getId());

		sendProjectCreatedEvent(project.getId(),project.getWhiteList());

	}

	@IsSendingAsyncMessage(MessageID.PROJECT_WHITELIST_UPDATED)
	private void sendProjectCreatedEvent(String projectId, Set<URI> whitelist) {
		DomainMessage request = new DomainMessage(MessageID.PROJECT_WHITELIST_UPDATED);
		ProjectMessage message = new ProjectMessage();
		message.setProjectId(projectId);
		message.setWhitelist(whitelist);
		request.set(MessageDataKeys.PROJECT_WHITELIST_UPDATE_DATA,message);

		eventBus.sendAsynchron(request);
	}
}
