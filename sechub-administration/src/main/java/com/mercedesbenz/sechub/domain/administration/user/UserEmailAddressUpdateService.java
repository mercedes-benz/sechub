// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUpdatesUserEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserEmailAddressUpdateService {

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@Validated
	@UseCaseAdminUpdatesUserEmailAddress(
			@Step(
					number = 2,
					name = "Service updates user email address.",
					next = { 3 },
					description = "The service will update the user email address and also trigger events."))
	/* @formatter:on */
    @Transactional
    public void updateUserEmailAddress(String userId, String newEmailAddress) {
        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidEmailAddress(newEmailAddress);

        User user = userRepository.findOrFailUser(userId);
        String formerEmailAddress = user.getEmailAddress();

        if (newEmailAddress.equalsIgnoreCase(formerEmailAddress)) {
            throw new NotAcceptableException("User has already this email address");
        }
        /* parameters valid, we audit log the change */
        auditLogService.log("Changed email address of user {}", logSanitizer.sanitize(userId, 30));

        user.emailAddress = newEmailAddress;

        /* create message containing data before user email has changed */
        UserMessage message = new UserMessage();
        message.setUserId(user.getName());
        message.setEmailAddress(user.getEmailAddress());
        message.setFormerEmailAddress(formerEmailAddress);
        message.setSubject("A SecHub administrator has changed your email address");

        userRepository.save(user);

        informUserEmailAddressUpdated(message);

    }

    @IsSendingAsyncMessage(MessageID.USER_EMAIL_ADDRESS_CHANGED)
    private void informUserEmailAddressUpdated(UserMessage message) {

        DomainMessage infoRequest = new DomainMessage(MessageID.USER_EMAIL_ADDRESS_CHANGED);
        infoRequest.set(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

}
