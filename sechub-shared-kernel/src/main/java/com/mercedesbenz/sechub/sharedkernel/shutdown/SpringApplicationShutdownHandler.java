// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.shutdown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.commons.core.shutdown.ShutdownListener;

/**
 * This class is responsible for handling the shutdown of the application inside
 * the Spring context. It listens for the {@link ContextClosedEvent} and informs
 * all registered {@link ShutdownListener}s.
 *
 * @author hamidonos
 */
@Component
public class SpringApplicationShutdownHandler implements ApplicationShutdownHandler {

    private static final Logger log = LoggerFactory.getLogger(SpringApplicationShutdownHandler.class);

    private final Collection<ShutdownListener> shutdownListeners = Collections.synchronizedCollection(new ArrayList<>());

    @Override
    public void register(ShutdownListener shutdownListener) {
        shutdownListeners.add(shutdownListener);
    }

    public void handleShutdown() {
        log.info("Start handling shutdown - informing shutdown listeners");
        shutdownListeners.forEach(listener -> {
            try {
                listener.onShutdown();
            } catch (RuntimeException e) {
                log.error("Notified shutdown listener failed: {}", listener.getClass(), e);
            }
        });
    }

    Collection<ShutdownListener> getShutdownListeners() {
        return Collections.unmodifiableCollection(shutdownListeners);
    }
}
