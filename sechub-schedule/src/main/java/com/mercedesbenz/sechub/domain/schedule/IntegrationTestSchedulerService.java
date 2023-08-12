// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob.*;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSchedulerService {

    private String DELETE_WAITING_JOBS_QUERY = "DELETE FROM " + ScheduleSecHubJob.CLASS_NAME + " t where t." + PROPERTY_STARTED + " is NULL";

    @Autowired
    private SecHubJobRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    /**
     * Deletes waiting jobs - job data will not be deleted here (for testing cleanup
     * it is okay to not delete job data).
     */
    public void deleteWaitingJobs() {
        /*
         * we do not add the query to the repository, because it is not used in
         * production but only for testing
         */
        Query query = entityManager.createQuery(DELETE_WAITING_JOBS_QUERY);
        query.executeUpdate();

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
        job.setJsonMessages(null);

        repository.save(job);
    }
}
