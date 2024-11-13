// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.domain.administration.OneTimeTokenGenerator;
import com.mercedesbenz.sechub.domain.administration.signup.Signup;
import com.mercedesbenz.sechub.domain.administration.signup.SignupRepository;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminAcceptsSignup;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserCreationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserCreationService.class);

    @Autowired
    SecHubEnvironment environment;

    @Autowired
    SignupRepository selfRegistrationRepository;

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OneTimeTokenGenerator oneTimeTokenGenerator;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    AuditLogService auditLog;

    @UseCaseAdminAcceptsSignup(@Step(number = 2, name = "Create user and send events", next = { 3,
            4 }, description = "The service will create the user a one time token for api token generation and triggers asynchronous events.\n"
                    + "It will also remove the existing user signup because no longer necessary."))
    public void createUserFromSelfRegistration(String userId) {

        String sanitizedLogUserId = logSanitizer.sanitize(userId, 30);
        auditLog.log("accepts signup of user {}", sanitizedLogUserId);

        assertion.assertIsValidUserId(userId);

        Optional<Signup> selfRegistration = selfRegistrationRepository.findById(userId);
        if (selfRegistration.isEmpty()) {
            LOG.warn("Did not found a self registration for user with name:{}, so skipped creation", sanitizedLogUserId);
            return;
        }
        Optional<User> found = userRepository.findById(userId);
        if (found.isPresent()) {
            LOG.warn("Self registration coming in for user:{} but user already exists. So just removing self registration entry", sanitizedLogUserId);
            selfRegistrationRepository.deleteById(userId);
            return;
        }

        String emailAddress = selfRegistration.get().getEmailAddress();
        assertion.assertIsValidEmailAddress(emailAddress);

        found = userRepository.findByEmailAddress(emailAddress);

        if (found.isPresent()) {
            LOG.warn("Self registration coming in for user:{} but email address {} already exists. So just removing self registration entry",
                    sanitizedLogUserId, emailAddress);
            selfRegistrationRepository.deleteById(userId);
            return;
        }

        String oneTimeToken = oneTimeTokenGenerator.generateNewOneTimeToken();

        User user = new User();
        user.name = userId;
        user.hashedApiToken = "";// leave it empty, so API auth is disabled - will be filled later after user has
        // clicked to link
        user.emailAddress = emailAddress;
        user.oneTimeToken = oneTimeToken;
        user.oneTimeTokenDate = new Date();

        userRepository.save(user);

        LOG.debug("Persisted new user:{}", sanitizedLogUserId);

        selfRegistrationRepository.deleteById(userId);

        LOG.debug("Removed self registration data of user:{}", sanitizedLogUserId);

        informUserAboutSignupAccepted(user);
        informUserCreated(user);

    }

    @IsSendingAsyncMessage(MessageID.USER_CREATED)
    private void informUserCreated(User user) {
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_CREATED);

        UserMessage message = createInitialUserAuthData(user);

        infoRequest.set(MessageDataKeys.USER_CREATION_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

    @IsSendingAsyncMessage(MessageID.USER_NEW_API_TOKEN_REQUESTED)
    private void informUserAboutSignupAccepted(User user) {
        /* we just send info about new api token */
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_NEW_API_TOKEN_REQUESTED);
        UserMessage userMessage = new UserMessage();
        userMessage.setEmailAddress(user.getEmailAddress());

        /*
         * Security: we do NOT use userid inside this link - if some body got
         * information about the link he/she is not able to use fetched api token
         * because not knowing which userid...
         */
        String linkWithOneTimeToken = environment.getServerBaseUrl() + AdministrationAPIConstants.API_FETCH_NEW_API_TOKEN_BY_ONE_WAY_TOKEN + "/"
                + user.getOneTimeToken();

        userMessage.setLinkWithOneTimeToken(linkWithOneTimeToken);
        userMessage.setSubject("SecHub user account created");
        infoRequest.set(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO, userMessage);

        eventBusService.sendAsynchron(infoRequest);
    }

    private UserMessage createInitialUserAuthData(User user) {
        UserMessage authDataHashed = new UserMessage();

        authDataHashed.setUserId(user.getName());
        authDataHashed.setEmailAddress(user.getEmailAddress());

        return authDataHashed;
    }

}
