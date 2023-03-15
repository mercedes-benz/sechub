// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSCheckJobStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCheckJobStatusService.class);

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSStreamContentUpdateChecker refreshCheckCalculator;

    public boolean isJobStreamUpdateNecessary(UUID jobUUID) {
        LOG.trace("check job stream update was requested for PDS job:{}", jobUUID);

        PDSJob job = fetchJobDataContainingStreamContent(jobUUID);
        return refreshCheckCalculator.isUpdateRequestedAndNecessary(job);
    }

    private PDSJob fetchJobDataContainingStreamContent(UUID jobUUID) {
        PDSJob job = assertJobFound(jobUUID, repository);
        return job;
    }
}