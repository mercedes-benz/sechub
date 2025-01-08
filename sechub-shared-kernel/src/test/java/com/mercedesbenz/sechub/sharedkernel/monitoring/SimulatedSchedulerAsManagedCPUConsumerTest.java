// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedSchedulerAsManagedCPUConsumerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedSchedulerAsManagedCPUConsumerTest.class);
    private Random random;

    SimulatedSchedulerAsManagedCPUConsumerTest() {
        this.random = new Random(System.currentTimeMillis());
    }

    public void simulateScheduler(SystemMonitorService monitor, int maxPrimeNumberChecks, int millisBeforeNextScheduler) {
        int nr = 0;
        while (true) {
            LOG.info("Next simulated trigger, active threads:" + Thread.activeCount());
            /* simulate scheduler - periodically check */
            /* wait before next thread when cpu overloaded */
            if (!monitor.isCPULoadAverageMaxReached()) {
                simulateSchedulerBatchJobRunning(nr++, maxPrimeNumberChecks);
            } else {
                LOG.info("!!!! cpu overload " + monitor.createCPUDescription());
            }
            simulateTimeBeforeNextSchedulerTrigger(millisBeforeNextScheduler);
        }
    }

    private void simulateTimeBeforeNextSchedulerTrigger(int millisBeforeNextScheduler) {
        try {
            LOG.info("Wait for next scheuduling, will wait:{} millis", millisBeforeNextScheduler);
            Thread.sleep(millisBeforeNextScheduler);
            LOG.info("Wait done for next scheuduling");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateSchedulerBatchJobRunning(int nr, int maxPrimeNumberChecks) {

        Thread t = new Thread(() -> {
            simulateCPUload(maxPrimeNumberChecks);
        }, "sechub-test-cpu-load-by-prime-numbers-" + (nr));
        t.start();
        LOG.info("> thread {} started", t.getName());
    }

    /*
     * Simulate CPU load - in a way that java compiler does not optimize code...
     */
    private void simulateCPUload(int maxPrimeNumberChecks) {
        int primeNumberCount = 0;
        long start = System.currentTimeMillis();
        for (int j = 0; j < maxPrimeNumberChecks; j++) {
            int newRandom = random.nextInt();
            if (isPrimeNumber(newRandom)) {
                primeNumberCount++;
            }
        }
        long end = System.currentTimeMillis();
        long diff = end - start;

        LOG.info("work done:" + Thread.currentThread().getName() + " in " + diff + " millis , prime numbers found in " + maxPrimeNumberChecks + " tries was "
                + primeNumberCount);
    }

    private boolean isPrimeNumber(int num) {
        int i = 2;
        boolean isPrime = false;
        while (i <= num / 2) {
            // condition for non prime number
            if (num % i == 0) {
                isPrime = true;
                break;
            }

            ++i;
        }
        return isPrime;
    }

}
