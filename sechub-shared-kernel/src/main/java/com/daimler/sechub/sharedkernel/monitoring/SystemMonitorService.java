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

    @Value("${sechub.monitoring.max.cpu.load:"+DEFAULT_MAX_ACCEPTED_LOAD_AVERAGE+"}")
    @MustBeDocumented(value="Maximum CPU load accepted by sechub system")
    private double maximumAcceptedCPULoadAverage = DEFAULT_MAX_ACCEPTED_LOAD_AVERAGE;
    
    @Value("${sechub.monitoring.max.memory.percentage:"+DEFAULT_MAX_ACCEPTED_MEM_AVERAGE+"}")
    @MustBeDocumented(value="Maximum memory usage percentage accepted by sechub system")
    private double maximumAcceptedMemoryUsage = DEFAULT_MAX_ACCEPTED_MEM_AVERAGE;

    @Value("${sechub.monitoring.cache.time.millis:"+DEFAULT_CACHE_TIME+"}")
    @MustBeDocumented(value="Time in milliseconds monitoring fetch results are cached before fetching again")
    private long cacheTimeInMilliseconds = DEFAULT_CACHE_TIME;
    
    private MemoryUsageMonitor memoryUsageMonitor;
    private CPUMonitor cpuMonitor;

    public SystemMonitorService() {

        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        try {
            osMBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

        } catch (IOException e) {
            LOG.error("Will not be able to check OS!", e);
        }
        memoryUsageMonitor = new MemoryUsageMonitor(Runtime.getRuntime(),cacheTimeInMilliseconds);
        cpuMonitor = new CPUMonitor(osMBean,cacheTimeInMilliseconds);
    }

    public boolean isCPULoadAverageMaxReached() {
        return getCPULoadAverage() > maximumAcceptedCPULoadAverage;
    }
    
    public double getCPULoadAverage() {
        return cpuMonitor.getCPULoadAverage();
    }

    public boolean isMemoryUsageMaxReached() {
        return getMemoryUsageInPercent() > maximumAcceptedMemoryUsage;
    }

    public double getMemoryUsageInPercent() {
        return memoryUsageMonitor.getMemoryUsageInPercent();
    }

    public String createCPULoadAverageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("CPU load average:");
        sb.append(cpuMonitor.getCPULoadAverage());
        sb.append("/");
        sb.append(maximumAcceptedCPULoadAverage);
        sb.append(";");
        return sb.toString();
    }

    public String createMemoryUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory usage:");
        sb.append(memoryUsageMonitor.getMemoryUsageInPercent());
        sb.append("/");
        sb.append(maximumAcceptedMemoryUsage);
        sb.append(";status=");
        sb.append(memoryUsageMonitor.describeMemorySizesReadable());
        sb.append(";");
        return sb.toString();
    }

}
