// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.APITokenGenerator;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogType;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AnonymousUserGetAPITokenByOneTimeTokenService {

    static final String ANSWER_WHEN_TOKEN_CANNOT_BE_CHANGED = "Your API token change request has either timed out or never existed. Please request another API token change.";

    /**
     * Default timeout is one day
     */
    static final long DEFAULT_OUTDATED_TIME_MILLIS = 86400000;// 1d * 24h * 60m * 60s * 1000ms = one day = 86400000

    private static final Logger LOG = LoggerFactory.getLogger(AnonymousUserGetAPITokenByOneTimeTokenService.class);

    @Value("${sechub.user.onetimetoken.outdated.millis:86400000}")
    @MustBeDocumented(value = "One time token time when outdating")
    long oneTimeOutDatedMillis = DEFAULT_OUTDATED_TIME_MILLIS;

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository sechubUserRepository;

    @Autowired
    APITokenGenerator apiTokenGenerator;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    SecurityLogService securityLogService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @UseCaseUserClicksLinkToGetNewAPIToken(@Step(number = 2, next = { 3,
            4 }, name = "Validation and update", description = "When its a valid one time token a new api token is generated and persisted hashed to user. The token itself is returned. When not valid an emtpy string is the result ..."))
    public String createNewAPITokenForUserByOneTimeToken(String oneTimeToken) {
        assertion.assertIsValidOneTimeToken(oneTimeToken);

        Optional<User> found = sechubUserRepository.findByOneTimeToken(oneTimeToken);
        if (!found.isPresent()) {
            securityLogService.log(SecurityLogType.POTENTIAL_INTRUSION,
                    "Did not found a user having one time token :{}. Maybe an attack, so will just return info string.",
                    logSanitizer.sanitize(oneTimeToken, 50));
            return ANSWER_WHEN_TOKEN_CANNOT_BE_CHANGED;
        }

        User user = found.get();
        /* check not outdated onetime token */
        if (user.isOneTimeTokenOutDated(oneTimeOutDatedMillis)) {
            securityLogService.log(SecurityLogType.POTENTIAL_USERDATA_LEAK,
                    "Did found a user having one time token :{}, but token is outdated! Maybe an attack (or user just waited too long...). Will just return info string.",
                    logSanitizer.sanitize(oneTimeToken, 50));
            return ANSWER_WHEN_TOKEN_CANNOT_BE_CHANGED;
        }

        user.oneTimeToken = null;
        user.oneTimeTokenDate = null;
        String rawToken = apiTokenGenerator.generateNewAPIToken();
        user.hashedApiToken = passwordEncoder.encode(rawToken);

        sechubUserRepository.save(user);

        LOG.info("Updated API token for user {}", user.getName());

        sendUserAPITokenChanged(user);

        /* we return the raw token to user - but do NOT save it but hashed variant */
        return rawToken;
    }

    @IsSendingAsyncMessage(MessageID.USER_API_TOKEN_CHANGED)
    private void sendUserAPITokenChanged(User user) {
        DomainMessage request = new DomainMessage(MessageID.USER_API_TOKEN_CHANGED);
        UserMessage message = new UserMessage();
        message.setEmailAddress(user.getEmailAddress());
        message.setUserId(user.getName());
        message.setHashedApiToken(user.getHashedApiToken());

        request.set(MessageDataKeys.USER_API_TOKEN_DATA, message);
        eventBusService.sendAsynchron(request);
    }

}
