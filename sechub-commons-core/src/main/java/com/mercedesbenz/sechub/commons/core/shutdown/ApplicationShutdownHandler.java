// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.shutdown;

/**
 * Interface for handling application shutdown events.
 *
 * <p>
 * Implementing classes should provide functionality to register a
 * {@link ShutdownListener} that will be invoked during the application shutdown
 * process.
 * </p>
 *
 * @author hamidonos
 */
public interface ApplicationShutdownHandler {

    /**
     * Registers a {@link ShutdownListener} to be notified when the application is
     * shutting down.
     *
     * @param shutdownListener The listener to be registered. It will be notified
     *                         during application shutdown.
     */
    void register(ShutdownListener shutdownListener);
}
