// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdminGrantsAdminRightsToUser;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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
					description = "The service will grant user admin righs and triggers asynchronous events"))
	/* @formatter:on */
    public void grantSuperAdminRightsFor(String userId) {
        auditLogService.log("Triggered granting admin rights for user {}", logSanitizer.sanitize(userId, 30));

        assertion.isValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        if (user.isSuperAdmin()) {
            LOG.info("User:{} was already a super administrator, so just ignored", user.getName());
            return;
        }
        user.superAdmin = true;
        userRepository.save(user);

        requestUserRoleRecalculaton(user);
        informUserBecomesSuperadmin(user);

    }

    @IsSendingAsyncMessage(MessageID.USER_BECOMES_SUPERADMIN)
    private void informUserBecomesSuperadmin(User user) {
        eventBusService
                .sendAsynchron(DomainMessageFactory.createUserBecomesSuperAdmin(user.getName(), user.getEmailAdress(), sechubEnvironment.getServerBaseUrl()));
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void requestUserRoleRecalculaton(User user) {
        eventBusService.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
    }

}
