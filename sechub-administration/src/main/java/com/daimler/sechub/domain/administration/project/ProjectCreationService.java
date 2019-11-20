// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.AlreadyExistsException;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorCreatesProject;
import com.daimler.sechub.sharedkernel.validation.URIValidation;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectCreationService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectCreationService.class);

	@Autowired
	UserContextService userContext;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	DomainMessageService eventBus;


	@Autowired
	UserRepository userRepository;

	@Autowired
	URIValidation uriValidation;

	@Autowired
	UserInputAssertion assertion;

	@Validated
	/* @formatter:off */
	@UseCaseAdministratorCreatesProject(
			@Step(number = 2,
			name = "Create project",
			description = "The service will create the project when not already existing with such name."))
	/* @formatter:on */
	public void createProject(@NotNull String projectId, @NotNull String description, @NotNull String owner, @NotNull Set<URI> whitelist) {
		LOG.info("Administrator {} triggers create of project:{}, having owner:{}",userContext.getUserId(),projectId,owner);

		assertion.isValidProjectId(projectId);
		assertion.isValidUserId(owner);
		assertion.isvalidProjectDescription(owner);

		/* assert found */
		Optional<Project> foundProject = projectRepository.findById(projectId);
		if (foundProject.isPresent()) {
			throw new AlreadyExistsException("Project '" + projectId + "' already exists");
		}

		Optional<User> foundOwner = userRepository.findById(owner);
		if (!foundOwner.isPresent()) {
			throw new NotFoundException("Owner '" + owner + "' not found");
		}
		/* setup */
		Project project = new Project();
		project.id = projectId;
		project.description = description;

		User ownerUser = foundOwner.get();
		project.owner=ownerUser;
		/** add only accepted/valid URIs - sanitize */
		whitelist.stream().filter(uri -> uriValidation.validate(uri).isValid()).forEach(project.getWhiteList()::add);

		/* store */
		projectRepository.save(project);

		LOG.debug("Persisted new project:{}", project.getId());

		sendProjectCreatedEvent(projectId, whitelist);
		sendRefreshUserAuth(ownerUser);

	}

	@IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
	private void sendRefreshUserAuth(User ownerUser) {
		eventBus.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(ownerUser.getName()));
	}

	@IsSendingAsyncMessage(MessageID.PROJECT_CREATED)
	private void sendProjectCreatedEvent(String projectId, Set<URI> whitelist) {
		DomainMessage request = new DomainMessage(MessageID.PROJECT_CREATED);
		ProjectMessage message = new ProjectMessage();
		message.setProjectId(projectId);
		message.setWhitelist(whitelist);
		request.set(MessageDataKeys.PROJECT_CREATION_DATA,message);

		eventBus.sendAsynchron(request);
	}

}
