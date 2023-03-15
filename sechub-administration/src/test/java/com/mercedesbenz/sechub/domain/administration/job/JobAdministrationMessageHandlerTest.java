// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class JobAdministrationMessageHandlerTest {

    private JobAdministrationMessageHandler handlerToTest;
    private AdministrationConfigService configService;

    @BeforeEach
    void beforeEach() {
        handlerToTest = new JobAdministrationMessageHandler();

        configService = mock(AdministrationConfigService.class);

        handlerToTest.configService = configService;
    }

    @Test
    void handler_receiving_auto_cleanup_calls_config_service_with_message_data() {
        /* prepare */
        long days = System.nanoTime();
        AdministrationConfigMessage configMessage = new AdministrationConfigMessage();
        configMessage.setAutoCleanupInDays(days);
        DomainMessage message = new DomainMessage(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED);

        message.set(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA, configMessage);

        /* execute */
        handlerToTest.receiveAsyncMessage(message);

        /* test */
        verify(configService).updateAutoCleanupInDays(days);
    }

}
