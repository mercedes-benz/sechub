// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.schedule.config.SchedulerConfigService;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.cluster.ClusterEnvironmentService;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;

@Service
public class SchedulerJobBatchTriggerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobBatchTriggerService.class);

	private static final int MINIMUM_RETRY_TIME_MS_TO_WAIT = 10;
	private static final int DEFAULT_TRIES = 5;
	private static final int DEFAULT_RETRIY_MAX_MILLIS = 300;

	@MustBeDocumented("Inside a cluster the next job fetching can lead to concurrent access. "
			+ "When this happens a retry can be done for the 'looser'. " + "This value defines the amount of *tries*"
			+ "If you do not want any retries set the value to a value lower than 2. 2 Means after one execution failed there is one retry. "
			+ "Values lower than 2 will lead to one try of execution only.")
	@Value("${sechub.config.trigger.nextjob.retries:" + DEFAULT_TRIES + "}")
	private int markNextJobRetries = DEFAULT_TRIES;

	@MustBeDocumented("When retry mechanism is enabled by `sechub.config.trigger.nextjob.retries`, and a retry is necessary, "
			+ "this value is used to define the maximum time period in millis which will be waited before retry. "
			+ "Why max value? Because cluster instances seems to be created often on exact same time by kubernetes. "
			+ "So having here a max value will result in a randomized wait time so cluster members will do "
			+ "fetch operations time shifted and automatically reduce collisions!")
	@Value("${sechub.config.trigger.nextjob.maxwaitretry:" + DEFAULT_RETRIY_MAX_MILLIS + "}")
	private int markNextJobWaitBeforeRetryMillis = DEFAULT_RETRIY_MAX_MILLIS;

	@Autowired
	ScheduleJobMarkerService markerService;

	@Autowired
	ScheduleJobLauncherService launcherService;

	@Autowired
	ClusterEnvironmentService environmentService;

	@Autowired
	SchedulerConfigService configService;

	// default 10 seconds
	@MustBeDocumented("Job scheduling is triggered by a cron job operation - default is 10 seconds. It can be configured different")
	@Scheduled(cron = "${sechub.config.trigger.nextjob.cron:*/10 * * * * *}")
	@UseCaseSchedulerStartsJob(@Step(number = 1, name = "Scheduling", description = "Fetches next schedule job from queue and trigger execution."))
	public void triggerExecutionOfNextJob() {
		if (LOG.isTraceEnabled()) {
			/* NOSONAR */LOG.trace("Trigger execution of next job started. Environment: {}",environmentService.getEnvironment());
		}
		if (!configService.isJobProcessingEnabled()){
			LOG.warn("Job processing is disabled, so cancel scheduling. Environment: {}",environmentService.getEnvironment());
			return;
		}
		RetryContext retryContext = new RetryContext(markNextJobRetries);
		do {
			try {
				ScheduleSecHubJob next = markerService.markNextJobExecutedByThisPOD();
				retryContext.executionDone();
				if (next == null) {
					return;
				}
				try {
					launcherService.executeJob(next);
				}catch(Exception e) {
					/* fatal failure happened, job launch was not executable */
					LOG.trace("was not able to execute next job, because fatal error occurred. Environment: {}",environmentService.getEnvironment());
					markerService.markJobExecutionFailed(next);
					retryContext.markAsFatalFailure();
				}

			} catch (OptimisticLockingFailureException e) {
				LOG.trace("was not able to trigger next, because already done. Environment: {}",environmentService.getEnvironment());

				retryContext.setRetryTimeToWait(createRandomTimeMillisToWait()).executionFailed();
			} catch (Exception e) {
				LOG.trace("was not able to trigger next job, because fatal error occurred. Environment: {}",environmentService.getEnvironment());

				retryContext.markAsFatalFailure();
			}
		} while (retryContext.isRetryPossible());

		if (! retryContext.isExecutionDone()) {
			LOG.warn("Was not able to handle trigger execution of next job, failed {} times. Environment:{}", retryContext.getExecutionFailedCount(), environmentService.getEnvironment());
		}
	}

	private int createRandomTimeMillisToWait() {
		/* fallback on wrong setup */
		if (markNextJobWaitBeforeRetryMillis<MINIMUM_RETRY_TIME_MS_TO_WAIT) {
			LOG.warn("Wrong configured markNextJobWaitBeforeRetryMillis :{}",markNextJobWaitBeforeRetryMillis);
			markNextJobWaitBeforeRetryMillis = MINIMUM_RETRY_TIME_MS_TO_WAIT+100;
			LOG.warn("Recalculated markNextJobWaitBeforeRetryMillis to:{}",markNextJobWaitBeforeRetryMillis);
		}
		return ThreadLocalRandom.current().nextInt(MINIMUM_RETRY_TIME_MS_TO_WAIT, markNextJobWaitBeforeRetryMillis);
	}

}
