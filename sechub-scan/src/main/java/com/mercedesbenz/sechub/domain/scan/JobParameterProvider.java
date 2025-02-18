// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;

public class JobParameterProvider {

    protected Map<String, String> configuredExecutorParameters = new TreeMap<>();

    public JobParameterProvider(List<ProductExecutorConfigSetupJobParameter> jobParameters) {
        if (jobParameters == null) {
            return;
        }
        /* create a simple map containing parameters */
        for (ProductExecutorConfigSetupJobParameter jobParameter : jobParameters) {
            configuredExecutorParameters.put(jobParameter.getKey(), jobParameter.getValue());
        }
    }

    /**
     * Get parameter string value for given key
     *
     * @param key
     * @return string or <code>null</code>
     */
    public String get(String key) {
        if (key == null) {
            return null;
        }
        return configuredExecutorParameters.get(key);
    }

    /**
     * Get parameter boolean value for given key
     *
     * @param key
     * @return <code>true</code> when value for given key is "true" or "TRUE",
     *         otherwise false
     */
    public boolean getBoolean(String key) {
        String asText = get(key);
        return Boolean.parseBoolean(asText);
    }

    /**
     * Get parameter integer value for given key
     *
     * @param key
     * @return integer value or -1 if not defined
     */
    public int getInt(String key) {
        String asText = get(key);
        if (asText == null) {
            return -1;
        }
        try {
            return Integer.parseInt(asText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Get parameter long value for given key
     *
     * @param key
     * @return long value or -1 if not defined
     */
    public long getLong(String key) {
        String asText = get(key);
        if (asText == null) {
            return -1;
        }
        try {
            return Integer.parseInt(asText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(configuredExecutorParameters.keySet());
    }

}
