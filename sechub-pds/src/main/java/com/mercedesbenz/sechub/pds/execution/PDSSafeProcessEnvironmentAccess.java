// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static java.util.Objects.*;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides safe access to process environment variables.
 */
public class PDSSafeProcessEnvironmentAccess {

    private static final Logger logger = LoggerFactory.getLogger(PDSSafeProcessEnvironmentAccess.class);

    private Map<String, String> environment;

    public PDSSafeProcessEnvironmentAccess(Map<String, String> environment) {
        this.environment = requireNonNull(environment, "Parameter 'environment' may not be null!");
    }

    /**
     * Set environment value for given key.
     *
     * @param key   the key for the environment map. Operation will be skipped, if
     *              key is <code>null</code>
     * @param value the value for the environment map. Operation will be skipped, if
     *              value is <code>null</code>
     */
    public void put(String key, String value) {
        if (key == null) {
            logger.warn("null key detected, skip put operation");
            return;
        }
        if (value == null) {
            logger.warn("null value for key '" + key + "' detected, skip put operation");
            return;
        }
        environment.put(key, value);
    }

    public Set<String> getKeys() {
        return environment.keySet();
    }

    public String get(String key) {
        return environment.get(key);
    }

}
