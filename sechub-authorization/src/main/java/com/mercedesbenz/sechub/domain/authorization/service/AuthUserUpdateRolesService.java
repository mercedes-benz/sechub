// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization.service;

import static com.mercedesbenz.sechub.sharedkernel.security.RoleConstants.isOwnerRole;
import static com.mercedesbenz.sechub.sharedkernel.security.RoleConstants.isSuperAdminRole;
import static com.mercedesbenz.sechub.sharedkernel.security.RoleConstants.isUserRole;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.authorization.AuthUser;
import com.mercedesbenz.sechub.domain.authorization.AuthUserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminAssignsUserToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUnassignsUserFromProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class AuthUserUpdateRolesService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthUserUpdateRolesService.class);

    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    AuthUserRepository authUserRepository;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@UseCaseAdminAssignsUserToProject(@Step(number=4,next={Step.NO_NEXT_STEP} ,name="Roles changed in auth", description="Authorization layer adds ROLE_USER"))
	@UseCaseAdminUnassignsUserFromProject(@Step(number=4,next={Step.NO_NEXT_STEP} ,name="Roles changed in auth", description="Authorization layer removes ROLE_USER"))/* @formatter:on */
    public void updateRoles(String userId, Set<String> roles) {
        assertion.assertIsValidUserId(userId);

        internalUpdateRoles(userId, roles, 0);

    }

    /**
     * Why this retry mechanism? The update role event can happen multiple times -
     * so an optimistic lock may happen. But last event shall override.
     *
     * @param userId
     * @param roles
     * @param retryCount
     */
    private void internalUpdateRoles(String userId, Set<String> roles, int retryCount) {
        if (retryCount > MAX_RETRY_COUNT) {
            LOG.error("Maximum retry count exceeded ({}), cannot update user {} with roles {}", MAX_RETRY_COUNT, userId, roles);
            return;
        }
        AuthUser user = authUserRepository.findOrFail(userId);
        LOG.debug("Current auth roles of user '{}'. Roles: superadmin={}, user={}, owner={}", userId, user.isRoleSuperAdmin(), user.isRoleUser(),
                user.isRoleOwner());

        /* reset all flags */
        user.setRoleSuperAdmin(false);
        user.setRoleOwner(false);
        user.setRoleUser(false);

        /* set only flags where role is defined */
        for (String role : roles) {
            if (isSuperAdminRole(role)) {
                user.setRoleSuperAdmin(true);
            } else if (isUserRole(role)) {
                user.setRoleUser(true);
            } else if (isOwnerRole(role)) {
                user.setRoleOwner(true);
            }
        }
        LOG.info("Updated auth roles of user '{}'. Roles: superadmin={}, user={}, owner={}", userId, user.isRoleSuperAdmin(), user.isRoleUser(),
                user.isRoleOwner());
        try {
            authUserRepository.save(user);
        } catch (OptimisticLockingFailureException e) {
            /*
             * when this happens we do not really care! reason: last event is the event that
             * matters!
             */
            LOG.warn("Optimistic lock failure, ai user has already been changed. Because last event overrides all we do a retry");
            internalUpdateRoles(userId, roles, retryCount++);
        }
    }

}
