// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SecHubJobTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubJobTransactionService.class);

    @Autowired
    SecHubJobRepository repository;

    public void updateExecutionStateInOwnTransaction(UUID sechubJobUUID, ExecutionState newState) {
        notNull(sechubJobUUID, "sechub job uuid may not be null!");
        notNull(newState, "newState may not be null!");

        Optional<ScheduleSecHubJob> jobOpt = repository.findById(sechubJobUUID);
        if (!jobOpt.isPresent()) {
            LOG.warn("Was not able to update execution state of job:{} because it does not exist!", sechubJobUUID);
            return;
        }

        ScheduleSecHubJob job = jobOpt.get();
        if (newState.equals(job.getExecutionState())) {
            LOG.debug("Job :{} was already in execution state: {} so no update necessary", sechubJobUUID, newState);
            return;
        }
        job.setExecutionState(newState);
        repository.save(job);
        LOG.info("Job :{} has now execution state: {}", sechubJobUUID, job.getExecutionState());
    }

    public void saveInOwnTransaction(ScheduleSecHubJob job) {
        repository.save(job);
    }

}
