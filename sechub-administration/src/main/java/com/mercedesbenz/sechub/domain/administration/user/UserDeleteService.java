// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserDeleteService {

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

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@Validated
	@UseCaseAdminDeletesUser(
			@Step(
					number = 2,
					name = "Service deletes user.",
					next = { 3,	4 },
					description = "The service will delete the user with dependencies and triggers asynchronous events"))
	/* @formatter:on */
    @Transactional
    public void deleteUser(String userId) {
        auditLogService.log("Triggers delete of user {}", logSanitizer.sanitize(userId, 30));

        assertion.assertIsValidUserId(userId);
        if (userId.contentEquals(userContext.getUserId())) {
            throw new NotAcceptableException("You are not allowed to delete yourself!");
        }

        User user = userRepository.findOrFailUser(userId);

        /* create message containing data before user is deleted */
        UserMessage message = new UserMessage();
        message.setUserId(user.getName());
        message.setEmailAddress(user.getEmailAddress());

        userRepository.deleteUserWithAssociations(user.getName());

        informUserDeleted(message);

    }

    @IsSendingAsyncMessage(MessageID.USER_DELETED)
    private void informUserDeleted(UserMessage message) {

        DomainMessage infoRequest = new DomainMessage(MessageID.USER_DELETED);
        infoRequest.set(MessageDataKeys.USER_DELETE_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

}
