package com.daimler.sechub.sharedkernel.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sharedkernel.util.SimpleByteUtil;

public class MemoryUsagePercentMonitor {
    

    private static final Logger LOG = LoggerFactory.getLogger(MemoryUsagePercentMonitor.class);

    private Runtime runtime;
    private CacheableMonitoringPercentage memoryData;
    private Object monitor = new Object();
    
    MemoryUsagePercentMonitor(Runtime runtime, long cacheTimeInMillis) {
        this.runtime=runtime;
        this.memoryData=new CacheableMonitoringPercentage(cacheTimeInMillis);
    }

    public double getMemoryUsageInPercent() {
        if (runtime == null) {
            return -1;
        }
        synchronized (monitor) {
            if (memoryData.isCacheValid()) {
                return memoryData.getPercentage();
            }
            
            long maxMemory = runtime.maxMemory();
            long allocatedMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            
            long memoryMaxOnePercent = maxMemory / 100;
            long usedMemory = allocatedMemory-freeMemory;
            
            
            if (LOG.isTraceEnabled()) {
                
                String maxMemoryString = SimpleByteUtil.humanReadableBytesLength(maxMemory);
                String allocatedMemoryString = SimpleByteUtil.humanReadableBytesLength(allocatedMemory);
                String freeMemoryString = SimpleByteUtil.humanReadableBytesLength(freeMemory);
                
                LOG.trace("Checked memory data, maxMemory:{}, allocatedMemory:{}, freeMemory:{}", maxMemoryString, allocatedMemoryString, freeMemoryString);
            }
            
            double usedMemoryPercentage=usedMemory / memoryMaxOnePercent;
            double memoryUsageInPercent = usedMemoryPercentage/100;// we use 0.1 for 10% etc. so 1 = 100%
            
            if (memoryUsageInPercent < 0) {
                memoryData.setPercentage(memoryUsageInPercent);
            } else {
                memoryData.setPercentage(memoryUsageInPercent);
            }
            double result = memoryData.getPercentage();
            LOG.trace("Checked memory usage, value now:{}", result);
            return result;
        }

    }
}
