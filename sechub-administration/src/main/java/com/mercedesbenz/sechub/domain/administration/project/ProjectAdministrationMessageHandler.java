// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;

@Component
public class ProjectAdministrationMessageHandler implements AsynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectAdministrationMessageHandler.class);

    @Autowired
    @Lazy
    ProjectAssignUserService assignUserService;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case ASSIGN_OWNER_AS_USER_TO_PROJECT:
            handleAssignOwnerAsUserToProject(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.ASSIGN_OWNER_AS_USER_TO_PROJECT)
    private void handleAssignOwnerAsUserToProject(DomainMessage request) {
        UserMessage message = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
        String userId = message.getUserId();
        String projectId = message.getProjectId();

        assignUserService.assignUserToProjectAsSystem(userId, projectId, false); // we do not fail if owner is already assigned to project!
    }

}
