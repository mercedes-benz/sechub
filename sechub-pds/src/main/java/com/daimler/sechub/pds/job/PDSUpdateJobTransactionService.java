package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PDSUpdateJobTransactionService {

    @Autowired
    PDSJobRepository repository;

    /**
     * Updates job state and - if not null - the result
     * 
     * @param jobUUID
     * @param result
     * @param newState
     * @param acceptedStatesBefore
     */
    public void updateJobInOwnTransaction(UUID jobUUID, String result, PDSJobStatusState newState, PDSJobStatusState... acceptedStatesBefore) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);
        assertJobIsInState(job, acceptedStatesBefore);

        job.setState(newState);
        if (result != null) {
            job.setResult(result);
        }
        repository.save(job);
    }

    public void markReadyToStartInOwnTransaction(UUID jobUUID) {
        updateJobInOwnTransaction(jobUUID, null, PDSJobStatusState.READY_TO_START, PDSJobStatusState.CREATED);
    }

    /* mark as running - no matter of state before */
    
    /**
     * Marks job as running - no matter which state before
     * @param uuid
     */
    public void markJobAsRunningInOwnTransaction(UUID uuid) {
        updateJobInOwnTransaction(uuid, null, PDSJobStatusState.RUNNING, PDSJobStatusState.values());
    }

}
