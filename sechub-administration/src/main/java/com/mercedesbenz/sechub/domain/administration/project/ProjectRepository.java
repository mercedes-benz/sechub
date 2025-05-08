// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mercedesbenz.sechub.domain.administration.user.User;
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

    /* @formatter:off */
    @Query(value = "SELECT p2u." + Project.ASSOCIATE_PROJECT_TO_USER_COLUMN_PROJECT_ID
            + " FROM " + Project.TABLE_NAME_PROJECT_TO_USER + " p2u"
            + " WHERE "+Project.ASSOCIATE_PROJECT_TO_USER_COLUMN_USER_ID+" = :userId ORDER BY p2u." + Project.ASSOCIATE_PROJECT_TO_USER_COLUMN_PROJECT_ID, nativeQuery = true)
    /* @formatter:on */
    Set<String> findAllProjectIdsWhereUserIsAssigned(String userId);

    /* @formatter:off */
    @Query(value = "SELECT p." + Project.COLUMN_PROJECT_ID
            + " FROM " + Project.TABLE_NAME + " p"
            + " WHERE "+Project.COLUMN_PROJECT_OWNER+" = :userId ORDER BY p." + Project.COLUMN_PROJECT_ID, nativeQuery = true)
    /* @formatter:on */
    Set<String> findAllProjectIdsWhereUserIsOwner(String userId);

    @Query(value = "SELECT new " + ProjectUserData.FULL_CLASSNAME + "(u." + User.PROPERTY_USER_NAME + ", u." + User.PROPERTY_USER_EMAILADDRESS + ") FROM "
            + Project.CLASS_NAME + " p JOIN p." + Project.PROPERTY_OWNER + " u WHERE p." + Project.PROPERTY_ID + " = :projectId")
    ProjectUserData fetchProjectUserDataForOwner(String projectId);

    @Query(value = "SELECT new " + ProjectUserData.FULL_CLASSNAME + "(u." + User.PROPERTY_USER_NAME + ", u." + User.PROPERTY_USER_EMAILADDRESS + ") FROM "
            + Project.CLASS_NAME + " p JOIN p." + Project.PROPERTY_USERS + " u WHERE p." + Project.PROPERTY_ID + " = :projectId order by u."
            + User.PROPERTY_USER_NAME)
    List<ProjectUserData> fetchOrderedProjectUserDataForAssignedUsers(String projectId);

}
