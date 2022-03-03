// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

public class EnvironmentVariableReader {

    /**
     *
     * @param environmentVariable
     * @return Value of environmentVariable as string or null if the environment
     *         variable is not set
     */
    public String readAsString(String environmentVariable) {
        return System.getenv(environmentVariable);
    }

    /**
     *
     * @param environmentVariable
     * @return Value of environmentVariable as integer or 0 if the environment
     *         variable is not set
     */
    public int readAsInt(String environmentVariable) {
        try {
            String value = System.getenv(environmentVariable);
            if (value == null) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value of " + environmentVariable
                    + " is not a valid integer. Please configure the owasp zap server via environment variables/command line parameters.");
        }
    }

}
