// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final String QUERY_DELETE_PROJECT_TO_USER = "delete from " + Project.TABLE_NAME_PROJECT_TO_USER + " p2u where p2u."
            + Project.ASSOCIATE_PROJECT_TO_USER_COLUMN_PROJECT_ID + " = ?1";
    private static final String QUERY_DELETE_PROJECT_TO_URI = "delete from " + Project.TABLE_NAME_PROJECT_WHITELIST_URI + " p2w where p2w."
            + Project.ASSOCIATE_PROJECT_TO_URI_COLUMN_PROJECT_ID + " = ?1";
    private static final String QUERY_DELETE_PROJECT_TO_METADATA = "delete from " + Project.TABLE_NAME_PROJECT_METADATA + " p2w where p2w."
            + Project.ASSOCIATE_PROJECT_TO_METADATA_COLUMN_PROJECT_ID + " = ?1";
    private static final String QUERY_DELETE_PROJECT = "delete from " + Project.TABLE_NAME + " p where p." + Project.COLUMN_PROJECT_ID + " = ?1";

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

        Query deleteProject = em.createNativeQuery(QUERY_DELETE_PROJECT);
        deleteProject.setParameter(1, projectId);
        deleteProject.executeUpdate();

    }

}
