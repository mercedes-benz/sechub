// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.authorization.AuthUser;
import com.mercedesbenz.sechub.domain.authorization.AuthUserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminAcceptsSignup;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AuthUserCreationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthUserCreationService.class);

    @Autowired
    AuthUserRepository userRepo;

    @Lazy
    @Autowired
    DomainMessageService eventBus;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseAdminAcceptsSignup(@Step(number = 4, next = {
            Step.NO_NEXT_STEP }, name = "Give user access", description = "Authorization layer is informed about new user and gives access to sechub. But without any project information"))
    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    public void createUser(String userId, String hashedApiToken) {
        assertion.assertIsValidUserId(userId);

        Optional<AuthUser> found = userRepo.findByUserId(userId);
        if (found.isPresent()) {
            LOG.warn("Will skip user create action because user already found with name:{}", userId);
            return;
        }
        AuthUser user = new AuthUser();
        user.setUserId(userId);
        userRepo.save(user);

        LOG.info("Created auth user:{}", userId);

        eventBus.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(userId));
    }

}
