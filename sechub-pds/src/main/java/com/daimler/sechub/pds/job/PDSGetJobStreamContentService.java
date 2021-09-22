// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSMustBeDocumented;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesJobErrorStream;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesJobOutputStream;
import com.daimler.sechub.pds.util.PDSResillientRetryExecutor;
import com.daimler.sechub.pds.util.PDSResillientRetryExecutor.ExceptionThrower;

@Service
@RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
public class PDSGetJobStreamContentService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSGetJobStreamContentService.class);

    private static final int DEFAULT_RESILIENCE_MAX_RETRIES = 3;

    private static final long DEFAULT_WATCH_TIME_PERIOD_IN_MILLIS = 500;
    private static final long DEFAULT_WATCH_PERIOD_MAX_RETRIES = 10;

    @PDSMustBeDocumented("Maximum amount of tries to check if a stream data refresh has been handled (stream data has been updated)")
    @Value("${sechub.pds.config.job.stream.check.retries:" + DEFAULT_WATCH_PERIOD_MAX_RETRIES + "}")
    private int maximumRefreshCheckRetries;

    @PDSMustBeDocumented("Defines time in milliseconds for PDS job stream data update checks after job has been marked as necessary to have stream data refresh")
    @Value("${sechub.pds.config.job.stream.check.timetowait:" + DEFAULT_WATCH_TIME_PERIOD_IN_MILLIS + "}")
    private int timeToWaitForNextCheckInMilliseconds;

    @PDSMustBeDocumented("Maximum amount of retries to mark job stream data refresh")
    @Value("${sechub.pds.config.job.stream.mark.retries:" + DEFAULT_RESILIENCE_MAX_RETRIES + "}")
    private int maximumRefreshRequestRetries;

    private ExceptionThrower<IllegalStateException> streamDataRefreshExceptionThrower;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSJobTransactionService jobTransactionService;

    @Autowired
    PDSStreamContentUpdateChecker refreshCheckCalculator;

    public PDSGetJobStreamContentService() {

        streamDataRefreshExceptionThrower = new ExceptionThrower<IllegalStateException>() {

            @Override
            public void throwException(String message, Exception cause) throws IllegalStateException {
                throw new IllegalStateException("Job stream data refresh failed. " + message, cause);
            }
        };

    }

    @RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
    @UseCaseAdminFetchesJobOutputStream(@PDSStep(name = "service call", description = "Output stream content of PDS job shall be returned. "
            + "If the data is available in database and not outdated it will be returned directly. "
            + "Otherwise job is marked for refresh and service will wait until stream data has been updated by executing machine. "
            + "After succesful upate the new result will be returned. ", number = 2))
    public String getJobOutputStreamContentAsText(UUID jobUUID) {
        LOG.info("Administrator starts fetching output stream data for PDS job:{}", jobUUID);
        return getJobStreamAsText(jobUUID, StreamType.OUTPUT);
    }

    @RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
    @UseCaseAdminFetchesJobErrorStream(@PDSStep(name = "service call", description = "Error stream content of PDS job shall be returned. "
            + "If the data is available in database and not outdated it will be returned directly. "
            + "Otherwise job is marked for refresh and service will wait until stream data has been updated by executing machine. "
            + "After succesful upate the new result will be returned. ", number = 2))
    public String getJobErrorStreamContentAsText(UUID jobUUID) {
        LOG.info("Administrator starts fetching error stream data for PDS job:{}", jobUUID);
        return getJobStreamAsText(jobUUID, StreamType.ERROR);
    }

    /**
     * Fetches job stream data as text. When jobs are still running, the job stream
     * data in DB will be updated (if outdated) on executing machine.
     * 
     * @param jobUUID
     * @param streamType
     * @return
     */
    protected String getJobStreamAsText(UUID jobUUID, StreamType streamType) {
        notNull(jobUUID, "job uuid may not be null!");
        notNull(streamType, "streamType may not be null!");

        PDSJob job = fetchJobDataContainingStreamContent(jobUUID);

        switch (streamType) {
        case ERROR:
            return job.getErrorStreamText();
        case OUTPUT:
            return job.getOutputStreamText();
        default:
            throw new IllegalStateException("Unsupported stream type:" + streamType);
        }
    }

    private PDSJob fetchJobDataContainingStreamContent(UUID jobUUID) {
        PDSJob job = assertJobFound(jobUUID, repository);

        PDSJobStatusState jobState = job.getState();
        if (jobState == null) {
            throw new IllegalStateException("No job status state set in job:" + jobUUID);
        }
        if (refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)) {
            job = triggerRefreshRequestAndWaitForUpdate(job);
        }
        return job;
    }

    private PDSJob triggerRefreshRequestAndWaitForUpdate(PDSJob job) {
        UUID jobUUID = job.getUUID();

        LocalDateTime refreshRequestTime = markJobStreamDataRefreshRequestedResilient(jobUUID);

        /* wait */
        LOG.debug("Wait until refresh request for PDS job:{} has updated stream data", jobUUID);

        PDSJob updatedPDSjob = null;
        int amountOfChecks = 0;
        do {
            try {
                Thread.sleep(timeToWaitForNextCheckInMilliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            updatedPDSjob = assertJobFound(jobUUID, repository);

            if (!refreshCheckCalculator.isLastUpdateTooOld(updatedPDSjob.getLastStreamTxtUpdate(), refreshRequestTime)) {
                /* okay, data has been updated so return it... */
                LOG.debug("Stream data for PDS job:{} has been updated, stop waiting", jobUUID);
                return updatedPDSjob;
            } else {
                LOG.debug("Stream data for PDS job:{} still outdated - continue waiting", jobUUID);
            }
            amountOfChecks++;

        } while (amountOfChecks < maximumRefreshCheckRetries);

        /* still refresh necessary but took too much time... */
        throw new IllegalStateException("Timeout! Even after " + amountOfChecks + " retries, waiting each " + timeToWaitForNextCheckInMilliseconds
                + " milliseconds there was no update for stream data content of PDS job:" + jobUUID);

    }

    private LocalDateTime markJobStreamDataRefreshRequestedResilient(UUID jobUUID) {
        LOG.debug("Mark stream data refresh requested for PDS job:{}", jobUUID);
        /*
         * here we execute the refresh request in a resilient way - so updates by other
         * cluster members are gracefully accepted
         */
        PDSResillientRetryExecutor<IllegalStateException> executor = new PDSResillientRetryExecutor<>(getMaximumRefreshRequestRetries(),
                streamDataRefreshExceptionThrower, OptimisticLockingFailureException.class);

        LocalDateTime refreshRequestTime = executor.execute(() -> {
            return jobTransactionService.markJobStreamDataRefreshRequestedInOwnTransaction(jobUUID);
        }, "PDS job:" + jobUUID);
        return refreshRequestTime;
    }

    private int getMaximumRefreshRequestRetries() {
        return maximumRefreshRequestRetries;
    }

    private enum StreamType {
        ERROR, OUTPUT
    }

}
