package com.daimler.sechub.sharedkernel.monitoring;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Service
public class SystemMonitorService {

    private static final int DEFAULT_CACHE_TIME = 10000; // ten seconds cached
    private static final double DEFAULT_MAX_ACCEPTED_LOAD_AVERAGE = 0.9;
    private static final double DEFAULT_MAX_ACCEPTED_MEM_AVERAGE = 0.9;
    private static final Logger LOG = LoggerFactory.getLogger(SystemMonitorService.class);
    private OperatingSystemMXBean osMBean;

    @Value("${sechub.monitoring.max.cpu.percentage:"+DEFAULT_MAX_ACCEPTED_LOAD_AVERAGE+"}")
    @MustBeDocumented(value="Maximum CPU percentage accepted by sechub system")
    private double maxCPULoadAverage = DEFAULT_MAX_ACCEPTED_LOAD_AVERAGE;
    
    @Value("${sechub.monitoring.max.memory.percentage:"+DEFAULT_MAX_ACCEPTED_MEM_AVERAGE+"}")
    @MustBeDocumented(value="Maximum CPU percentage accepted by sechub system")
    private double maxSystemMemoryAverage = DEFAULT_MAX_ACCEPTED_MEM_AVERAGE;

    @Value("${sechub.monitoring.cache.time.millis:"+DEFAULT_CACHE_TIME+"}")
    @MustBeDocumented(value="Time in milliseconds monitoring fetch results are cached before fetching again")
    private long cacheTimeInMilliseconds = DEFAULT_CACHE_TIME;
    
    private MemoryUsagePercentMonitor memoryUsagePercentageMonitor;
    private CPUMonitor cpuMonitor;

    public SystemMonitorService() {

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        try {
            osMBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

        } catch (IOException e) {
            LOG.error("Will not be able to check OS!", e);
        }
        memoryUsagePercentageMonitor = new MemoryUsagePercentMonitor(Runtime.getRuntime(),cacheTimeInMilliseconds);
        cpuMonitor = new CPUMonitor(osMBean,cacheTimeInMilliseconds);
    }

    public boolean isCPULoadAverageMaxReached() {
        return getCPULoadAverage() > maxCPULoadAverage;
    }

    public double getCPULoadAverage() {
        return cpuMonitor.getCPULoadAverage();
    }

    public boolean isMemoryAverageMaxReached() {
        return getMemoryUsageInPercent() > maxSystemMemoryAverage;
    }

    public double getMemoryUsageInPercent() {
        return memoryUsagePercentageMonitor.getMemoryUsageInPercent();
    }

}
