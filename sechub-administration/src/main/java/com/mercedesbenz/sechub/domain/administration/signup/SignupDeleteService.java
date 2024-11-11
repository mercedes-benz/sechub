// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminDeletesSignup;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class SignupDeleteService {

    @Autowired
    SignupRepository userSelfRegistrationRepository;

    @Autowired
    AuditLogService auditLog;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseAdminDeletesSignup(@Step(number = 2, name = "Persistence", description = "Existing signup will be deleted"))
    public void delete(String userId) {
        auditLog.log("triggered delete of user signup: {}", logSanitizer.sanitize(userId, 30));

        assertion.assertIsValidUserId(userId);

        Signup foundByName = userSelfRegistrationRepository.findOrFailSignup(userId);
        userSelfRegistrationRepository.delete(foundByName);

    }

}
