package com.daimler.sechub.sharedkernel.monitoring;

class CacheableMonitoringValue {
    
    private double value = -1;
    private long timestamp;
    private long cacheTimeInMillis;

    public CacheableMonitoringValue(long cacheTimeInMillis) {
        this.cacheTimeInMillis = cacheTimeInMillis;
    }

    public void setValue(double value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public double getValue() {
        return value;
    }

    public boolean isCacheValid() {
        long now = System.currentTimeMillis();
        if (now - timestamp < cacheTimeInMillis) {
            return true;
        }
        return false;
    }
}