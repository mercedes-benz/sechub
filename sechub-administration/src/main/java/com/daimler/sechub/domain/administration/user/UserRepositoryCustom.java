// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

public interface UserRepositoryCustom {

	/**
	 * Delete user with associations (e.g. project2user).
	 * But will throw a {@link NotAcceptableException} when
	 * the user is still an owner of a project!
	 * @param userId
	 */
	public void deleteUserWithAssociations(String userId);
}
