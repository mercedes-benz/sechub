// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.shutdown;

/**
 * Interface for listening to application shutdown events.
 * <p>
 * Implementing classes should define the behavior that should occur when the
 * application shuts down.
 * </p>
 *
 * @author hamidonos
 */
public interface ShutdownListener {

    /**
     * Method that is called on the listener when the application is shutting down.
     */
    void onShutdown();
}
