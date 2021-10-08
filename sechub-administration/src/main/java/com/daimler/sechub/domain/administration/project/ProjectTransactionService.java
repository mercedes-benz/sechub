// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static java.util.Objects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;

@Service
public class ProjectTransactionService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectTransactionService.class);

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ProjectMetaDataEntityRepository metaDataRepository;

	@Autowired
	UserRepository userRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Project saveInOwnTransaction(Project project) {
		requireNonNull(project, "Project may not be null!");

		/* store */
		Project result = projectRepository.save(project);
		
		metaDataRepository.saveAll(project.metaData);
		
		LOG.debug("Saved project:{}", result.getId());
		return result;
	}

	/**
	 * Persists a project, user entity and also project meta data in same (new) transaction
	 * @param project
	 * @param users
	 * @return persisted project
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Project saveInOwnTransaction(Project project, User ... users) {
		requireNonNull(project, "Project may not be null!");
		requireNonNull(users, "User may not be null!");

		/* store */
		Project savedProject = projectRepository.save(project);
		LOG.debug("Saved project:{}", savedProject.getId());
		
		metaDataRepository.saveAll(project.metaData);
		LOG.debug("Saved metadata for project:{}", savedProject.getId());

		for (User user: users) {
		    User savedUser = userRepository.save(user);
		    LOG.debug("Saved user:{}", savedUser.getName());
		}
		
		return savedProject;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteWithAssociationsInOwnTransaction(String projectId) {
		requireNonNull(projectId, "projectId may not be null!");

		/* store */
		projectRepository.deleteProjectWithAssociations(projectId);

		LOG.debug("Deleted project:{} with associations", projectId);
	}

}
