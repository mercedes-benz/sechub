// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;

/**
 * This service is only reponsible to mark next {@link ScheduleSecHubJob} to execute.
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

	/**
	 * @return either schedule job to execute, or <code>null</code> if no one has to be executed
	 */
	@Transactional
	public ScheduleSecHubJob markNextJobExecutedByThisPOD() {

		if (LOG.isTraceEnabled()) {
			/*NOSONAR*/LOG.trace("Trigger execution of next job started");
		}

		Optional<ScheduleSecHubJob> secHubJobOptional = jobRepository.findNextJobToExecute();
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
