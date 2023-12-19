// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetailsForEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class UserDetailInformationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDetailInformationService.class);

    @Autowired
    UserContextService userContext;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@UseCaseAdminShowsUserDetails(
			@Step(
				number = 2,
				name = "Service fetches user details.",
				description = "The service will fetch user details for given user id"))
	/* @formatter:on */
    public UserDetailInformation fetchDetails(String userId) {
        LOG.debug("User {} is fetching user details for user: {}", userContext.getUserId(), logSanitizer.sanitize(userId, 30));

        assertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        return new UserDetailInformation(user);
    }

    /* @formatter:off */
    @UseCaseAdminShowsUserDetailsForEmailAddress(
            @Step(
                number = 2,
                name = "Service fetches user details.",
                description = "The service will fetch user details for given user email address"))
    /* @formatter:on */
    public UserDetailInformation fetchDetailsByEmailAddress(String emailAddress) {
        LOG.debug("User {} is fetching user details for user email: {}", userContext.getUserId(), logSanitizer.sanitize(emailAddress, 30));

        assertion.assertIsValidEmailAddress(emailAddress);

        User user = userRepository.findOrFailUserByEmailAddress(emailAddress);

        return new UserDetailInformation(user);
    }
}
