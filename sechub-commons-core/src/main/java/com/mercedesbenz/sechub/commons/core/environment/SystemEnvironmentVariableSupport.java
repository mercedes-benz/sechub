// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.environment;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemEnvironmentVariableSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SystemEnvironmentVariableSupport.class);
    private static final String ENV_PREFIX_ID = "env:";

    private SystemEnvironment systemEnvironment;

    public SystemEnvironmentVariableSupport(SystemEnvironment systemEnvironment) {
        this.systemEnvironment = systemEnvironment;
    }

    /**
     * Will check if {@value#ENV_PREFIX_ID} is the prefix for the given value (case
     * insensitive). <br>
     * <br>
     * If the prefix is detected, the remaining parts are trimmed and used as a
     * variable name and the variable content is fetched from system environment.
     * <br>
     * <br>
     * If the prefix was not detected, the given string will be just returned.<br>
     * Some examples:
     * <ul>
     * <li>"env:MY_TEST_DATA"</li> will return the content of environment variable
     * "MY_TEST_DATA"
     * <li>"just a value"</li> will return "just a value"
     * </ul>
     *
     * @param value
     * @return vlaue or variable content, <code>null</code> when not defined
     */
    public String getValueOrVariableContent(String value) {
        if (value == null) {
            return null;
        }
        if (!value.toLowerCase().startsWith(ENV_PREFIX_ID)) {
            return value;
        }
        String key = value.substring(ENV_PREFIX_ID.length()).trim();

        String result = systemEnvironment.getEnv(key);
        if (result == null) {
            LOG.warn("No environment entry defined for variable: {}", key);
        }
        return result;
    }

    /**
     * Checks given value is defined in environment variable.
     *
     * @param envVariableName the name of the environment variable
     * @param value           the expected value to be inside the env variable
     *
     * @throws IllegalStateException    if the value is not null or empty but the
     *                                  environment variable does not contain this
     *                                  value
     * @throws IllegalArgumentException if the environment variable name is null or
     *                                  blank
     */
    public void assertDefinedByEnvironment(String envVariableName, String value) {
        if (envVariableName == null) {
            throw new IllegalArgumentException("Environment variable name must not be null!");
        }
        if (envVariableName.isBlank()) {
            throw new IllegalArgumentException("Environment variable name must not be blank!");
        }
        String found = systemEnvironment.getEnv(envVariableName);
        if (Objects.equals(value, found)) {
            return;
        }
        if (value != null && value.isBlank()) {
        }
        throw new IllegalStateException("The found value was not defined in env variable: " + envVariableName);

    }
}
