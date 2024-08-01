// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminGrantsAdminRightsToUser;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserGrantSuperAdminRightsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserGrantSuperAdminRightsService.class);

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    SecHubEnvironment sechubEnvironment;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@Validated
	@UseCaseAdminGrantsAdminRightsToUser(
			@Step(
					number = 2,
					name = "Service grants user admin rights.",
					next = { 3,	4 },
					description = "The service will grant user admin rights and triggers asynchronous events"))
	/* @formatter:on */
    public void grantSuperAdminRightsFor(String userId) {
        auditLogService.log("Triggered granting admin rights for user {}", logSanitizer.sanitize(userId, 30));

        assertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        if (user.isSuperAdmin()) {
            LOG.info("User:{} was already a super administrator, so just ignored", user.getName());
            return;
        }
        user.superAdmin = true;
        userRepository.save(user);

        requestUserRoleRecalculation(user);
        informUserBecomesSuperAdmin(user);

    }

    @IsSendingAsyncMessage(MessageID.USER_BECOMES_SUPERADMIN)
    private void informUserBecomesSuperAdmin(User user) {
        eventBusService
                .sendAsynchron(DomainMessageFactory.createUserBecomesSuperAdmin(user.getName(), user.getEmailAddress(), sechubEnvironment.getServerBaseUrl()));
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void requestUserRoleRecalculation(User user) {
        eventBusService.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

}
