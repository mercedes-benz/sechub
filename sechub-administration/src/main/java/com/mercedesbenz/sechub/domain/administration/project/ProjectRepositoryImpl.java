// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.domain.administration.project.Project.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
/* @formatter:off */
    private static final String QUERY_DELETE_PROJECT_TO_USER = "delete from " + TABLE_NAME_PROJECT_TO_USER + " p2u where p2u." + ASSOCIATE_PROJECT_TO_USER_COLUMN_PROJECT_ID + " = ?1";

    private static final String QUERY_DELETE_PROJECT_TO_URI = "delete from " + TABLE_NAME_PROJECT_WHITELIST_URI + " p2w where p2w." + Project.ASSOCIATE_PROJECT_TO_URI_COLUMN_PROJECT_ID + " = ?1";

    private static final String QUERY_DELETE_PROJECT_TO_METADATA = "delete from " + TABLE_NAME_PROJECT_METADATA + " p2w where p2w." + Project.ASSOCIATE_PROJECT_TO_METADATA_COLUMN_PROJECT_ID + " = ?1";

    private static final String QUERY_DELETE_PROJECT_TO_TEMPLATE = "delete from " + TABLE_NAME_PROJECT_TEMPLATES + " p2w where p2w." + Project.ASSOCIATE_PROJECT_TO_TEMPLATE_COLUMN_PROJECT_ID + " = ?1";

    private static final String QUERY_DELETE_PROJECT = "delete from " + TABLE_NAME + " p where p." + Project.COLUMN_PROJECT_ID + " = ?1";
    /* @formatter:on */

    @Override
    public void deleteProjectWithAssociations(String projectId) {
        Query deleteProjectToUser = em.createNativeQuery(QUERY_DELETE_PROJECT_TO_USER);
        deleteProjectToUser.setParameter(1, projectId);
        deleteProjectToUser.executeUpdate();

        Query deleteProjectToURI = em.createNativeQuery(QUERY_DELETE_PROJECT_TO_URI);
        deleteProjectToURI.setParameter(1, projectId);
        deleteProjectToURI.executeUpdate();

        Query deleteProjectToMetaData = em.createNativeQuery(QUERY_DELETE_PROJECT_TO_METADATA);
        deleteProjectToMetaData.setParameter(1, projectId);
        deleteProjectToMetaData.executeUpdate();

        Query deleteProjectToTemplate = em.createNativeQuery(QUERY_DELETE_PROJECT_TO_TEMPLATE);
        deleteProjectToTemplate.setParameter(1, projectId);
        deleteProjectToTemplate.executeUpdate();

        Query deleteProject = em.createNativeQuery(QUERY_DELETE_PROJECT);
        deleteProject.setParameter(1, projectId);
        deleteProject.executeUpdate();

    }

}
