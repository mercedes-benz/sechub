// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

/**
 * Represents a possibility to do some callbacks at resilience side
 *
 * @author Albert Tregnaghi
 *
 */
public interface ResilienceCallback {

    /**
     * This method is called before each retry.
     *
     * @param context
     */
    public void beforeRetry(ResilienceContext context);
}
