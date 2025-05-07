// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetailsForEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserFetchesUserDetailInformation;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
public class UserDetailInformationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDetailInformationService.class);

    private final UserContextService userContext;
    private final UserRepository userRepository;
    private final LogSanitizer logSanitizer;
    private final UserInputAssertion assertion;

    private ProjectRepository projectRepository;

    public UserDetailInformationService(UserContextService userContext, UserRepository userRepository, ProjectRepository projectRepository,
            LogSanitizer logSanitizer, UserInputAssertion assertion) {
        this.userContext = userContext;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;

        this.logSanitizer = logSanitizer;
        this.assertion = assertion;
    }

    /* @formatter:off */
	@UseCaseUserFetchesUserDetailInformation(
			@Step(
				number = 2,
				name = "Service fetches user details for the authenticated user.",
				description = "The service will fetch user details for the authenticated user"))
	/* @formatter:on */
    @RolesAllowed(RoleConstants.ROLE_USER)
    public UserDetailInformation fetchDetails() {
        String userId = userContext.getUserId();

        LOG.debug("User {} is fetching his user details", userId);

        User user = userRepository.findOrFailUser(userId);

        Set<String> assignedProjectIds = projectRepository.findAllProjectIdsWhereUserIsAssigned(userId);
        Set<String> ownedProjectIds = projectRepository.findAllProjectIdsWhereUserIsOwner(userId);

        return new UserDetailInformation(user, assignedProjectIds, ownedProjectIds);
    }

    /* @formatter:off */
	@UseCaseAdminShowsUserDetails(
			@Step(
				number = 2,
				name = "Service fetches user details.",
				description = "The service will fetch user details for given user id"))
	/* @formatter:on */
    @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
    public UserDetailInformation fetchDetailsById(String userId) {
        LOG.debug("User {} is fetching user details for user: {}", userContext.getUserId(), logSanitizer.sanitize(userId, 30));

        assertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        Set<String> assignedProjects = projectRepository.findAllProjectIdsWhereUserIsAssigned(user.getName());
        Set<String> ownedProjects = projectRepository.findAllProjectIdsWhereUserIsOwner(user.getName());

        return new UserDetailInformation(user, assignedProjects, ownedProjects);
    }

    /* @formatter:off */
    @UseCaseAdminShowsUserDetailsForEmailAddress(
            @Step(
                number = 2,
                name = "Service fetches user details.",
                description = "The service will fetch user details for given user email address"))
    /* @formatter:on */
    @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
    public UserDetailInformation fetchDetailsByEmailAddress(String emailAddress) {
        LOG.debug("User {} is fetching user details for user email: {}", userContext.getUserId(), logSanitizer.sanitize(emailAddress, 30));

        assertion.assertIsValidEmailAddress(emailAddress);

        User user = userRepository.findOrFailUserByEmailAddress(emailAddress);

        Set<String> assignedProjects = projectRepository.findAllProjectIdsWhereUserIsAssigned(user.getName());
        Set<String> ownedProjects = projectRepository.findAllProjectIdsWhereUserIsOwner(user.getName());

        return new UserDetailInformation(user, assignedProjects, ownedProjects);
    }
}
