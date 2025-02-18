// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAnonymousUserVerifiesEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserUpdatesEmailAddress;
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
    private final UserEmailChangeTokenService userEmailChangeTokenService;
    private final SecHubEnvironment environment;

    /* @formatter:off */
    public UserEmailAddressUpdateService(DomainMessageService eventBusService,
                                         UserRepository userRepository,
                                         AuditLogService auditLogService,
                                         LogSanitizer logSanitizer,
                                         UserInputAssertion assertion,
                                         UserContextService userContextService,
                                         UserEmailChangeTokenService userEmailChangeTokenService,
                                         SecHubEnvironment environment) {
        /* @formatter:on */
        this.eventBusService = eventBusService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.logSanitizer = logSanitizer;
        this.assertion = assertion;
        this.userContextService = userContextService;
        this.userEmailChangeTokenService = userEmailChangeTokenService;
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
        validateNewEmail(newEmailAddress);

        User user = userRepository.findOrFailUser(userId);
        String formerEmailAddress = user.getEmailAddress();

        if (newEmailAddress.equals(formerEmailAddress)) {
            throw new BadRequestException("New email address is same as former email address!");
        }
        validateEmailUnique(newEmailAddress);

        /* parameters valid, we audit log the change */
        auditLogService.log("Changed email address of user {}", logSanitizer.sanitize(userId, 30));

        user.emailAddress = newEmailAddress;

        /* create message containing data before user email has changed */
        UserMessage message = createUserMessage(user, formerEmailAddress);
        message.setSubject("A SecHub administrator has changed your email address");

        userRepository.save(user);

        informUserEmailAddressUpdated(message);

    }

    /* @formatter:off */
    @UseCaseUserUpdatesEmailAddress(
            @Step(
                    number = 2,
                    name = "Service checks user mail address and sends approval mail",
                    next = {3},
                    description = "The service will check the user input and send a mail to verify"))
    public void userRequestUpdateMailAddress(String email) {
        /* @formatter:on */
        validateNewEmail(email);

        String userId = userContextService.getUserId();
        User user = userRepository.findOrFailUser(userId);

        String formerEmailAddress = user.getEmailAddress();
        if (email.equals(formerEmailAddress)) {
            throw new BadRequestException("New email address is same as former email address!");
        }
        validateEmailUnique(email);

        String baseUrl = environment.getServerBaseUrl();
        UserEmailChangeRequest userEmailChangeRequest = new UserEmailChangeRequest(userId, email);
        String mailToken = userEmailChangeTokenService.generateToken(userEmailChangeRequest);

        String linkWithOneTimeToken = baseUrl + AdministrationAPIConstants.API_ANONYMOUS_USER_VERIFY_EMAIL + "/" + mailToken;

        UserMessage message = createUserMessage(user, formerEmailAddress);
        // send mail to new email address
        message.setEmailAddress(email);
        message.setLinkWithOneTimeToken(linkWithOneTimeToken);
        message.setSubject("You have requested to change your SecHub email address");

        informUserWantsToChangeEmailAddress(message);
    }

    /* @formatter:off */
    @UseCaseAnonymousUserVerifiesEmailAddress(
            @Step(
                    number = 2,
                    name = "Service verifies user mail address",
                    next = {3},
                    description = "The service will verify the token and update the user email address"))
    public void userVerifiesUserEmailAddress(String token) {
        /* @formatter:on */
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token must not be null or blank!");
        }
        UserEmailChangeRequest userEmailChangeRequest = userEmailChangeTokenService.extractUserInfoFromToken(token);

        String userIdFromToken = userEmailChangeRequest.userId();
        User user = userRepository.findOrFailUser(userIdFromToken);
        String emailFromToken = userEmailChangeRequest.newEmail();
        String formerEmailAddress = user.getEmailAddress();

        validateNewEmail(emailFromToken);
        if (emailFromToken.equals(formerEmailAddress)) {
            throw new BadRequestException("Token has already been used!");
        }
        validateEmailUnique(emailFromToken);

        user.emailAddress = emailFromToken;

        UserMessage message = createUserMessage(user, formerEmailAddress);
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

    private void validateNewEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email must not be empty");
        }
        assertion.assertIsValidEmailAddress(email);
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmailAddress(email)) {
            throw new BadRequestException("The email address is already in use. Please chose another one.");
        }
    }

    private static UserMessage createUserMessage(User user, String formerEmailAddress) {
        UserMessage message = new UserMessage();
        message.setUserId(user.getName());
        message.setEmailAddress(user.getEmailAddress());
        message.setFormerEmailAddress(formerEmailAddress);
        return message;
    }

}
