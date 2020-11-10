// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

import org.springframework.stereotype.Component;

@Component
public class SystemEnvironment {

    /**
     * Gets the value of the specified environment variable. An environment variable is a system-dependent external named value.
     * @param variableName
     * @return value or <code>null</code>
     */
    public String getEnv(String variableName) {
        if (variableName==null) {
            return null;
        }
        return System.getenv(variableName);
    }
}
