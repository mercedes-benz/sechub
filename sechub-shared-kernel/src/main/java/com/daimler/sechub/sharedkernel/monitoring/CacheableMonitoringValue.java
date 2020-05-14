package com.daimler.sechub.sharedkernel.monitoring;

import java.util.HashMap;
import java.util.Map;

class CacheableMonitoringValue {

    private double value = -1;
    private long timestamp;
    private long cacheTimeInMillis;
    private Map<String, Object> additionalData = new HashMap<>();

    public CacheableMonitoringValue(long cacheTimeInMillis) {
        this.cacheTimeInMillis = cacheTimeInMillis;
        updateCacheTimeStamp();// set initial timestamp
    }

    public void setValue(double value) {
        this.value = value;
        updateCacheTimeStamp();
    }

    public double getValue() {
        return value;
    }
    
    public Object getAdditionalData(String key) {
        return additionalData.get(key);
    }
    
    public void setAdditionalData(String key, Object value) {
        additionalData.put(key, value);
        updateCacheTimeStamp();
    }

    public boolean isCacheValid() {
        long now = System.currentTimeMillis();
        if (now - timestamp < cacheTimeInMillis) {
            return true;
        }
        return false;
    }
    
    private void updateCacheTimeStamp() {
        this.timestamp = System.currentTimeMillis();
    }

}