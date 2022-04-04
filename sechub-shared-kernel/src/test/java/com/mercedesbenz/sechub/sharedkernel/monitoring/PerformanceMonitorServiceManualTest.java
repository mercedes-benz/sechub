// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.mercedesbenz.sechub.test.TestConstants;

/**
 * This is just a simple test application to play around with the performance
 * monitor service. It was used to determine default values of accepted default
 * average load.
 *
 * @author Albert Tregnaghi
 *
 */
class PerformanceMonitorServiceManualTest {

    private static final String MODE_PROPERTY = "sechub.manualtest.performance.monitor.mode";

    private enum TestMode {
        CPU,

        MEMORY,
    }

    @Test
    @EnabledIfSystemProperty(named = TestConstants.MANUAL_TEST_BY_DEVELOPER, matches = "true", disabledReason = TestConstants.DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST_FOR_GENERATION)
    void manualTestByDeveloper() {
        String testMode = System.getProperty(MODE_PROPERTY);
        if (testMode == null) {
            throw new IllegalArgumentException("usage: -D" + MODE_PROPERTY + "=" + Arrays.asList(TestMode.values()));
        }
        new InternalTestApplication(testMode).measure();
    }

    private class InternalTestApplication {

        private TestMode mode;
        private boolean freezeComputerAccepted = false;

        InternalTestApplication(String mode) {
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

}
