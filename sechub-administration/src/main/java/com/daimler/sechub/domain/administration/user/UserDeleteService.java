// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.logforgery.LogSanitizer;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorDeletesUser;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserDeleteService {

	private static final Logger LOG = LoggerFactory.getLogger(UserDeleteService.class);

	@Autowired
	DomainMessageService eventBusService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserContextService userContext;

	@Autowired
	AuditLogService auditLogService;

	@Autowired
	LogSanitizer logSanitizer;

	/* @formatter:off */
	@Validated
	@UseCaseAdministratorDeletesUser(
			@Step(
					number = 2,
					name = "Service deletes user.",
					next = { 3,	4 },
					description = "The service will delete the user with dependencies and triggers asynchronous events"))
	/* @formatter:on */
	public void deleteUser(String userId) {
		auditLogService.log("Triggers delete of user {}",logSanitizer.sanitize(userId,30));
		if (userId==null) {
			LOG.warn("Username was null! Should not happen");
			return;
		}
		if (userId.contentEquals(userContext.getUserId())) {
			throw new NotAcceptableException("You are not allowed to delte yourself!");
		}

		User user = userRepository.findOrFailUser(userId);

		/* create message containing data before user is deleted */
		UserMessage message = new UserMessage();
		message.setUserId(user.getName());
		message.setEmailAdress(user.getEmailAdress());

		userRepository.delete(user);

		informUserDeleted(message);

	}

	@IsSendingAsyncMessage(MessageID.USER_DELETED)
	private void informUserDeleted(UserMessage message) {

		DomainMessage infoRequest = new DomainMessage(MessageID.USER_DELETED);
		infoRequest.set(MessageDataKeys.USER_DELETE_DATA, message);

		eventBusService.sendAsynchron(infoRequest);
	}

}
