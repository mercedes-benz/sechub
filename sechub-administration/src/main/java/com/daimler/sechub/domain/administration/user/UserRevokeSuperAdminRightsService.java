// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.logforgery.LogSanitizer;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorRevokesAdminRightsFromAdmin;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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
	@UseCaseAdministratorRevokesAdminRightsFromAdmin(
			@Step(
					number = 2,
					name = "Service revokes user admin rights.",
					next = { 3,	4 },
					description = "The service will revoke user admin righs and triggers asynchronous events"))
	/* @formatter:on */
	public void revokeSuperAdminRightsFrom(String userId) {
		auditLogService.log("Triggered revoking admin rights from user {}",logSanitizer.sanitize(userId,30));

		assertion.isValidUserId(userId);

		User user = userRepository.findOrFailUser(userId);

		if (!user.isSuperAdmin()) {
			LOG.info("User:{} was already no super administrator, so just ignored",userId);
			return;
		}
		assertNotLastSuperAdmin();

		user.superAdmin=false;
		userRepository.save(user);

		requestUserRoleRecalculaton(user);
		informUserNoLongerSuperadmin(user);

	}

	private void assertNotLastSuperAdmin() {
		User exampleUser = new User();
		exampleUser.superAdmin = true;
		long count = userRepository.count(Example.of(exampleUser));
		if (count<2) {
			throw new NotAcceptableException("Would be last super admin. So cannot revoke admin rights!");
		}
	}

	@IsSendingAsyncMessage(MessageID.USER_NO_LONGER_SUPERADMIN)
	private void informUserNoLongerSuperadmin(User user) {
		eventBusService.sendAsynchron(DomainMessageFactory.createUserNoLongerSuperAdmin(user.getName(), user.getEmailAdress(), secHubEnvironment.getServerBaseUrl()));
	}

	@IsSendingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
	private void requestUserRoleRecalculaton(User user) {
		eventBusService.sendAsynchron(DomainMessageFactory.createRequestRoleCalculation(user.getName()));
	}

}
