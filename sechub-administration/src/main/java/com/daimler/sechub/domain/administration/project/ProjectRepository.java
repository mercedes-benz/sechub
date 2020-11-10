// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.sechub.sharedkernel.error.NotFoundException;

public interface ProjectRepository extends JpaRepository<Project,String>, ProjectRepositoryCustom {

	public default Project findOrFailProject(String projectId) {
		Optional<Project> found = findById(projectId);
		if (! found.isPresent()) {
			throw new NotFoundException("Project '" + projectId + "' not found!");
		}
		return found.get();
	}
}
