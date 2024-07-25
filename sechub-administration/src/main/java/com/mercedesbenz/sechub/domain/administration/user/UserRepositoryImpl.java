// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final String QUERY_DELETE_PROJECT_TO_USER = "delete from " + User.TABLE_NAME_PROJECT_TO_USER + " p2u where p2u."
            + User.ASSOCIATE_PROJECT_TO_USER_COLUMN_USER_ID + " = ?1";
    private static final String QUERY_DELETE_USER = "delete from " + User.TABLE_NAME + " u where u." + User.COLUMN_USER_ID + " = ?1";
    private static final String QUERY_COUNT_USER_IS_OWNER_OF_PROJECT = "select count(p." + Project.COLUMN_PROJECT_ID + ") from " + Project.TABLE_NAME
            + " p where p." + Project.COLUMN_PROJECT_OWNER + " = ?1";

    @Override
    public void deleteUserWithAssociations(String userId) {

        int count = countAmountOfOwnedProjects(userId);
        if (count > 0) {
            throw new NotAcceptableException("User " + userId + " is " + count + " times still owner of a project! Move ownership before delete!");
        }

        Query deleteProjectToUser = em.createNativeQuery(QUERY_DELETE_PROJECT_TO_USER);
        deleteProjectToUser.setParameter(1, userId);
        deleteProjectToUser.executeUpdate();

        Query deleteUser = em.createNativeQuery(QUERY_DELETE_USER);
        deleteUser.setParameter(1, userId);
        deleteUser.executeUpdate();

    }

    @Override
    public int countAmountOfOwnedProjects(String userId) {

        Query countUserIsOwnerOfProject = em.createNativeQuery(QUERY_COUNT_USER_IS_OWNER_OF_PROJECT);
        countUserIsOwnerOfProject.setParameter(1, userId);

        Object result = countUserIsOwnerOfProject.getSingleResult();
        Number number = (Number) result;
        return number.intValue();
    }

}
