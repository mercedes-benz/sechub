// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.PDSNotFoundException;

public class PDSJobAssert {

    /**
     * Throws an {@link PDSNotFoundException} when job is not found
     *
     * @param jobUUID
     * @param repository
     * @return
     */
    public static PDSJob assertJobFound(UUID jobUUID, PDSJobRepository repository) {
        notNull(jobUUID, "job uuid may not be null!");
        notNull(repository, "repository may not be null!");

        Optional<PDSJob> found = repository.findById(jobUUID);
        if (!found.isPresent()) {
            throw new PDSNotFoundException("Given job does not exist!");
        }
        PDSJob pdsJob = found.get();
        return pdsJob;
    }

    /**
     * Assert job is in one of accepted states - if not a
     * {@link PDSNotAcceptableException} will be thrown
     *
     * @param job
     * @param accepted
     */
    public static void assertJobIsInState(PDSJob job, PDSJobStatusState... accepted) {
        notEmpty(accepted, "At least one accepted argument must be defined!");

        PDSJobStatusState jobState = job.getState();
        if (jobState == null) {
            throw new IllegalStateException("No job state set in job!");
        }
        for (PDSJobStatusState allowed : accepted) {
            if (allowed.equals(jobState)) {
                return;
            }
        }
        throw new PDSNotAcceptableException("Job in state:" + jobState + ", but accepted is only:" + Arrays.asList(accepted));
    }
}
