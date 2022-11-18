// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.assertJobFound;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.notNull;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobStream;

@Service
public class PDSGetJobStreamService {

    private static final int TRUNCATED_STREAM_SIZE = 2500;

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserFetchesJobStream(@PDSStep(name = "service call", description = "returns job's error stream", number = 2))
    public String getJobErrorStream(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);

        return truncateStream(job.getErrorStreamText());
    }

    @UseCaseUserFetchesJobStream(@PDSStep(name = "service call", description = "returns job's output stream", number = 2))
    public String getJobOutputStream(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);

        return truncateStream(job.getOutputStreamText());
    }

    private String truncateStream(String stream) {
        if (stream.length() > TRUNCATED_STREAM_SIZE)
            return stream.substring(0, TRUNCATED_STREAM_SIZE);
        return stream;
    }

}
