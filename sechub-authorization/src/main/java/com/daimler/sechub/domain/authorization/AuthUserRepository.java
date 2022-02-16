// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.authorization;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.daimler.sechub.sharedkernel.error.NotFoundException;

public interface AuthUserRepository extends JpaRepository<AuthUser, String> {

    Optional<AuthUser> findByUserId(String userid);

    @Query(AuthUser.QUERY_COUNT_SUPERADMINS)
    int countAmountOfSuperAdmins();

    public default AuthUser findOrFail(String userId) {
        Optional<AuthUser> found = findByUserId(userId);
        if (!found.isPresent()) {
            throw new NotFoundException("no user found :" + userId);
        }
        return found.get();
    }
}
