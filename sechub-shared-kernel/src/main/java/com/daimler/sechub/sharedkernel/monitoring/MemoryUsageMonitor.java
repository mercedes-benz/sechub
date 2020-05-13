package com.daimler.sechub.sharedkernel.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sharedkernel.util.SimpleByteUtil;

public class MemoryUsageMonitor {
    

    private static final Logger LOG = LoggerFactory.getLogger(MemoryUsageMonitor.class);

    private Runtime runtime;
    private CacheableMonitoringValue memoryData;
    private Object monitor = new Object();
    
    MemoryUsageMonitor(Runtime runtime, long cacheTimeInMillis) {
        this.runtime=runtime;
        this.memoryData=new CacheableMonitoringValue(cacheTimeInMillis);
    }

    public double getMemoryUsageInPercent() {
        if (runtime == null) {
            return -1;
        }
        synchronized (monitor) {
            if (memoryData.isCacheValid()) {
                return memoryData.getValue();
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
                memoryData.setValue(memoryUsageInPercent);
            } else {
                memoryData.setValue(memoryUsageInPercent);
            }
            double result = memoryData.getValue();
            LOG.trace("Checked memory usage, value now:{}", result);
            return result;
        }

    }

    public String describeMemorySizesReadable() {
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        StringBuilder sb = new StringBuilder();
        sb.append("max:");
        sb.append(SimpleByteUtil.humanReadableBytesLength(maxMemory));
        sb.append(",allocated:");
        sb.append(SimpleByteUtil.humanReadableBytesLength(allocatedMemory));
        sb.append(",free:");
        sb.append(SimpleByteUtil.humanReadableBytesLength(freeMemory));
        return sb.toString();
    }
}
