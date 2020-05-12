package com.daimler.sechub.sharedkernel.monitoring;

class CacheableMonitoringPercentage {
    
    private double percentage = -1;
    private long timestamp;
    private long cacheTimeInMillis;

    public CacheableMonitoringPercentage(long cacheTimeInMillis) {
        this.cacheTimeInMillis = cacheTimeInMillis;
    }

    public void setPercentage(double value) {
        this.percentage = value;
        this.timestamp = System.currentTimeMillis();
    }

    public double getPercentage() {
        return percentage;
    }

    public boolean isCacheValid() {
        long now = System.currentTimeMillis();
        if (now - timestamp < cacheTimeInMillis) {
            return true;
        }
        return false;
    }
}