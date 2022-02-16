// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.signup;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.sechub.sharedkernel.error.NotFoundException;

public interface SignupRepository extends JpaRepository<Signup, String> {

    public default Signup findOrFailSignup(String userId) {
        Optional<Signup> foundSignup = findById(userId);
        if (!foundSignup.isPresent()) {
            throw new NotFoundException("Signup '" + userId + "' not found!");
        }
        return foundSignup.get();
    }
}
