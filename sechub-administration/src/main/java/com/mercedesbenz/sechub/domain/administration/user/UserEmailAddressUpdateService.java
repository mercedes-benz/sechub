// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.InternalServerErrorException;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUpdatesUserEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserUpdatesEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserVerifiesEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
public class UserEmailAddressUpdateService {

    private final DomainMessageService eventBusService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final LogSanitizer logSanitizer;
    private final UserInputAssertion assertion;
    private final UserContextService userContextService;
    private final UserEmailChangeTokenGenerator userEmailChangeTokenGenerator;
    private final SecHubEnvironment environment;

    /* @formatter:off */
    public UserEmailAddressUpdateService(DomainMessageService eventBusService,
                                         UserRepository userRepository,
                                         AuditLogService auditLogService,
                                         LogSanitizer logSanitizer,
                                         UserInputAssertion assertion,
                                         UserContextService userContextService,
                                         UserEmailChangeTokenGenerator userEmailChangeTokenGenerator,
                                         SecHubEnvironment environment) {
        /* @formatter:on */
        this.eventBusService = eventBusService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.logSanitizer = logSanitizer;
        this.assertion = assertion;
        this.userContextService = userContextService;
        this.userEmailChangeTokenGenerator = userEmailChangeTokenGenerator;
        this.environment = environment;
    }

    /* @formatter:off */
	@Validated
    @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
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
        UserMessage message = getUserMessage(user, formerEmailAddress);

        userRepository.save(user);

        informUserEmailAddressUpdated(message);

    }

    @UseCaseUserUpdatesEmailAddress(@Step(number = 2, name = "Service checks user mail address and sends approval mail", next = {
            3 }, description = "The service will check the user input and send a mail to verify"))
    public void userUpdatesEmailAddress(String email) {
        assertion.assertIsValidEmailAddress(email);
        String userId = userContextService.getUserId();
        User user = userRepository.findOrFailUser(userId);

        String formerEmailAddress = user.getEmailAddress();
        if (formerEmailAddress == null || formerEmailAddress.isBlank()) {
            throw new InternalServerErrorException("User has no email address");
        }

        if (email.equalsIgnoreCase(formerEmailAddress)) {
            throw new NotAcceptableException("User has already this email address");
        }

        String mailToken = userEmailChangeTokenGenerator.generateToken(email);
        String linkWithOneTimeToken = environment.getServerBaseUrl() + AdministrationAPIConstants.API_USER_VERIFY_EMAIL_BUILD + "/" + mailToken;

        UserMessage message = new UserMessage();
        message.setUserId(userId);
        message.setEmailAddress(email);
        message.setFormerEmailAddress(formerEmailAddress);
        message.setLinkWithOneTimeToken(linkWithOneTimeToken);
        message.setSubject("You have requested to change your SecHub email address");

        informUserWantsToChangeEmailAddress(message);
    }

    @UseCaseUserVerifiesEmailAddress(@Step(number = 2, name = "Service verifies user mail address", next = {
            3 }, description = "The service will verify the token and update the user email address"))
    public void verifyUserEmailAddress(String token) {

        String emailFromToken = userEmailChangeTokenGenerator.getEmailFromToken(token);

        String userId = userContextService.getUserId();
        User user = userRepository.findOrFailUser(userId);

        if (user.getEmailAddress().equals(emailFromToken)) {
            throw new NotAcceptableException("User has already this email address");
        }

        String formerEmailAddress = user.getEmailAddress();

        user.emailAddress = emailFromToken;

        UserMessage message = getUserMessage(user, formerEmailAddress);
        message.setUserId(userId);
        message.setEmailAddress(emailFromToken);
        message.setFormerEmailAddress(formerEmailAddress);
        message.setSubject("Your SecHub email address has been changed");

        userRepository.save(user);
        informUserEmailAddressUpdated(message);
    }

    @IsSendingAsyncMessage(MessageID.USER_EMAIL_ADDRESS_CHANGED)
    private void informUserEmailAddressUpdated(UserMessage message) {

        DomainMessage infoRequest = new DomainMessage(MessageID.USER_EMAIL_ADDRESS_CHANGED);
        infoRequest.set(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

    @IsSendingAsyncMessage(MessageID.USER_EMAIL_ADDRESS_CHANGE_REQUEST)
    private void informUserWantsToChangeEmailAddress(UserMessage message) {
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_EMAIL_ADDRESS_CHANGE_REQUEST);
        infoRequest.set(MessageDataKeys.USER_EMAIL_ADDRESS_CHANGE_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

    private static UserMessage getUserMessage(User user, String formerEmailAddress) {
        UserMessage message = new UserMessage();
        message.setUserId(user.getName());
        message.setEmailAddress(user.getEmailAddress());
        message.setFormerEmailAddress(formerEmailAddress);
        message.setSubject("A SecHub administrator has changed your email address");
        return message;
    }

}
