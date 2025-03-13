// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.shutdown;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SecHubApplicationFailedHandler implements ApplicationListener<ApplicationFailedEvent> {

    private SpringApplicationShutdownHandler shutdownHandler;

    public SecHubApplicationFailedHandler(SpringApplicationShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
    }

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        shutdownHandler.handleShutdown();
    }

}
