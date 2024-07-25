// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

public interface ProjectRepository extends JpaRepository<Project, String>, ProjectRepositoryCustom {

    default Project findOrFailProject(String projectId) {
        Optional<Project> found = findById(projectId);
        if (found.isEmpty()) {
            throw new NotFoundException("Project '" + projectId + "' not found!");
        }
        return found.get();
    }
}
