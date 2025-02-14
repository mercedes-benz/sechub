// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static java.util.Objects.*;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

@Component
public class ScheduleSecHubConfigurationRuntimeValidation {

    private DomainMessageService eventBus;

    @Lazy
    public ScheduleSecHubConfigurationRuntimeValidation(DomainMessageService eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * This validation asserts that a SecHub configuration is valid at runtime. This
     * is done by sending a synchronous domain event which will return a check
     * result in form of a {@link SecHubMessagesList}. If the list contains error
     * messages, the validation has failed.<br>
     * <br>
     * Attention: This method should only be used at job creation time.
     *
     * @throws ResponseStatusException
     */
    @IsSendingSyncMessage(MessageID.REQUEST_FULL_CONFIGURATION_VALIDATION)
    @IsRecevingSyncMessage(MessageID.REQUEST_FULL_CONFIGURATION_VALIDATION)
    public void assertConfigurationValidAtRuntime(SecHubConfiguration configuration) {
        requireNonNull(configuration, "SecHub configuration may not be null!");

        DomainMessage request = new DomainMessage(MessageID.REQUEST_FULL_CONFIGURATION_VALIDATION);
        request.set(MessageDataKeys.SECHUB_UNENCRYPTED_CONFIG, configuration);

        /* send request */
        DomainMessageSynchronousResult result = eventBus.sendSynchron(request);

        /* inspect result */
        SecHubMessagesList messagesList = result.get(MessageDataKeys.ERROR_MESSAGES);
        if (messagesList == null) {
            throw new IllegalStateException(
                    "For the key " + MessageDataKeys.ERROR_MESSAGES.getId() + " there must be at least an empty list of SecHub messages!");
        }
        List<SecHubMessage> sechubMessages = messagesList.getSecHubMessages();

        SecHubMessage validationErrorMessage = null;
        for (SecHubMessage sechubMessage : sechubMessages) {
            if (SecHubMessageType.ERROR.equals(sechubMessage.getType())) {
                validationErrorMessage = sechubMessage;
                break;/* we just use the first error message - and skip further processing here */
            }
        }
        if (validationErrorMessage == null) {
            /* no error found, means valid */
            return;
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, validationErrorMessage.getText());

    }

}
