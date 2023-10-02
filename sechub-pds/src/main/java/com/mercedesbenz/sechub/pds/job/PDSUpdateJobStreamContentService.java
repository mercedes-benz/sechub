// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor.ExceptionThrower;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
public class PDSUpdateJobStreamContentService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSUpdateJobStreamContentService.class);

    private static final int RESILIENCE_MAX_RETRIES_JOB_STREAM_DATA_REFRESH_REQUEST = 3;

    private ExceptionThrower<IllegalStateException> exceptionThrower;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSJobTransactionService jobTransactionService;

    @Autowired
    PDSStreamContentUpdateChecker refreshCheckCalculator;

    public PDSUpdateJobStreamContentService() {

        exceptionThrower = new ExceptionThrower<IllegalStateException>() {

            @Override
            public void throwException(String message, Exception cause) throws IllegalStateException {
                throw new IllegalStateException("Job stream data update failed. " + message, cause);
            }
        };

    }

    public void setJobStreamAsText(UUID jobUUID, String outputStreamText, String errorStreamText) {
        notNull(jobUUID, "job uuid may not be null!");
        notNull(outputStreamText, "outputStreamText may not be null!");
        notNull(errorStreamText, "errorStreamText may not be null!");

        /*
         * here we execute the refresh request in a resilient way - so updates by other
         * cluster members are gracefully accepted
         */
        PDSResilientRetryExecutor<IllegalStateException> executor = new PDSResilientRetryExecutor<>(getMaximumRefreshRequestRetries(), exceptionThrower,
                OptimisticLockingFailureException.class);

        executor.execute(() -> {
            PDSJob job = assertJobFound(jobUUID, repository);
            job.outputStreamText = outputStreamText;
            job.errorStreamText = errorStreamText;
            job.lastStreamTextUpdate = LocalDateTime.now();

            jobTransactionService.saveInOwnTransaction(job);

            LOG.debug("updated stream data fields for PDS job:{}", jobUUID);

            return null;
        }, "PDS job:" + jobUUID);
    }

    private int getMaximumRefreshRequestRetries() {
        return RESILIENCE_MAX_RETRIES_JOB_STREAM_DATA_REFRESH_REQUEST;
    }

}
