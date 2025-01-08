// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMemoryConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TestMemoryConsumer.class);
    private Random random;

    public void consumeMemory(SystemMonitorService monitor, int maxThreads, int threadNr) {
        this.random = new Random(System.currentTimeMillis());
        while (true) {

            if (!monitor.isMemoryUsageMaxReached()) {
                LOG.info("create threads");
                for (int i = 0; i < 50; i++) {
                    addMemoryUsageByThread(monitor, LOG, maxThreads, threadNr);
                    threadNr++;
                }
            }
            System.out.println("performance testmain: CPU load average:" + monitor.getCPULoadAverage() + ", cpu-max:" + monitor.isCPULoadAverageMaxReached()
                    + ", mem-usage:" + monitor.getMemoryUsageInPercent() + ", mem-max:" + monitor.isMemoryUsageMaxReached());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void addMemoryUsageByThread(SystemMonitorService monitor, final Logger logger, int maxThreads, int threadNr) {
        if (!monitor.isMemoryUsageMaxReached()) {
            Thread t = new Thread(() -> {
                try {

                    StringBuilder sb = new StringBuilder();

                    while (!Thread.interrupted() && !monitor.isMemoryUsageMaxReached()) {
                        Thread.sleep(100);
                        /* consume a lot of memory */
                        for (int i = 0; i < 1000; i++) {
                            if (!monitor.isMemoryUsageMaxReached()) {
                                sb.append(random.nextInt());
                            }
                        }
                    }
                    logger.trace("Thread done:" + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "sechub-test-memory-consumer:" + (threadNr));
            t.start();

        }
    }
}
