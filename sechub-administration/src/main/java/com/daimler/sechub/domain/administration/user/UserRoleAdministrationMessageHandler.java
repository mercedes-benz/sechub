// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

@Component
public class UserRoleAdministrationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleAdministrationMessageHandler.class);

    @Autowired
    @Lazy
    UserRoleCalculationService userRoleCalculationService;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case REQUEST_USER_ROLE_RECALCULATION:
            handleRoleRecalculationRequest(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.REQUEST_USER_ROLE_RECALCULATION)
    private void handleRoleRecalculationRequest(DomainMessage request) {
        UserMessage message = request.get(MessageDataKeys.USER_ID_DATA);
        String userId = message.getUserId();

        userRoleCalculationService.recalculateRolesOfUser(userId);
    }

}
