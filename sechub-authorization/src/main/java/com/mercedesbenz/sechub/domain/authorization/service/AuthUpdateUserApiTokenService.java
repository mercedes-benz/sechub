// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.authorization.AuthUser;
import com.mercedesbenz.sechub.domain.authorization.AuthUserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AuthUpdateUserApiTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthUpdateUserApiTokenService.class);

    @Autowired
    AuthUserRepository userRepo;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseUserClicksLinkToGetNewAPIToken(@Step(number = 3, next = { Step.NO_NEXT_STEP }, name = "Update auth data"))
    public void updateAPIToken(String userId, String hashedApiToken) {
        assertion.assertIsValidUserId(userId);

        AuthUser user = userRepo.findOrFail(userId);
        user.setHashedApiToken(hashedApiToken);
        userRepo.save(user);

        LOG.debug("API token for user:{} updated", userId);

    }

}
