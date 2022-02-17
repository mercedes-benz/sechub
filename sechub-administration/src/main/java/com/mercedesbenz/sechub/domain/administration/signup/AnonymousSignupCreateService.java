// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserSignup;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AnonymousSignupCreateService {

    private static final Logger LOG = LoggerFactory.getLogger(AnonymousSignupCreateService.class);

    @Autowired
    SignupRepository userSelfRegistrationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserInputAssertion assertion;

    /**
     * Tries to register a new user. If signup for user or a user with same name or
     * email already exists, nothing happens. No error or something. Only when
     * registration data is invalid an error will happen. Frontends shall always
     * give only a hint like "You will receive an email with registration details
     * when your request is accepted"
     *
     * @param userSelfRegistrationInput
     */
    @Validated
    @UseCaseUserSignup(@Step(number = 2, name = "Persistence", description = "Valid self registration input will be persisted to database."))
    public void register(@Valid SignupJsonInput userSelfRegistrationInput) {
        String userId = userSelfRegistrationInput.getUserId();
        String emailAdress = userSelfRegistrationInput.getEmailAdress();

        LOG.debug("user tries to register himself:{},mail:{}", userId, emailAdress);

        assertion.isValidUserId(userId);
        assertion.isValidEmailAddress(emailAdress);

        assertNotAlreadySignedIn(userId, emailAdress);
        assertUsernameNotUsedAlready(userId, emailAdress);
        assertEmailAdressNotUsedAlready(userId, emailAdress);

        Signup entity = new Signup();

        entity.setEmailAdress(emailAdress);
        entity.setUserId(userId);
        userSelfRegistrationRepository.save(entity);
        LOG.debug("Added registration entry for user:{},mail:{}", entity.getUserId(), entity.getEmailAdress());

        /* trigger event */
        informAboutSignupRequest(entity);
    }

    @IsSendingAsyncMessage(MessageID.USER_SIGNUP_REQUESTED)
    private void informAboutSignupRequest(Signup signup) {
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_SIGNUP_REQUESTED);

        UserMessage userMessage = new UserMessage();
        userMessage.setEmailAdress(signup.getEmailAdress());
        userMessage.setUserId(signup.getUserId());

        infoRequest.set(MessageDataKeys.USER_SIGNUP_DATA, userMessage);

        eventBusService.sendAsynchron(infoRequest);
    }

    private void assertEmailAdressNotUsedAlready(String userId, String emailAdress) {
        Optional<User> foundUserByMail = userRepository.findByEmailAdress(emailAdress);

        if (foundUserByMail.isPresent()) {
            LOG.warn("Self registration coming in for emailadress:{} and user:{} but an existing user does already have this email adress. So not accepted",
                    emailAdress, userId);
            handleRegistrationNotPossible();
        }
    }

    private void assertUsernameNotUsedAlready(String userId, String emailAdress) {
        Optional<User> foundUser = userRepository.findById(userId);

        if (foundUser.isPresent()) {
            LOG.warn("Self registration coming in for emailadress:{} and user:{} but existing user found by name. So not accepted", emailAdress, userId);
            handleRegistrationNotPossible();
        }
    }

    private void assertNotAlreadySignedIn(String userId, String emailAdress) {
        Optional<Signup> found = userSelfRegistrationRepository.findById(userId);
        if (found.isPresent()) {
            LOG.warn("Self registration coming in for emailadress:{} and user:{} but signup already exists. So not accepted", emailAdress, userId);
            handleRegistrationNotPossible();
        }
    }

    private void handleRegistrationNotPossible() {
        throw new NotAcceptableException("registration not possible");
    }

}
