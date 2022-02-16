// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import java.lang.management.OperatingSystemMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CPUMonitor {

    private static final String KEY_DESCRIPTION = "description";

    private static final Logger LOG = LoggerFactory.getLogger(CPUMonitor.class);

    private OperatingSystemMXBean osMBean;
    private CacheableMonitoringValue cpuData;
    private Object monitor = new Object();

    CPUMonitor(OperatingSystemMXBean osMBean, long cacheTimeInMilliseconds) {
        this.osMBean = osMBean;
        setCacheTimeInMillis(cacheTimeInMilliseconds);
    }

    /**
     * Calculate CPU load average - similar to
     * {@link SystemMonitorService#getCPULoadAverage()} but we measure average per
     * CPU/processor and also in a cached way, to reduce additional CPU usage by
     * measuring time.
     *
     * @return CPU load average will return positive value when available
     *         ((0.0->1.0-...) but negative (e.g.-1) if not available
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
            int availableProcessors = osMBean.getAvailableProcessors();

            if (systemLoadAverage < 0) {
                cpuData.setValue(systemLoadAverage);
            } else {
                if (availableProcessors == 0) {
                    /* should never happen, but ... */
                    cpuData.setValue(-2);
                } else {
                    cpuData.setValue(systemLoadAverage / availableProcessors);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("CPU load average:").append(cpuData.getValue());
            sb.append(", system load average:").append(systemLoadAverage);
            sb.append(", available processors:").append(availableProcessors);

            cpuData.setAdditionalData(KEY_DESCRIPTION, sb.toString());

            double result = cpuData.getValue();
            LOG.trace("Checked cpu usage, value now:{}", result);
            return result;
        }
    }

    public String getDescription() {
        synchronized (monitor) {
            String description = (String) cpuData.getAdditionalData(KEY_DESCRIPTION);
            if (description == null) {
                return "<no cpu data available>";
            }
            return description;
        }
    }

    public void setCacheTimeInMillis(long cacheTimeInMilliseconds) {
        this.cpuData = new CacheableMonitoringValue(cacheTimeInMilliseconds);
    }

}
