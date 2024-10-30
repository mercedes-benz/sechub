// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJobMessagesSupport;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.domain.schedule.strategy.SchedulerNextJobResolver;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemResumesSuspendedJobs;

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
    SchedulerNextJobResolver nextJobResolver;

    private ScheduleSecHubJobMessagesSupport jobMessageSupport = new ScheduleSecHubJobMessagesSupport();

    /**
     * @return either schedule job to execute, or <code>null</code> if no one has to
     *         be executed
     */
    @Transactional
    @UseCaseSystemResumesSuspendedJobs(@Step(number = 3, name = "Mark next job to execute", description = "When a suspended job is the next one, the job execution state will be changed to RESUMING"))
    public ScheduleSecHubJob markNextJobToExecuteByThisInstance() {

        if (LOG.isTraceEnabled()) {
            LOG.trace("Trigger execution of next job started");
        }

        UUID nextJobUUID = nextJobResolver.resolveNextJobUUID();
        if (nextJobUUID == null) {
            return null;
        }

        Optional<ScheduleSecHubJob> secHubJobOptional = jobRepository.getJobAndIncrementVersionWhenExecutable(nextJobUUID);
        if (!secHubJobOptional.isPresent()) {
            LOG.trace("Did not find executable job for next job uuid. Job UUID was {}. ", nextJobUUID);
            return null;
        }

        ScheduleSecHubJob secHubJob = secHubJobOptional.get();
        ExecutionState state = secHubJob.getExecutionState();

        if (ExecutionState.SUSPENDED.equals(state)) {
            secHubJob.setExecutionState(ExecutionState.RESUMING);
            secHubJob.setEnded(null); // reset end timestamp, because no longer "ended"
        } else {
            secHubJob.setExecutionState(ExecutionState.STARTED);
            secHubJob.setStarted(LocalDateTime.now());
        }
        return jobRepository.save(secHubJob);
    }

    @Transactional
    public void markJobExecutionFailed(ScheduleSecHubJob secHubJob) {
        if (secHubJob == null) {
            return;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Mark execution failed for job:{}", secHubJob.getUUID());
        }
        secHubJob.setExecutionResult(ExecutionResult.FAILED);
        secHubJob.setExecutionState(ExecutionState.ENDED);
        secHubJob.setEnded(LocalDateTime.now());
        jobMessageSupport.addMessages(secHubJob, Arrays.asList(new SecHubMessage(SecHubMessageType.ERROR, "Job execution failed")));
        jobRepository.save(secHubJob);
    }

    /**
     * Marks SecHub jobs for given UUIDS as SUSPENDED and also set ended date time.
     * This is done in a new transaction
     *
     * @param sechubJobUUIDs
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markJobsAsSuspended(Set<UUID> sechubJobUUIDs) {
        LOG.info("Mark jobs with next uuids as suspended: {}", sechubJobUUIDs);
        jobRepository.markJobsAsSuspended(sechubJobUUIDs, LocalDateTime.now());
    }

}
