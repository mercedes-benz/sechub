package com.daimler.sechub.adapter;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents meta data for an adapter - e.g. for restarts of a former adapter
 * exection after a JVM crash.
 * 
 * @author Albert Tregnaghi
 *
 */
public class AdapterMetaData {

    private int adapterVersion;
    
    private Map<String, String> metaData = new TreeMap<>();

    public int getAdapterVersion() {
        return adapterVersion;
    }

    public void setAdapterVersion(int adapterVersion) {
        this.adapterVersion = adapterVersion;
    }
    
    public Set<String> getKeys() {
        return new TreeSet<>(metaData.keySet());
    }
    
    /**
     * Set meta data  
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, String value) {
        metaData.put(failSafeKey(key), value);
    }
    /**
     * Set meta data  
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, boolean value) {
        metaData.put(failSafeKey(key), ""+value);
    }
    
    /**
     * 
     * Set meta data  
     * @param key- if key is <code>null</code> it will be handled like "null"
     * @param value
     */
    public void setValue(String key, long value) {
        setValue(key, ""+value);
    }
    
    /**
     * get meta data  
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public String getValue(String key) {
        return metaData.get(failSafeKey(key));
    }
    
    /**
     * get meta data as long value
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public Long getValueLong(String key) {
        String value = metaData.get(failSafeKey(key));
        if (value==null) {
            return null;
        }
        return Long.parseLong(value);
    }
    
    /**
     * get meta data as boolean value
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public Boolean getValueBoolean(String key) {
        String value = metaData.get(failSafeKey(key));
        if (value==null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * get meta data as uri value
     * @param key - if key is <code>null</code> it will be handled like "null"
     * @return value or <code>null</code>
     */
    public URI getValueURI(String key) {
        String value = metaData.get(failSafeKey(key));
        if (value==null) {
            return null;
        }
        return URI.create(value);
    }
    
    
    /**
     * Check if contains expected value for given key
     * @param key
     * @param expectedValue
     * @return
     */
    public boolean hasValue(String key, boolean expectedValue) {
        return hasValue(key, ""+expectedValue);
    }
    
    /**
     * Check if contains expected value for given key
     * @param key
     * @param expectedValue
     * @return
     */
    public boolean hasValue(String key, String expectedValue) {
        String value = getValue(key);
        return Objects.equals(value, expectedValue);
    }

    private String failSafeKey(String key) {
        if (key==null) {
            return "null";
        }
        return key;
    }
    
    public boolean isExisting(String key) {
        String data = metaData.get(key);
        return data!=null && !data.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "AdapterMetaData [adapterVersion=" + adapterVersion + ", metaData=" + metaData + "]";
    }



}
