// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.monitoring;

import org.assertj.core.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * This is just a simple test application to play around with the performance
 * monitor service. It was used to determine default values of accepted default
 * average load.
 *
 * @author Albert Tregnaghi
 *
 */
public class PerformanceMonitorServiceTestMain {

    private TestMode mode;
    private boolean freezeComputerAccepted = false;

    private enum TestMode {
        CPU,

        MEMORY,
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: arg0=" + Arrays.asList(TestMode.values()));
        }
        new PerformanceMonitorServiceTestMain(args[0]).measure();
    }

    PerformanceMonitorServiceTestMain(String mode) {
        this.mode = TestMode.valueOf(mode);
    }

    public void measure() {
        enableTraceLogging(SystemMonitorService.class);
        enableTraceLogging(CPUMonitor.class);
        enableTraceLogging(MemoryUsageMonitor.class);

        SystemMonitorService monitor = new SystemMonitorService();
        int maxThreads = mode == TestMode.CPU ? Runtime.getRuntime().availableProcessors() * 30 : 3000;
        int threadNr = 0;
        if (mode == TestMode.CPU) {
            if (freezeComputerAccepted) {
                new CPUConsumer().consumeCPUTime(monitor, maxThreads, 100, 500);
            } else {
                new SimulatedSchedulerAsManagedCPUConsumer().simulateScheduler(monitor, 100, 300);
            }
        }
        if (mode == TestMode.MEMORY) {
            new MemoryConsumer().consumeMemory(monitor, maxThreads, threadNr);
        }
    }

    private Logger enableTraceLogging(Class<?> clazz) {
        final Logger logger = LoggerFactory.getLogger(clazz);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger classic = (ch.qos.logback.classic.Logger) logger;
            classic.setLevel(Level.TRACE);
        }
        return logger;
    }

}
