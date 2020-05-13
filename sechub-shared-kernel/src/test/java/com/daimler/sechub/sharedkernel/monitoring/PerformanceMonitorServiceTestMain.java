package com.daimler.sechub.sharedkernel.monitoring;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class PerformanceMonitorServiceTestMain {

    private static Random random;

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceMonitorServiceTestMain.class);

    public static void main(String[] args) {
        random = new Random(System.currentTimeMillis());
        
        enableTraceLogging(SystemMonitorService.class);
        enableTraceLogging(CPUMonitor.class);
        enableTraceLogging(MemoryUsageMonitor.class);
        
        SystemMonitorService monitor = new SystemMonitorService();
        int maxThreads = 3000;
        int threadNr = 0;
        while (true) {
            if (!isMaxReached(monitor)) {
                LOG.info("create threads");
                for (int i = 0; i < 50; i++) {
                    addMemoryAndCPUUsageByThread(monitor, LOG, maxThreads, threadNr);
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

    private static Logger enableTraceLogging(Class<?> clazz) {
        final Logger logger = LoggerFactory.getLogger(clazz);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger classic = (ch.qos.logback.classic.Logger) logger;
            classic.setLevel(Level.TRACE);
        }
        return logger;
    }

    private static boolean isMaxReached(SystemMonitorService monitor) {
        return monitor.isMemoryUsageMaxReached();
//        return monitor.isCPULoadAverageMaxReached() || 
    }
    
    private static void addMemoryAndCPUUsageByThread(SystemMonitorService monitor, final Logger logger, int maxThreads, int threadNr) {
        if (!monitor.isMemoryUsageMaxReached()) {
            /* each thread consumes 1 MB */
            Thread t = new Thread(() -> {
                try {

                    StringBuilder sb = new StringBuilder();

                    while (!Thread.interrupted() && !isMaxReached(monitor)) {
                        Thread.sleep(100);
                        /* consume a lot of memory */
                        for (int i = 0; i < 1000; i++) {
                            if (!isMaxReached(monitor)) {
                                sb.append(random.nextInt());
                            }
                        }
                    }
                    logger.trace("Thread done:" + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "test-thread:" + (threadNr));
            t.start();

        }
    }
}
