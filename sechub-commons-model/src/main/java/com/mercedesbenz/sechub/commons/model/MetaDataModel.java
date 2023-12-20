// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A simple meta data model which can be converted by json converter
 *
 * @author Albert Tregnaghi
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.NON_PRIVATE)
public class MetaDataModel {

    protected Map<String, String> metaData = new TreeMap<>();

    @JsonIgnore
    public Set<String> getKeys() {
        return new TreeSet<>(metaData.keySet());
    }

    /**
     * Set string value for given key
     *
     * @param key   - if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, String value) {
        metaData.put(failSafeKey(key), value);
    }

    /**
     * Set boolean value for given key
     *
     * @param key   - if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, boolean value) {
        metaData.put(failSafeKey(key), "" + value);
    }

    /**
     *
     * Set long value for given key
     *
     * @param key-  if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, long value) {
        setValue(key, "" + value);
    }

    /**
     * Resolves value as string
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public String getValueAsStringOrNull(String key) {
        return metaData.get(failSafeKey(key));
    }

    /**
     * Resolves value as Long object
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     * @throws NumberFormatException if value cannot be converted to a Long value
     */
    public Long getValueAsLongOrNull(String key) {
        String value = getValueAsStringOrNull(key);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    /**
     * Resolves value as boolean
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return <code>true</code> when value is "true", otherwise "false"
     */
    public boolean getValueAsBoolean(String key) {
        String value = getValueAsStringOrNull(key);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Resolves value as Boolean object or <code>null</code>
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public Boolean getValueAsBooleanOrNull(String key) {
        String value = getValueAsStringOrNull(key);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Resolves value as URI object or <code>null</code>
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     * @throws IllegalStateException when defined URI in model is not a valid
     */
    public URI getValueAsURIorNull(String key) {
        String value = getValueAsStringOrNull(key);
        if (value == null) {
            return null;
        }
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Value is not a URI:" + value, e);
        }
    }

    /**
     * Check if model contains expected value for given key
     *
     * @param key
     * @param expectedValue
     * @return <code>true</code> when model contains expected value
     */
    public boolean hasValue(String key, String expectedValue) {
        String value = getValueAsStringOrNull(key);
        return Objects.equals(value, expectedValue);
    }

    /**
     * Checks if a value exists for given key. Empty values are handled as non
     * existing.
     *
     * @param key
     * @return <code>true</code> when data for key exists, means not
     *         <code>null</code> or empty.
     */
    public boolean isExisting(String key) {
        String data = metaData.get(key);
        return data != null && !data.trim().isEmpty();
    }

    private String failSafeKey(String key) {
        if (key == null) {
            return "null";
        }
        return key;
    }
}
