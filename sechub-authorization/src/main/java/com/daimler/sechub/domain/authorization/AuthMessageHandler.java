// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.authorization.service.AuthUpdateUserApiTokenService;
import com.daimler.sechub.domain.authorization.service.AuthUserCreationService;
import com.daimler.sechub.domain.authorization.service.AuthUserDeleteService;
import com.daimler.sechub.domain.authorization.service.AuthUserUpdateRolesService;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

/**
 * Auth service does handle authorization and authentication messages
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class AuthMessageHandler implements AsynchronMessageHandler {

	private static final Logger LOG = LoggerFactory.getLogger(AuthMessageHandler.class);


	@Autowired
	AuthUserCreationService userAuthCreationService;

	@Autowired
	AuthUserUpdateRolesService userAuthUpdateRolesService;

	@Autowired
	AuthUpdateUserApiTokenService userAuthTokenUpdateService;

	@Autowired
	AuthUserDeleteService userAuthDeleteService;

	@Override
	public void receiveAsyncMessage(DomainMessage request) {
		MessageID messageId = request.getMessageId();

		LOG.debug("received domain request: {}", request);


		switch (messageId) {
		case USER_CREATED:
			handleUserCreation(request);
			break;
		case USER_API_TOKEN_CHANGED:
			handleUserApiTokenChanged(request);
			break;
		case USER_ROLES_CHANGED:
			handleUserRolesChanged(request);
			break;
		case USER_DELETED:
			handleUserDeleted(request);
			break;
		default:
			throw new IllegalStateException("unhandled message id:"+messageId);
		}
	}

	@IsReceivingAsyncMessage(MessageID.USER_ROLES_CHANGED)
	private void handleUserRolesChanged(DomainMessage request) {
		UserMessage userMessage = request.get(MessageDataKeys.USER_ROLES_DATA);
		userAuthUpdateRolesService.updateRoles(userMessage.getUserId(), userMessage.getRoles());

	}

	@IsReceivingAsyncMessage(MessageID.USER_API_TOKEN_CHANGED)
	private void handleUserApiTokenChanged(DomainMessage request) {
		UserMessage userMessage = request.get(MessageDataKeys.USER_API_TOKEN_DATA);
		userAuthTokenUpdateService.updateAPIToken(userMessage.getUserId(), userMessage.getHashedApiToken());
	}

	@IsReceivingAsyncMessage(MessageID.USER_CREATED)
	private void handleUserCreation(DomainMessage request) {
		UserMessage userMessage = request.get(MessageDataKeys.USER_CREATION_DATA);
		userAuthCreationService.createUser(userMessage.getUserId(), userMessage.getHashedApiToken());
	}

	@IsReceivingAsyncMessage(MessageID.USER_DELETED)
	private void handleUserDeleted(DomainMessage request) {
		UserMessage userMessage = request.get(MessageDataKeys.USER_DELETE_DATA);
		userAuthDeleteService.deleteUser(userMessage.getUserId());
	}

}
