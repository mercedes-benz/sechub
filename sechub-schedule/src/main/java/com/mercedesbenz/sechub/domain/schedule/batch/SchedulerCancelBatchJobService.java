// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.batch;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.domain.schedule.SchedulingConstants;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class SchedulerCancelBatchJobService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerCancelBatchJobService.class);

    @Autowired
    JobExplorer explorer;

    @Autowired
    JobOperator operator;

    @Autowired
    UserInputAssertion assertion;

    @Transactional
    public boolean stopAllRunningBatchJobsForSechubJobUUID(UUID jobUUID) {
        assertion.isValidJobUUID(jobUUID);
        return stopAllRunningBatchJobsForSechubJobUUID(jobUUID, true, false);
    }

    @Transactional
    public boolean stopAndAbandonAllRunningBatchJobsForSechubJobUUID(UUID jobUUID) {
        assertion.isValidJobUUID(jobUUID);
        return stopAllRunningBatchJobsForSechubJobUUID(jobUUID, true, true);
    }

    @Transactional
    public boolean stopAndAbandonAllRunningBatchJobs() {
        return stopAllRunningBatchJobsForSechubJobUUID(null, true, true);
    }

    private boolean stopAllRunningBatchJobsForSechubJobUUID(UUID jobUUID, boolean stop, boolean abandon) {
        boolean foundAtLeastOne = false;
        /* prepare batch job */
        String jobUUIDasString = null;
        if (jobUUID != null) {
            jobUUIDasString = jobUUID.toString();
        }
        Set<JobExecution> found = explorer.findRunningJobExecutions(BatchConfiguration.JOB_NAME_EXECUTE_SCAN);
        for (JobExecution exec : found) {
            String sechubUUIDfound = exec.getJobParameters().getString(SchedulingConstants.BATCHPARAM_SECHUB_UUID);
            if (jobUUIDasString == null || jobUUIDasString.equals(sechubUUIDfound)) {
                /* found one */
                LOG.info("Found running batch-job {} for job uuid:{}", exec.getId(), jobUUIDasString);
                if (stop) {
                    stop(exec, jobUUID);
                }
                if (abandon) {
                    abandon(exec, jobUUID);
                }
                foundAtLeastOne = true;
            }
        }
        return foundAtLeastOne;
    }

    private void stop(JobExecution foundRunningExecution, UUID jobUUID) {
        Long executionId = foundRunningExecution.getId();
        try {
            operator.stop(executionId);
            LOG.info("Stopped running batch-job {} for {}", executionId, jobUUID);
        } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
            LOG.info("Was not able to stop running batch-job {} for {}", executionId, jobUUID);
        }
    }

    private void abandon(JobExecution foundRunningExecution, UUID jobUUID) {
        Long executionId = foundRunningExecution.getId();
        try {
            operator.abandon(executionId);
            LOG.info("Abandoned former stopped batch-job {} for {}", executionId, jobUUID);
        } catch (NoSuchJobExecutionException e) {
            LOG.warn("Cannot abondon given batch-job {} for {}", executionId, jobUUID, e);
        } catch (JobExecutionAlreadyRunningException e) {
            LOG.warn("Not Stopped so cannot abandon running batch-job {} for {}", executionId, jobUUID, e);
        }
    }

}
