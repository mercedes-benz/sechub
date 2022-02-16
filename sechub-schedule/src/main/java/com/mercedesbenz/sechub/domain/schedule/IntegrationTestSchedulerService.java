// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSchedulerService {

    @Autowired
    private SecHubJobRepository repository;

    @Transactional
    public void deleteWaitingJobs() {
        repository.deleteWaitingJobs();
    }

    /**
     * Reverts/Marks given job as still running - will reset result, state, end
     * timestamp and traffic light
     *
     * @param sechubJobUUID
     */
    public void revertJobAsStillRunning(UUID sechubJobUUID) {
        Optional<ScheduleSecHubJob> found = repository.findById(sechubJobUUID);
        if (!found.isPresent()) {
            throw new NotFoundException("Job not found!");
        }
        ScheduleSecHubJob job = found.get();
        job.setExecutionResult(ExecutionResult.NONE);
        job.setExecutionState(ExecutionState.STARTED);
        job.setEnded(null);
        job.setTrafficLight(null);

        repository.save(job);
    }

    public void revertJobAsStillNotApproved(UUID sechubJobUUID) {
        Optional<ScheduleSecHubJob> found = repository.findById(sechubJobUUID);
        if (!found.isPresent()) {
            throw new NotFoundException("Job not found!");
        }
        ScheduleSecHubJob job = found.get();
        job.setExecutionResult(ExecutionResult.NONE);
        job.setExecutionState(ExecutionState.INITIALIZING);
        job.setEnded(null);
        job.setTrafficLight(null);

        repository.save(job);
    }
}
