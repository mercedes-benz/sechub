package com.daimler.sechub.sharedkernel.monitoring;

import java.lang.management.OperatingSystemMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CPUMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(CPUMonitor.class);
    
    private OperatingSystemMXBean osMBean;
    private CacheableMonitoringValue cpuData;
    private Object monitor = new Object();

    CPUMonitor(OperatingSystemMXBean osMBean, long cacheTimeInMilliseconds) {
        this.osMBean=osMBean;
        this.cpuData = new CacheableMonitoringValue(cacheTimeInMilliseconds);
    }
    
    /**
     * Calculate CPU load average - similar to
     * {@link SystemMonitorService#getCPULoadAverage()} but we measure average per
     * CPU/processor and also in a cached way, to reduce additional CPU usage by
     * measuring time.
     * 
     * @return CPU load average will return positive value when available ((0.0->1.0-...) but negative (e.g.-1) if not available
     */
    public double getCPULoadAverage() {
        if (osMBean == null) {
            return -1;
        }
        synchronized (monitor) {
            if (cpuData.isCacheValid()) {
                return cpuData.getValue();
            }
            double systemLoadAverage = osMBean.getSystemLoadAverage();
            
            if (systemLoadAverage < 0) {
                cpuData.setValue(systemLoadAverage);
            } else {
                int availableProcessors = osMBean.getAvailableProcessors();
                if (availableProcessors == 0) {
                    /* should never happen, but ... */
                    cpuData.setValue(-2);
                } else {
                    cpuData.setValue(systemLoadAverage / availableProcessors);
                }
            }
            double result = cpuData.getValue();
            LOG.trace("Checked cpu usage, value now:{}", result);
            return result;
        }
    }

}
