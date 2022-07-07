// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents meta data for an adapter - e.g. for restarts of a former adapter
 * exection after a JVM crash.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.NON_PRIVATE)
public class AdapterMetaData {

    int adapterVersion;

    Map<String, String> metaData = new TreeMap<>();

    public int getAdapterVersion() {
        return adapterVersion;
    }

    @JsonIgnore
    public Set<String> getKeys() {
        return new TreeSet<>(metaData.keySet());
    }

    /**
     * Set meta data
     *
     * @param key   - if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, String value) {
        metaData.put(failSafeKey(key), value);
    }

    /**
     * Set meta data
     *
     * @param key   - if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, boolean value) {
        metaData.put(failSafeKey(key), "" + value);
    }

    /**
     *
     * Set meta data
     *
     * @param key-  if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, long value) {
        setValue(key, "" + value);
    }

    /**
     * get meta data
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public String getValueAsStringOrNull(String key) {
        return metaData.get(failSafeKey(key));
    }

    /**
     * get meta data as long value
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public Long getValueAsLongOrNull(String key) {
        String value = getValueAsStringOrNull(key);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    /**
     * get meta data as boolean value
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
     * get meta data as boolean value
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
     * get meta data as uri value
     *
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public URI getValueAsURIorNull(String key) {
        String value = getValueAsStringOrNull(key);
        if (value == null) {
            return null;
        }
        return URI.create(value);
    }

    /**
     * Check if contains expected value for given key
     *
     * @param key
     * @param expectedValue
     * @return
     */
    public boolean hasValue(String key, String expectedValue) {
        String value = getValueAsStringOrNull(key);
        return Objects.equals(value, expectedValue);
    }

    private String failSafeKey(String key) {
        if (key == null) {
            return "null";
        }
        return key;
    }

    public boolean isExisting(String key) {
        String data = metaData.get(key);
        return data != null && !data.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "AdapterMetaData [adapterVersion=" + adapterVersion + ", metaData=" + metaData + "]";
    }

}
