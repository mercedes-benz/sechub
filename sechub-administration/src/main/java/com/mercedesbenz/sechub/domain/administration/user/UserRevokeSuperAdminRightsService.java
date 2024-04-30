// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminRevokesAdminRightsFromAdmin;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserRevokeSuperAdminRightsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRevokeSuperAdminRightsService.class);

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    SecHubEnvironment secHubEnvironment;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@Validated
	@UseCaseAdminRevokesAdminRightsFromAdmin(
			@Step(
					number = 2,
					name = "Service revokes user admin rights.",
					next = { 3,	4 },
					description = "The service will revoke user admin righs and triggers asynchronous events"))
	/* @formatter:on */
    public void revokeSuperAdminRightsFrom(String userId) {
        String sanitizedLogUserId = logSanitizer.sanitize(userId, 30);
        auditLogService.log("Triggered revoking admin rights from user {}", sanitizedLogUserId);

        assertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        if (!user.isSuperAdmin()) {
            LOG.info("User:{} was already no super administrator, so just ignored", sanitizedLogUserId);
            return;
        }
        assertNotLastSuperAdmin();

        user.superAdmin = false;
        userRepository.save(user);

        requestUserRoleRecalculaton(user);
        informUserNoLongerSuperadmin(user);

    }

    private void assertNotLastSuperAdmin() {
        User exampleUser = new User();
        exampleUser.superAdmin = true;
        long count = userRepository.count(Example.of(exampleUser));
        if (count < 2) {
            throw new NotAcceptableException("Would be last super admin. So cannot revoke admin rights!");
        }
    }

    @IsSendingAsyncMessage(MessageID.USER_NO_LONGER_SUPERADMIN)
    private void informUserNoLongerSuperadmin(User user) {
        eventBusService
                .sendAsynchron(DomainMessageFactory.createUserNoLongerSuperAdmin(user.getName(), user.getEmailAddress(), secHubEnvironment.getServerBaseUrl()));
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void requestUserRoleRecalculaton(User user) {
        eventBusService.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

}
