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
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AnonymousUserRequestsNewApiTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(AnonymousUserRequestsNewApiTokenService.class);

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SecHubEnvironment environment;

    @Autowired
    OneTimeTokenGenerator oneTimeTokenGenerator;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    public void anonymousRequestToGetNewApiTokenForUserEmailAddress(String userEmail) {
        LOG.info("New api token requested for email address: {})", logSanitizer.sanitize(userEmail, 50));

        assertion.assertIsValidEmailAddress(userEmail);

        Optional<User> found = userRepository.findByEmailAddress(userEmail);
        if (!found.isPresent()) {
            /* we just do nothing here - prevent user enumeration by hacking... */
            LOG.warn("Anonymous request to get new api token, but user unknown: {})", logSanitizer.sanitize(userEmail, 50));
            return;
        }

        User user = saveUserWithNewOneTimeToken(found);

        sendUserNewApiTokenRequested(userEmail, user);
    }

    @IsSendingAsyncMessage(MessageID.USER_NEW_API_TOKEN_REQUESTED)
    private void sendUserNewApiTokenRequested(String userEmail, User user) {
        /* we just send info about new api token */
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_NEW_API_TOKEN_REQUESTED);
        UserMessage userMessage = new UserMessage();
        userMessage.setEmailAddress(userEmail);

        /*
         * Security: we do NOT use userid inside this link - if some body got
         * information about the link he/she is not able to use fetched api token
         * because not knowing which userid...
         */
        String linkWithOneTimeToken = environment.getServerBaseUrl() + AdministrationAPIConstants.API_FETCH_NEW_API_TOKEN_BY_ONE_WAY_TOKEN + "/"
                + user.getOneTimeToken();

        userMessage.setLinkWithOneTimeToken(linkWithOneTimeToken);
        userMessage.setSubject("Your request for a new SecHub API token");
        infoRequest.set(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO, userMessage);

        eventBusService.sendAsynchron(infoRequest);
    }

    private User saveUserWithNewOneTimeToken(Optional<User> found) {
        String oneTimeToken = oneTimeTokenGenerator.generateNewOneTimeToken();

        User user = found.get();
        user.oneTimeToken = oneTimeToken;
        user.oneTimeTokenDate = new Date();

        userRepository.save(user);

        LOG.debug("Updated one time token for user:{}", user.getName());
        return user;
    }

}
