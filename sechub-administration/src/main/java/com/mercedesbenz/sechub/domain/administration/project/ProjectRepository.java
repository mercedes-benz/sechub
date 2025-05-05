// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

public interface ProjectRepository extends JpaRepository<Project, String>, ProjectRepositoryCustom {

    default Project findOrFailProject(String projectId) {
        Optional<Project> found = findById(projectId);
        if (found.isEmpty()) {
            throw new NotFoundException("Project '" + projectId + "' not found!");
        }
        return found.get();
    }

    /**
     * Fetches all project identifiers
     *
     * @return project Id's, ordered ascending
     */
    @Query(value = "SELECT p." + Project.COLUMN_PROJECT_ID + " FROM " + Project.TABLE_NAME + " p ORDER BY p." + Project.COLUMN_PROJECT_ID, nativeQuery = true)
    List<String> findAllProjectIdsOrdered();
}
