// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

public interface SecurityLogService {

    /**
     * Log a security event.
     * 
     * @param type
     * @param message
     * @param objects
     */
    void log(SecurityLogType type, String message, Object... objects);

}