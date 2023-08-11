// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJobMessagesSupport;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.domain.schedule.strategy.SchedulerStrategy;
import com.mercedesbenz.sechub.domain.schedule.strategy.SchedulerStrategyFactory;

/**
 * This service is only responsible to mark next {@link ScheduleSecHubJob} to
 * execute. This is done inside a transaction. Doing this inside an own service
 * will hold the transaction only to this service and end it.
 *
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

    private ScheduleSecHubJobMessagesSupport jobMessageSupport = new ScheduleSecHubJobMessagesSupport();

    /**
     * @return either schedule job to execute, or <code>null</code> if no one has to
     *         be executed
     */
    @Transactional
    public ScheduleSecHubJob markNextJobToExecuteByThisInstance() {

        schedulerStrategy = schedulerStrategyFactory.build();

        if (LOG.isTraceEnabled()) {
            /* NOSONAR */LOG.trace("Trigger execution of next job started");
        }

        UUID nextJobId = schedulerStrategy.nextJobId();
        if (nextJobId == null) {
            return null;
        }

        Optional<ScheduleSecHubJob> secHubJobOptional = jobRepository.getJob(nextJobId);
        if (!secHubJobOptional.isPresent()) {
            if (LOG.isTraceEnabled()) {
                /* NOSONAR */LOG.trace("No job found.");
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
        if (secHubJob == null) {
            return;
        }
        if (LOG.isTraceEnabled()) {
            /* NOSONAR */LOG.trace("Mark execution failed for job:{}", secHubJob.getUUID());
        }
        secHubJob.setExecutionResult(ExecutionResult.FAILED);
        secHubJob.setExecutionState(ExecutionState.ENDED);
        secHubJob.setEnded(LocalDateTime.now());
        jobMessageSupport.addMessages(secHubJob, Arrays.asList(new SecHubMessage(SecHubMessageType.ERROR, "Job execution failed")));
        jobRepository.save(secHubJob);
    }
}
