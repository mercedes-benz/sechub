// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.sharedkernel.logging.AlertLogReason.*;
import static com.mercedesbenz.sechub.sharedkernel.logging.AlertLogType.*;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.cluster.ClusterEnvironmentService;
import com.mercedesbenz.sechub.sharedkernel.logging.AlertLogService;
import com.mercedesbenz.sechub.sharedkernel.monitoring.SystemMonitorService;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;

import jakarta.annotation.PostConstruct;

@Service
public class SchedulerJobBatchTriggerService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobBatchTriggerService.class);

    private static final int MINIMUM_RETRY_TIME_MS_TO_WAIT = 10;
    private static final int DEFAULT_TRIES = 5;
    private static final int DEFAULT_RETRY_MAX_MILLIS = 300;
    private static final int DEFAULT_INITIAL_DELAY_MILLIS = 5000;
    private static final int DEFAULT_FIXED_DELAY_MILLIS = 10000;

    private static final boolean DEFAULT_HEALTHCHECK_ENABLED = true;

    @MustBeDocumented("Inside a cluster the next job fetching can lead to concurrent access. " + "When this happens a retry can be done for the 'looser'. "
            + "This value defines the amount of *tries*"
            + "If you do not want any retries set the value to a value lower than 2. 2 Means after one execution failed there is one retry. "
            + "Values lower than 2 will lead to one try of execution only.")
    @Value("${sechub.config.trigger.nextjob.retries:" + DEFAULT_TRIES + "}")
    private int markNextJobRetries = DEFAULT_TRIES;

    @MustBeDocumented("When retry mechanism is enabled by `sechub.config.trigger.nextjob.retries`, and a retry is necessary, "
            + "this value is used to define the maximum time period in millis which will be waited before retry. "
            + "Why max value? Because cluster instances seems to be created often on exact same time by kubernetes. "
            + "So having here a max value will result in a randomized wait time: means cluster members will do "
            + "fetch operations time shifted and this automatically reduces collisions!")
    @Value("${sechub.config.trigger.nextjob.maxwaitretry:" + DEFAULT_RETRY_MAX_MILLIS + "}")
    private int markNextJobWaitBeforeRetryMillis = DEFAULT_RETRY_MAX_MILLIS;

    @MustBeDocumented("Define initial delay for next job execution trigger. Interesting inside a cluster - just define this value different inside your instances (e.g. random value). This avoids write operations at same time.")
    @Value("${sechub.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS + "}")
    private String infoInitialDelay; // here only for logging - used in scheduler annotation as well!

    @MustBeDocumented("Define delay for next job execution trigger after last executed.")
    @Value("${sechub.config.trigger.nextjob.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    private String infoFixedDelay; // here only for logging - used in scheduler annotation as well!

    @MustBeDocumented("When enabled each trigger will do an healt check by monitoring service. If system has too much CPU load or uses too much memory, the trigger will not execute until memory and CPU load is at normal level!")
    @Value("${sechub.config.trigger.healthcheck.enabled:" + DEFAULT_HEALTHCHECK_ENABLED + "}")
    private boolean healthCheckEnabled = DEFAULT_HEALTHCHECK_ENABLED;

    @Autowired
    ScheduleJobMarkerService markerService;

    @Autowired
    ScheduleJobLauncherService launcherService;

    @Autowired
    ClusterEnvironmentService environmentService;

    @Autowired
    SchedulerConfigService configService;

    @Autowired
    SystemMonitorService monitorService;

    @Autowired
    AlertLogService alertLogService;

    @PostConstruct
    protected void postConstruct() {
        // show info about delay values in log (once)
        LOG.info("Scheduler service created with {} millisecondss initial delay and {} millisecondss as fixed delay", infoInitialDelay, infoFixedDelay);
    }

    // default 10 seconds delay and 5 seconds initial
    @MustBeDocumented(value = "Job scheduling is triggered by a cron job operation - default is 10 seconds to delay after last execution. "
            + "For initial delay " + DEFAULT_INITIAL_DELAY_MILLIS
            + " milliseconds are defined. It can be configured differently. This is useful when you need to startup a cluster. Simply change the initial delay values in to allow the cluster to startup.", scope = "schedule")
    @Scheduled(initialDelayString = "${sechub.config.trigger.nextjob.initialdelay:" + DEFAULT_INITIAL_DELAY_MILLIS
            + "}", fixedDelayString = "${sechub.config.trigger.nextjob.delay:" + DEFAULT_FIXED_DELAY_MILLIS + "}")
    @UseCaseSchedulerStartsJob(@Step(number = 1, name = "Scheduling", description = "Fetches next schedule job from queue and trigger execution."))
    public void triggerExecutionOfNextJob() {
        if (LOG.isTraceEnabled()) {
            /* NOSONAR */LOG.trace("Trigger execution of next job started. Environment: {}", environmentService.getEnvironment());
        }
        if (!configService.isJobProcessingEnabled()) {
            LOG.warn("Job processing is disabled, so cancel scheduling. Environment: {}", environmentService.getEnvironment());
            return;
        }

        if (healthCheckEnabled) {
            if (monitorService.isCPULoadAverageMaxReached()) {
                alertLogService.log(SCHEDULER_PROBLEM, CPU_OVERLOAD, "Job processing is skipped. {}, {}", monitorService.createCPUDescription(),
                        environmentService.getEnvironment());
                return;
            }
            if (monitorService.isMemoryUsageMaxReached()) {
                alertLogService.log(SCHEDULER_PROBLEM, MEMORY_OVERLOAD, "Job processing is skipped. {}, {}", monitorService.createMemoryDescription(),
                        environmentService.getEnvironment());
                return;
            }
        }
        RetryContext retryContext = new RetryContext(markNextJobRetries);
        do {
            try {
                ScheduleSecHubJob next = markerService.markNextJobToExecuteByThisInstance();
                retryContext.executionDone();
                if (next == null) {
                    return;
                }
                try {
                    launcherService.executeJob(next);
                } catch (Exception e) {
                    /* fatal failure happened, job launch was not executable */
                    LOG.trace("was not able to execute next job, because fatal error occurred. Environment: {}", environmentService.getEnvironment());
                    markerService.markJobExecutionFailed(next);
                    retryContext.markAsFatalFailure();
                }

            } catch (OptimisticLockingFailureException e) {
                LOG.trace("was not able to trigger next, because already done. Environment: {}", environmentService.getEnvironment());

                retryContext.setRetryTimeToWait(createRandomTimeMillisToWait()).executionFailed();
            } catch (Exception e) {
                LOG.trace("was not able to trigger next job, because fatal error occurred. Environment: {}", environmentService.getEnvironment());

                retryContext.markAsFatalFailure();
            }

        } while (retryContext.isRetryPossible());

        if (!retryContext.isExecutionDone()) {
            LOG.warn("Was not able to handle trigger execution of next job, failed {} times. Environment:{}", retryContext.getExecutionFailedCount(),
                    environmentService.getEnvironment());
        }
    }

    private int createRandomTimeMillisToWait() {
        /* fallback on wrong setup */
        if (markNextJobWaitBeforeRetryMillis < MINIMUM_RETRY_TIME_MS_TO_WAIT) {
            LOG.warn("Wrong configured markNextJobWaitBeforeRetryMillis :{}", markNextJobWaitBeforeRetryMillis);
            markNextJobWaitBeforeRetryMillis = MINIMUM_RETRY_TIME_MS_TO_WAIT + 100;
            LOG.warn("Recalculated markNextJobWaitBeforeRetryMillis to:{}", markNextJobWaitBeforeRetryMillis);
        }
        return ThreadLocalRandom.current().nextInt(MINIMUM_RETRY_TIME_MS_TO_WAIT, markNextJobWaitBeforeRetryMillis);
    }

}
