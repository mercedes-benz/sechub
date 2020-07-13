package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.pds.security.PDSRoleConstants;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PDSJobTransactionService {

    @Autowired
    PDSJobRepository repository;

    /* mark as running - no matter of state before */
    @RolesAllowed({PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN})
    public void markReadyToStartInOwnTransaction(UUID jobUUID) {
        updateJobInOwnTransaction(jobUUID, null,null,null, PDSJobStatusState.READY_TO_START, PDSJobStatusState.CREATED);
    }

    /**
     * Marks job as running - no matter which state before
     * 
     * @param uuid
     */
    public void markJobAsRunningInOwnTransaction(UUID uuid) {
        updateJobInOwnTransaction(uuid, null, LocalDateTime.now(), null, PDSJobStatusState.RUNNING, PDSJobStatusState.values());
    }

    private void updateJobInOwnTransaction(UUID jobUUID, String result, LocalDateTime started, LocalDateTime ended, PDSJobStatusState newState, PDSJobStatusState... acceptedStatesBefore) {
        notNull(jobUUID, "job uuid may not be null!");
        
        PDSJob job = assertJobFound(jobUUID, repository);
        if (started!=null) {
            job.setStarted(started);
        }
        assertJobIsInState(job, acceptedStatesBefore);
        
        job.setState(newState);
        if (result != null) {
            job.setResult(result);
        }
        repository.save(job);
    }

    public String getJobConfiguration(UUID jobUUID) {
        return assertJobFound(jobUUID, repository).getJsonConfiguration();
    }
}
