package com.mercedesbenz.sechub.sharedkernel.shutdown;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class SecHubApplicationContextCloseHandler implements ApplicationListener<ContextClosedEvent> {

    private SpringApplicationShutdownHandler shutdownHandler;

    public SecHubApplicationContextCloseHandler(SpringApplicationShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        shutdownHandler.handleShutdown();
    }

}
