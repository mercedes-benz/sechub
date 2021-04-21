// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.domain.schedule.strategy.SchedulerStrategy;
import com.daimler.sechub.domain.schedule.strategy.SchedulerStrategyFactory;

/**
 * This service is only responsible to mark next {@link ScheduleSecHubJob} to execute.
 * This is done inside a transaction. Doing this inside an own service will hold the
 * transaction only to this service and end it.
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScheduleJobMarkerService {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduleJobMarkerService.class);

	@Autowired
	SecHubJobRepository jobRepository;
	
	@Autowired
	SchedulerStrategyFactory schedulerStrategyFactory;
	
	private SchedulerStrategy schedulerStrategy;

	/**
	 * @return either schedule job to execute, or <code>null</code> if no one has to be executed
	 */
	@Transactional
	public ScheduleSecHubJob markNextJobToExecuteByThisInstance() {
	    	
	    schedulerStrategy = schedulerStrategyFactory.build();

		if (LOG.isTraceEnabled()) {
			/*NOSONAR*/LOG.trace("Trigger execution of next job started");
		}
		
		UUID nextJobId = schedulerStrategy.nextJobId();
		if (nextJobId == null) {
		    return null;
		}

		Optional<ScheduleSecHubJob> secHubJobOptional = jobRepository.getJob(nextJobId);
		if (!secHubJobOptional.isPresent()) {
			if (LOG.isTraceEnabled()) {
				/*NOSONAR*/LOG.trace("No job found.");
			}
			return null;
		}
		ScheduleSecHubJob secHubJob = secHubJobOptional.get();
		secHubJob.setExecutionState(ExecutionState.STARTED);
		secHubJob.setStarted(LocalDateTime.now());
		return jobRepository.save(secHubJob);
	}

	@Transactional
	public void markJobExecutionFailed(ScheduleSecHubJob secHubJob) {
		if (secHubJob==null) {
			return;
		}
		if (LOG.isTraceEnabled()) {
			/*NOSONAR*/LOG.trace("Mark execution failed for job:{}",secHubJob.getUUID());
		}
		secHubJob.setExecutionResult(ExecutionResult.FAILED);
		secHubJob.setExecutionState(ExecutionState.ENDED);
		secHubJob.setEnded(LocalDateTime.now());
		jobRepository.save(secHubJob);
	}
}
