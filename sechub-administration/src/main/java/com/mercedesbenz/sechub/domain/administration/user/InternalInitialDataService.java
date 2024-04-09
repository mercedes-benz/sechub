// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

/**
 * This is an internal service only. It's used on startup to create an
 * administrator with well known api token etc. This admin shall be used only at
 * startup and deleted as soon as there is another (real) administrator applied
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class InternalInitialDataService {

    private static final Logger LOG = LoggerFactory.getLogger(InternalInitialDataService.class);

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserInputAssertion assertion;

    /**
     * Creates an initial administrator - when not used in integration tests but on
     * normal environment this administrator should be deleted when another user has
     * gained administrator rights
     *
     * @param userId
     * @param emailAddress
     * @param fixApiToken  - use "{nooop}" as prefix to prevent token encryption
     */
    public void createInitialAdmin(String userId, String emailAddress, String fixApiToken) {
        internalCreateInitialUser(userId, emailAddress, fixApiToken, true);
    }

    /**
     * Creates an initial test user
     *
     * @param userId
     * @param emailAddress
     * @param unencryptedAPItoken - use "{nooop}" as prefix to prevent token
     *                            encryption
     */
    public void createInitialTestUser(String userId, String emailAddress, String unencryptedAPItoken) {
        internalCreateInitialUser(userId, emailAddress, unencryptedAPItoken, false);
    }

    private void internalCreateInitialUser(String userId, String emailAddress, String unencryptedAPItoken, boolean createAsSuperAdmin) {

        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidEmailAddress(emailAddress);

        User exampleUser = new User();
        exampleUser.superAdmin = true;

        if (createAsSuperAdmin && userRepository.exists(Example.of(exampleUser))) {
            LOG.info("At least one admin exists already, so skip initial admin creation of {} ", userId);
            return;
        }
        if (createAsSuperAdmin) {
            logInitialAdminCredentials(userId, unencryptedAPItoken, emailAddress);
        }

        Optional<User> found = userRepository.findById(userId);
        if (found.isPresent()) {
            LOG.error("Initial admin {} already exists ?!?!", userId);
            return;
        }
        /* create */
        User user = createUser(userId, emailAddress, unencryptedAPItoken, createAsSuperAdmin);
        /* send events - will create auth layer etc. */
        informUserCreated(user);
        /*
         * We must this send additional to inform authorization domain to update the
         * token... the user must exist before, so we wait some time to ensure we have
         * user being created in admin layer - this avoids race conditions.
         *
         * Not very smart... but it works and its only done one time at first start...
         * so acceptable.
         */
        try {
            Thread.sleep(500); //
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        informUserAPITokenChanged(user);

    }

    private User createUser(String userId, String emailAddress, String fixApiToken, boolean superAdmin) {
        /* create a super admin */
        User user = new User();
        user.name = userId;
        if (fixApiToken.startsWith("{noop}")) {
            /*
             * special case for noop variants, where no password encrypt is wanted - e.g. on
             * integration tests bcrypt passwords do slow done automated tests
             */
            user.hashedApiToken = fixApiToken;
        } else {
            user.hashedApiToken = encryptPassword(fixApiToken);
        }
        user.emailAddress = emailAddress;
        user.superAdmin = superAdmin;

        userRepository.save(user);

        LOG.debug("Persisted new initial admin:{}", userId);
        return user;
    }

    private void logInitialAdminCredentials(String initialAdminUserId, String unencryptedApiToken, String emailAddress) {
        StringBuilder sb = new StringBuilder();
        sb.append("Initial SecHub Admin created !");
        sb.append("\n##################### ATTENTION #####################################################");
        sb.append("\n# Created initial admin:'{}' with password:'{}'");
        sb.append("\n#                 email:'{}'");
        sb.append("\n# In production: CHANGE this initial API token, when your server has been started.");
        sb.append("\n# Everybody able to read this log file has access to these admin credentials!");
        sb.append("\n#####################################################################################");

        LOG.warn(sb.toString(), initialAdminUserId, unencryptedApiToken, emailAddress);
        /*
         * TODO de-jcup, 2019-05-29: think about forgetting this log as well - user has
         * an email and can reset his/her account
         */
    }

    @IsSendingAsyncMessage(MessageID.USER_CREATED)
    private void informUserCreated(User user) {
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_CREATED);

        UserMessage message = createInitialUserAuthData(user);

        infoRequest.set(MessageDataKeys.USER_CREATION_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

    @IsSendingAsyncMessage(MessageID.USER_API_TOKEN_CHANGED)
    private void informUserAPITokenChanged(User user) {
        DomainMessage infoRequest = new DomainMessage(MessageID.USER_API_TOKEN_CHANGED);

        UserMessage message = createInitialUserAuthData(user);
        message.setHashedApiToken(user.getHashedApiToken());

        infoRequest.set(MessageDataKeys.USER_API_TOKEN_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }

    private UserMessage createInitialUserAuthData(User user) {
        UserMessage authDataHashed = new UserMessage();

        authDataHashed.setUserId(user.getName());
        authDataHashed.setEmailAddress(user.getEmailAddress());

        return authDataHashed;
    }

    private String encryptPassword(String initialPassword) {
        /* encrypt */
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return encoder.encode(initialPassword);
    }

}
