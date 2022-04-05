// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.authorization.AuthUserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AuthUserDeleteService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthUserDeleteService.class);

    @Autowired
    AuthUserRepository authUserRepository;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseAdminDeletesUser(@Step(number = 4, next = {
            Step.NO_NEXT_STEP }, name = "Delete user access", description = "Authorization layer is informed about user deltete and removes access to sechub. But without any project information"))
    public void deleteUser(String userId) {

        assertion.assertIsValidUserId(userId);

        authUserRepository.deleteById(userId);
        LOG.info("Deleted auth user:{}", userId);
    }

}
