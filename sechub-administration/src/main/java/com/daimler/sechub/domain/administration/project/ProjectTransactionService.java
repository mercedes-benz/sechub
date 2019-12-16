// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import static java.util.Objects.*;
@Service
public class ProjectTransactionService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectTransactionService.class);

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserInputAssertion assertion;

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Project saveInOwnTransaction(Project project) {
		requireNonNull(project, "Project may not be null!");

		/* store */
		Project result = projectRepository.save(project);
		LOG.debug("Saved project:{}", result.getId());
		return result;


	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Project saveInOwnTransaction(Project project, User user) {
		requireNonNull(project, "Project may not be null!");
		requireNonNull(user, "User may not be null!");

		/* store */

		Project result = projectRepository.save(project);
		LOG.debug("Saved project:{}", result.getId());
		userRepository.save(user);
		LOG.debug("Saved user:{}", result.getId());
		return result;


	}


	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void deleteWithAssociationsInOwnTransaction(String projectId) {
		projectRepository.deleteProjectWithAssociations(projectId);

		LOG.debug("Deleted project:{} with associations", projectId);
	}

}
