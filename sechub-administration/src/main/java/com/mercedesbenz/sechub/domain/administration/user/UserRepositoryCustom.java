// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;

public interface UserRepositoryCustom {

    /**
     * Delete user with associations (e.g. project2user). But will throw a
     * {@link NotAcceptableException} when the user is still an owner of a project!
     *
     * @param userId
     */
    public void deleteUserWithAssociations(String userId);

    /**
     * Count amount of projects where user is owner
     *
     * @param userId
     * @return amount of owned projects
     */
    public int countAmountOfOwnedProjects(String userId);
}
