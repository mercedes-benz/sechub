// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class UserRoleCalculationService {

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserInputAssertion assertion;

    public void recalculateRolesOfUser(String userId) {
        assertion.assertIsValidUserId(userId);

        User user = userRepository.findOrFailUser(userId);

        boolean active = !user.isDeactivated();

        boolean isUser = active;
        boolean isOwner = active && userOwnsAtLeastOneProject(userId);
        boolean isAdmin = active && user.isSuperAdmin();

        Set<String> roles = new LinkedHashSet<>();
        if (isAdmin) {
            roles.add(RoleConstants.ROLE_SUPERADMIN);
        }
        if (isOwner) {
            roles.add(RoleConstants.ROLE_OWNER);
        }
        if (isUser) {
            roles.add(RoleConstants.ROLE_USER);
        }
        sendUserRoleChangedEvent(user, roles);
    }

    private boolean userOwnsAtLeastOneProject(String userId) {
        // we do NOT use the user entity and the ownedProjects set
        // instead we simply count. Reason for this is to avoid
        // unexpected caching issues with ORM.
        return userRepository.countAmountOfOwnedProjects(userId) > 0;
    }

    @IsSendingAsyncMessage(MessageID.USER_ROLES_CHANGED)
    private void sendUserRoleChangedEvent(User user, Set<String> roles) {
        DomainMessage roleChangeRequest = new DomainMessage(MessageID.USER_ROLES_CHANGED);

        UserMessage rolesData = new UserMessage();
        rolesData.setUserId(user.getName());
        rolesData.setRoles(roles);
        roleChangeRequest.set(MessageDataKeys.USER_ROLES_DATA, rolesData);

        eventBus.sendAsynchron(roleChangeRequest);
    }
}
