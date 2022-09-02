// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.environment;

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
}
