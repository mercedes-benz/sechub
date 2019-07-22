// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.sechub.sharedkernel.error.NotFoundException;

public interface UserRepository extends JpaRepository<User, String>{

	Optional<User> findByOneTimeToken(String oneTimeToken);
	
	public default User findOrFailUser(String userId) {
		Optional<User> foundUser  = findById(userId);
		if (! foundUser.isPresent()) {
			throw new NotFoundException("User '" + userId + "' not found!");
		}
		return foundUser.get();
	}

	Optional<User> findByEmailAdress(String emailAdress);
}
