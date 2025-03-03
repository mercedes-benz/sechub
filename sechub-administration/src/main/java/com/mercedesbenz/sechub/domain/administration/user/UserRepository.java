// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

public interface UserRepository extends JpaRepository<User, String>, UserRepositoryCustom {

    Optional<User> findByOneTimeToken(String oneTimeToken);

    default User findOrFailUser(String userId) {
        Optional<User> foundUser = findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("User '" + userId + "' not found!");
        }
        return foundUser.get();
    }

    Optional<User> findByEmailAddress(String emailAddress);

    default User findOrFailUserByEmailAddress(String emailAddress) {
        Optional<User> foundUser = findByEmailAddress(emailAddress);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("No user with email address '" + emailAddress + "' found!");
        }
        return foundUser.get();
    }

    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.emailAddress) = lower(:email) ")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

}
