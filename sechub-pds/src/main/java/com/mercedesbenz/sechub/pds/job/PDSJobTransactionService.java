// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionData;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobErrorStream;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobMetaData;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobOutputStream;

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

    public void forceStateResetInOwnTransaction(Set<UUID> jobUUIDs, PDSJobStatusState forcedStatusState) {
        LOG.info("Force state change to '{}' for jobs: {}", forcedStatusState, jobUUIDs);
        repository.forceStateForJobs(forcedStatusState, jobUUIDs);
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
    @UseCaseAdminFetchesJobMetaData(@PDSStep(name = "Request meta data refresh", description = "Updates the refresh request timestamp in database. This timestamp will be introspected while PDS job process execution - which will fetch meta data content (if available)", number = 3))
    public LocalDateTime markJobExecutionDataRefreshRequestedInOwnTransaction(UUID jobUUID) {
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
        PDSJobStatusState oldState = job.getState();
        assertJobIsInState(job, acceptedStatesBefore);

        job.setState(newState);
        if (result != null) {
            job.setResult(result);
        }

        repository.save(job);

        LOG.debug("Updated job in own transaction - PDS job uuid: {}, newState: {}, oldState: {}", job.getUUID(), job.getState(), oldState);
    }

    /**
     * Read job configuration data in own transaction
     *
     * @param jobUUID
     * @return job configuration, will fail when job is not found
     */
    public JobConfigurationData getJobConfigurationData(UUID jobUUID) {

        PDSJob job = assertJobFound(jobUUID, repository);

        JobConfigurationData data = new JobConfigurationData();
        data.jobConfigurationJson = job.getJsonConfiguration();
        data.metaData = job.getMetaDataText();

        return data;
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

    public void updateJobExecutionDataInOwnTransaction(UUID jobUUID, PDSExecutionData data) {
        PDSJob job = assertJobFound(jobUUID, repository);

        job.outputStreamText = data.getOutputStreamData();
        job.errorStreamText = data.getErrorStreamData();
        job.lastStreamTextUpdate = LocalDateTime.now();
        job.metaDataText = data.getMetaData();

        repository.save(job);
    }

    public void saveInOwnTransaction(PDSJob job) {
        repository.save(job);
    }

    public void updateJobMessagesInOwnTransaction(UUID jobUUID, SecHubMessagesList sechubMessageList) {
        PDSJob job = assertJobFound(jobUUID, repository);
        job.messages = sechubMessageList.toJSON();

        repository.save(job);
    }

    public void markJobAsCancelRequestedInOwnTransaction(UUID jobUUID) {
        updatJobStatusState(jobUUID, PDSJobStatusState.CANCEL_REQUESTED);
    }

    public void markJobAsCanceledInOwnTransaction(UUID jobUUID) {
        updatJobStatusState(jobUUID, PDSJobStatusState.CANCELED);
    }

    private void updatJobStatusState(UUID jobUUID, PDSJobStatusState state) {
        PDSJob job = assertJobFound(jobUUID, repository);
        PDSJobStatusState oldState = job.getState();
        if (state == oldState) {
            LOG.info("Did not change PDS job: {} status state:{} already set.", jobUUID, state);
            return;
        }
        job.setState(state);
        repository.save(job);

        LOG.info("Changed PDS job: {} from status state: {} to: {}", jobUUID, oldState, state);
    }

}
