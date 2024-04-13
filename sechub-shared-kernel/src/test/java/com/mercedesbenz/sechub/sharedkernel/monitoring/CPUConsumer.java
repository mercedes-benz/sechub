// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPUConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(CPUConsumer.class);

    private Random random;

    CPUConsumer() {
        this.random = new Random(System.currentTimeMillis());
    }

    public void consumeCPUTime(SystemMonitorService monitor, int maxThreads, int timeToSleepMin, int timeToSleepMax) {
        int[] timeToWait = new int[maxThreads];
        for (int i = 0; i < maxThreads; i++) {
            int r = random.nextInt();
            if (r < timeToSleepMin || r > timeToSleepMax) {
                continue;
            }
            timeToWait[i] = r;
        }

        for (int i = 0; i < maxThreads; i++) {
            final int pos = i;
            Thread t = new Thread(() -> {
                try {

                    while (!Thread.interrupted()) {
                        Thread.sleep(timeToWait[pos]);
                        if (monitor.isCPULoadAverageMaxReached()) {
                            continue;
                        }
                        /* simulate load */
                        for (int j = 0; j < 20000; j++) {
                            random.nextInt();
                        }
                        System.out.println("work done:" + Thread.currentThread().getName());
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "sechub-test-cpu-consumer-" + (i));
            t.start();
            LOG.info("created + started thread :" + t.getName());
        }
    }
}
