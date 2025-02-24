package com.mercedesbenz.sechub.sharedkernel.shutdown;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.context.event.ContextClosedEvent;

class SecHubApplicationContextCloseHandlerTest {

    @Test
    void onApplicationContextClosedEvent_the_shutdownhandler_method_is_called() {
        /* prepare */
        SpringApplicationShutdownHandler shutdownHandler = mock();
        SecHubApplicationContextCloseHandler handlerToTest = new SecHubApplicationContextCloseHandler(shutdownHandler);
        ContextClosedEvent event = mock();

        /* execute */
        handlerToTest.onApplicationEvent(event);

        /* test */
        verify(shutdownHandler).handleShutdown();

    }

}
