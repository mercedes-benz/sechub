// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.shutdown;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationFailedEvent;

class SecHubApplicationFailedHandlerTest {

    @Test
    void onApplicationFailedEvent_the_shutdownhandler_method_is_called() {
        /* prepare */
        SpringApplicationShutdownHandler shutdownHandler = mock();
        SecHubApplicationFailedHandler handlerToTest = new SecHubApplicationFailedHandler(shutdownHandler);
        ApplicationFailedEvent event = mock();

        /* execute */
        handlerToTest.onApplicationEvent(event);

        /* test */
        verify(shutdownHandler).handleShutdown();

    }

}
