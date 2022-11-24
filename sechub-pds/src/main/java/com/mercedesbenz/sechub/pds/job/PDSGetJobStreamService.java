// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.assertJobFound;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.notNull;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSGetJobStreamService {

    protected static final int TRUNCATED_STREAM_SIZE = 2500;

    @Autowired
    PDSJobRepository repository;

    public String getJobErrorStream(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);

        return truncateStream(job.getErrorStreamText());
    }

    public String getJobOutputStream(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);

        return truncateStream(job.getOutputStreamText());
    }

    protected String truncateStream(String stream) {
        if (stream.length() > TRUNCATED_STREAM_SIZE) {
            return stream.substring(stream.length() - TRUNCATED_STREAM_SIZE, stream.length());
        }
        return stream;
    }

}
