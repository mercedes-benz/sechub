// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

public class EnvironmentVariableReader {

    /**
     * Reads the value of the environment variable {@link environmentVariable} and
     * returns the value as string.
     *
     * @param environmentVariable
     * @return Value of environmentVariable as string or null if the environment
     *         variable is not set
     */
    public String readAsString(String environmentVariable) {
        return System.getenv(environmentVariable);
    }

    /**
     * Reads the value of the environment variable {@link environmentVariable} and
     * returns the value as integer.
     *
     * @param environmentVariable
     * @return Value of environmentVariable as integer or 0 if the environment
     *         variable is not set
     * @throws IllegalArgumentException If the value of the environment variable
     *                                  {@link environmentVariable} is no valid
     *                                  integer.
     */
    public int readAsInt(String environmentVariable) {
        try {
            String value = System.getenv(environmentVariable);
            if (value == null) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value of " + environmentVariable + " is not a valid integer.");
        }
    }
}
