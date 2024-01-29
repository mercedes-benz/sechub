// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.config;

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
}
