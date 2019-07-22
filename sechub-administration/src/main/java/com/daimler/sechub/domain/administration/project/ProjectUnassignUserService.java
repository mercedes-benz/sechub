// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.util.Arrays;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.AlreadyExistsException;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorUnassignsUserFromProject;


@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectUnassignUserService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectUnassignUserService.class);

	@Autowired
	DomainMessageService eventBus;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	UserRepository userRepository;

	/* @formatter:off */
	@UseCaseAdministratorUnassignsUserFromProject(@Step(number = 2, name = "Unassign user", description = "The service will remove the user to the project. If users has no longer access to projects ROLE_USER will be removed"))
	/* @formatter:on */
	public void unassignUserFromProject(String userId, String projectId) {
		Project project = projectRepository.findOrFailProject(projectId);
		User user = userRepository.findOrFailUser(userId);
		if (!project.getUsers().remove(user)) {
			throw new AlreadyExistsException("User already not assigned to this project!");
		}
		user.getProjects().remove(project);

		projectRepository.save(project);
		userRepository.save(user);

		LOG.debug("Persisted assignment of user:{} to project:{}", user.getName(), project.getId());

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
