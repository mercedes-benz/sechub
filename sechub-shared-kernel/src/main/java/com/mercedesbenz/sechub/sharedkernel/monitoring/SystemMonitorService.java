// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

import jakarta.annotation.PostConstruct;

/**
 * A service where callers can check current CPU and memory state of the running
 * machine. Service will provide metrics, descriptions and has got an check
 * methods if CPU and memory state is accepted or not.<br>
 * <br>
 * The configuration which CPU load average and memory usage is allowed is made
 * over spring values resolved inside this service. But the reactions to the
 * situation / metrics remains on caller side.<br>
 * <br>
 * For example: Scheduler batch trigger service is responsible for triggering
 * new batch actions. Before doing such an operation it is useful to check if
 * memory and CPU are available enough to execute next batch job - if not the
 * scheduler will just not trigger any new batch jobs and will retry later
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class SystemMonitorService {

    private static final int DEFAULT_CACHE_TIME = 2000; // two seconds cached
    private static final double MINIMUM_ALLOWED_CPU_AVERAGE_LOAD = 0.7;
    private static final double DEFAULT_MAX_ACCEPTED_CPU_LOAD_AVERAGE = 2.0;

    private static final double MINIMUM_ALLOWED_MEMORY_USAGE_PERCENTAGE = 50;
    private static final double DEFAULT_MAX_ACCEPTED_MEM_AVERAGE = 90;

    private static final Logger LOG = LoggerFactory.getLogger(SystemMonitorService.class);
    private OperatingSystemMXBean osMBean;

    @Value("${sechub.monitoring.accepted.cpu.average.max:" + DEFAULT_MAX_ACCEPTED_CPU_LOAD_AVERAGE + "}")
    @MustBeDocumented(value = "Maximum CPU load average accepted by sechub system. Value is calculated by measured system load average divided by available processors. A value above 1.0 usually means that a processor is very heavily loaded.", scope = SCOPE_MONITORING)
    private double maximumAcceptedCPULoadAverage = DEFAULT_MAX_ACCEPTED_CPU_LOAD_AVERAGE;

    @Value("${sechub.monitoring.accepted.memory.usage.max:" + DEFAULT_MAX_ACCEPTED_MEM_AVERAGE + "}")
    @MustBeDocumented(value = "Maximum memory usage percentage accepted by sechub system. Can be a value from 50 up to 100 for 100%", scope = SCOPE_MONITORING)
    private double maximumAcceptedMemoryUsage = DEFAULT_MAX_ACCEPTED_MEM_AVERAGE;

    @Value("${sechub.monitoring.cache.time.millis:" + DEFAULT_CACHE_TIME + "}")
    @MustBeDocumented(value = "Time in milliseconds monitoring fetch results are cached before fetching again", scope = SCOPE_MONITORING)
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
        memoryUsageMonitor = new MemoryUsageMonitor(new MemoryRuntime(), cacheTimeInMilliseconds);
        cpuMonitor = new CPUMonitor(osMBean, cacheTimeInMilliseconds);

    }

    /**
     * After spring dependency injection has been done we check that given spring
     * values are correct. If a user made some odd settings which would make SecHub
     * unusable there will be automatically fallbacks. Changes or detected odd setup
     * will be logged as warnings.
     */
    @PostConstruct
    public void init() {

        /* health check - fallback */
        healthCheckCPUSetup();
        healtchCheckMemorySetup();

        memoryUsageMonitor.setCacheTimeInMillis(cacheTimeInMilliseconds);
        cpuMonitor.setCacheTimeInMillis(cacheTimeInMilliseconds);
    }

    /**
     * Check if maximum of allowed CPU usage has been reached. Callers should stop
     * CPU intensive actions when this returns <code>true</code>
     *
     * @return <code>true</code> when maximum has been reached
     */
    public boolean isCPULoadAverageMaxReached() {
        return getCPULoadAverage() > maximumAcceptedCPULoadAverage;
    }

    /**
     * Returns calculate CPU load average
     *
     * @return CPU load average will return positive value when available
     *         ((0.0->1.0->...) but negative (e.g.-1) if not available
     */
    public double getCPULoadAverage() {
        return cpuMonitor.getCPULoadAverage();
    }

    /**
     * Check if maximum of allowed memory usage has been reached. Callers should
     * stop memory intensive actions when this returns <code>true</code>
     *
     * @return <code>true</code> when maximum has been reached
     */
    public boolean isMemoryUsageMaxReached() {
        return getMemoryUsageInPercent() > maximumAcceptedMemoryUsage;
    }

    /**
     * Resolves percentage of memory usage (results can be from 0 to 100)
     *
     * @return percentage of memory usage
     */
    public double getMemoryUsageInPercent() {
        return memoryUsageMonitor.getMemoryUsageInPercent();
    }

    public String createCPUDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Maximum accepted cpu load:");
        sb.append(maximumAcceptedCPULoadAverage);
        sb.append("; status=");
        sb.append(cpuMonitor.getDescription());
        return sb.toString();
    }

    public String createMemoryDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Maximum accepted memory usage:");
        sb.append(maximumAcceptedMemoryUsage);
        sb.append("; status=");
        sb.append(memoryUsageMonitor.getDescription());
        sb.append(";");
        return sb.toString();
    }

    private void healthCheckCPUSetup() {
        if (maximumAcceptedCPULoadAverage < MINIMUM_ALLOWED_CPU_AVERAGE_LOAD) {
            LOG.warn("You defined maximum accepted CPU load average wrong:{}, fallback to minimum allowed cpu average load:{}", maximumAcceptedCPULoadAverage,
                    MINIMUM_ALLOWED_CPU_AVERAGE_LOAD);
            maximumAcceptedCPULoadAverage = MINIMUM_ALLOWED_CPU_AVERAGE_LOAD;
        }
        if (maximumAcceptedCPULoadAverage > 10) {
            LOG.warn("Maybe too high configured maximum accepted CPU load average:{} ", maximumAcceptedCPULoadAverage);
        }
        LOG.info("Defined maximum CPU load average:{}", maximumAcceptedCPULoadAverage);
    }

    private void healtchCheckMemorySetup() {
        if (maximumAcceptedMemoryUsage < MINIMUM_ALLOWED_MEMORY_USAGE_PERCENTAGE) {
            LOG.warn("You defined maximum accepted memory usage wrong:{}, fallback to minimum allowed percentage:{}", maximumAcceptedMemoryUsage,
                    MINIMUM_ALLOWED_MEMORY_USAGE_PERCENTAGE);
            maximumAcceptedMemoryUsage = MINIMUM_ALLOWED_MEMORY_USAGE_PERCENTAGE;
        }
        if (maximumAcceptedMemoryUsage > 100) {
            LOG.warn("More than 100% defined:{}% - fallback to 100%", maximumAcceptedMemoryUsage);
            maximumAcceptedMemoryUsage = 100;
        }
        if (maximumAcceptedMemoryUsage > 95) {
            LOG.warn("Maybe too high configured maximum accepted memory usage:{}%", maximumAcceptedMemoryUsage);
        }
        LOG.info("Defined maximum MEMORY load average:{}", maximumAcceptedMemoryUsage);
    }

}
