// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesJobErrorStream;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesJobOutputStream;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PDSJobTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSJobTransactionService.class);

    @Autowired
    PDSJobRepository repository;

    public PDSJobTransactionService() {
    }

    /**
     * Mark job as ready to start - state before allowed: only CREATED
     *
     * @param jobUUID
     */
    @RolesAllowed({ PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN })
    public void markReadyToStartInOwnTransaction(UUID jobUUID) {
        LOG.info("Mark job {} as ready to start", jobUUID);
        updateJobInOwnTransaction(jobUUID, null, null, null, PDSJobStatusState.READY_TO_START, PDSJobStatusState.CREATED);
    }

    /**
     * Mark job as running - no matter which state before
     *
     * @param jobUUID
     */
    public void markJobAsRunningInOwnTransaction(UUID jobUUID) {
        updateJobInOwnTransaction(jobUUID, null, LocalDateTime.now(), null, PDSJobStatusState.RUNNING, PDSJobStatusState.values());
    }

    /**
     * Mark job as being requested to refresh stream data
     *
     * @param jobUUID
     * @return local date time for the new refresh time stamp
     */
    @UseCaseAdminFetchesJobOutputStream(@PDSStep(name = "Request stream data refresh", description = "Updates the refresh request timestamp in database. This timestamp will be introspected while PDS job process execution - which will fetch and update stream content", number = 3))
    @UseCaseAdminFetchesJobErrorStream(@PDSStep(name = "Request stream data refresh", description = "Updates the refresh request timestamp in database. This timestamp will be introspected while PDS job process execution - which will fetch and update stream content", number = 3))
    public LocalDateTime markJobStreamDataRefreshRequestedInOwnTransaction(UUID jobUUID) {
        PDSJob job = assertJobFound(jobUUID, repository);

        updateJobRefreshRequestInOwnTransaction(job);

        return job.getLastStreamTextRefreshRequest();
    }

    private void updateJobRefreshRequestInOwnTransaction(PDSJob job) {
        job.lastStreamTextRefreshRequest = LocalDateTime.now();
        repository.save(job);

        LOG.debug("Updated job refresh stream text request in own transaction - PDS job uuid={}, lastStreamTxtRefreshTimeStamp={}", job.getUUID(),
                job.getLastStreamTextRefreshRequest());
    }

    private void updateJobInOwnTransaction(UUID jobUUID, String result, LocalDateTime started, LocalDateTime ended, PDSJobStatusState newState,
            PDSJobStatusState... acceptedStatesBefore) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);
        if (started != null) {
            job.setStarted(started);
        }
        assertJobIsInState(job, acceptedStatesBefore);

        job.setState(newState);
        if (result != null) {
            job.setResult(result);
        }

        repository.save(job);
        LOG.debug("Updated job in own transaction - PDS job uuid={}, state={}", job.getUUID(), job.getState());
    }

    /**
     * Read job configuration in own transaction
     *
     * @param jobUUID
     * @return job configuration, will fail when job is not found
     */
    public String getJobConfiguration(UUID jobUUID) {
        return assertJobFound(jobUUID, repository).getJsonConfiguration();
    }

    /**
     * Resolves next job to execute. If found the job will be marked as queued
     *
     * @return uuid or <code>null</code> if no job found to put in queue.
     */
    public UUID findNextJobToExecuteAndMarkAsQueued() {

        Optional<PDSJob> nextJob = repository.findNextJobToExecute();
        if (!nextJob.isPresent()) {
            return null;
        }
        PDSJob pdsJob = nextJob.get();
        pdsJob.setState(PDSJobStatusState.QUEUED);
        return pdsJob.getUUID();
    }

    public void updateJobStreamDataInOwnTransaction(UUID jobUUID, String outputStreamData, String errorStreamData) {
        PDSJob job = assertJobFound(jobUUID, repository);

        job.outputStreamText = outputStreamData;
        job.errorStreamText = errorStreamData;
        job.lastStreamTextUpdate = LocalDateTime.now();

        repository.save(job);
    }

    public void saveInOwnTransaction(PDSJob job) {
        repository.save(job);
    }
}
