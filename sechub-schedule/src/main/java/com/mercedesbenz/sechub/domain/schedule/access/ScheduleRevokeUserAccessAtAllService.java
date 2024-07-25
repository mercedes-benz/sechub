// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.transaction.Transactional;

@Service
public class ScheduleRevokeUserAccessAtAllService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleRevokeUserAccessAtAllService.class);

    @Autowired
    ScheduleAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Transactional
    @UseCaseAdminDeletesUser(@Step(number = 3, name = "revoke user from schedule access"))
    public void revokeUserAccess(String userId) {
        assertion.assertIsValidUserId(userId);

        repository.deleteAccessForUserAtAll(userId);

        LOG.info("Revoked access at all for user:{}", userId);
    }

}
