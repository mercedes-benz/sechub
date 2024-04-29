// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.transaction.Transactional;

@Service
public class ScanRevokeUserAccessAtAllService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanRevokeUserAccessAtAllService.class);

    @Autowired
    ScanAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Transactional
    @UseCaseAdminDeletesUser(@Step(number = 3, name = "revoke user from schedule access"))
    public void revokeUserAccess(String userId) {
        assertion.assertIsValidUserId(userId);

        repository.deleteAcessForUserAtAll(userId);

        LOG.info("Revoked access at all for user:{}", userId);
    }

}
