// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

@Service
public class UserRoleCalculationService {

	@Autowired
	DomainMessageService eventBus;

	@Autowired
	UserRepository userRepository;

	public void recalculateRolesOfUser(String userId) {
		User user = userRepository.findOrFailUser(userId);

		boolean active = ! user.isDeactivated();

		boolean isUser =  active;
		boolean isOwner = active && !user.getOwnedProjects().isEmpty();
		boolean isAdmin = active && user.isSuperAdmin();

		Set<String> roles = new LinkedHashSet<>();
		if (isAdmin){
			roles.add(RoleConstants.ROLE_SUPERADMIN);
		}
		if (isOwner){
			roles.add(RoleConstants.ROLE_OWNER);
		}
		if (isUser){
			roles.add(RoleConstants.ROLE_USER);
		}
		sendUserRoleChangedEvent(user, roles);
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
