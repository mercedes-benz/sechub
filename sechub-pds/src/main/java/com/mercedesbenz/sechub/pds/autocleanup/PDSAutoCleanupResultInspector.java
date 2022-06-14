// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

public interface PDSAutoCleanupResultInspector {

    /**
     * Inspects the given result and trigger appropriate actions
     *
     * @param result
     */
    void inspect(PDSAutoCleanupResult result);

}
