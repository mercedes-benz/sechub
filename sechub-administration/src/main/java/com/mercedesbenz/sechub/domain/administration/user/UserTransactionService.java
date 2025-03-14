// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTransactionService {

    private final static Logger logger = LoggerFactory.getLogger(UserTransactionService.class.getName());
    private final UserRepository userRepository;

    public UserTransactionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User saveInOwnTransaction(User user) {
        requireNonNull(user);

        User result = userRepository.save(user);

        logger.debug("Saved user: {}", user.getName());
        return result;
    }
}
