// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.autocleanup;

public interface AutoCleanupResultInspector {

    /**
     * Inspects the given result and trigger appropriate actions
     *
     * @param result
     */
    void inspect(AutoCleanupResult result);

}
