// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.job.PDSJob;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;

@Service
public class PDSApplyFutureExecutionResultToJobService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSApplyFutureExecutionResultToJobService.class);

    @Autowired
    PDSJobRepository repository;

    /**
     * Applies given future of an execution result to the given PDS job.
     *
     * @param future future result
     * @param job    pds
     */
    public void applyResultToJob(Future<PDSExecutionResult> future, PDSJob job) {
        // we use this moment of time for all, currently the easiest and central way
        job.setEnded(LocalDateTime.now());

        UUID jobUUID = job.getUUID();
        if (future.isCancelled()) {
            job.setState(PDSJobStatusState.CANCELED);
        } else {
            PDSExecutionResult callResult;
            try {
                callResult = future.get();
                LOG.debug("Fetch job result from future, pds job uuid={}, state={}", jobUUID, job.getState());
                job.setResult(callResult.getResult());

                if (callResult.isCanceled()) {
                    job.setState(PDSJobStatusState.CANCELED);
                } else if (callResult.isFailed()) {
                    job.setState(PDSJobStatusState.FAILED);
                    if (callResult.isEncryptionFailure()) {
                        job.setEncryptionOutOfSync(true);
                    }
                } else {
                    job.setState(PDSJobStatusState.DONE);
                }

            } catch (InterruptedException e) {
                LOG.error("Job with uuid:{} was interrupted", jobUUID, e);

                job.setState(PDSJobStatusState.FAILED);
                job.setResult("Job interrupted");
            } catch (ExecutionException e) {
                LOG.error("Job with uuid:{} failed in execution", jobUUID, e);

                job.setState(PDSJobStatusState.FAILED);
                job.setResult("Job execution failed");
            }
            LOG.debug("Handled job result and state job uuid={}, state={}", jobUUID, job.getState());
        }
        repository.save(job);
        LOG.debug("Stored job pds uuid={}, state={}", jobUUID, job.getState());
    }

}
