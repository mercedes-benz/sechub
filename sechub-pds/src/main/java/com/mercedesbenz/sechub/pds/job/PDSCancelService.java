// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.usecase.PDSDocumentationScopeConstants.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService.CancelResult;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemHandlesJobCancelRequests;

@Service
public class PDSCancelService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCancelService.class);
    private static final int DEFAULT_TIME_IN_MINUTES_BEFORE_TREATED_AS_ORPHANED_CANCEL = 60;

    @PDSMustBeDocumented(value = "The time in minutes after which a cancel job request is treated as orphaned.", scope = SCOPE_JOB)
    @Value("${pds.config.cancelrequest.minutes.before.treated.orphaned:" + DEFAULT_TIME_IN_MINUTES_BEFORE_TREATED_AS_ORPHANED_CANCEL + "}")
    int minutesToWaitBeforeTreatedAsOrphaned = DEFAULT_TIME_IN_MINUTES_BEFORE_TREATED_AS_ORPHANED_CANCEL;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSJobTransactionService jobTransactionService;

    @Autowired
    PDSExecutionService executionService;

    @UseCaseSystemHandlesJobCancelRequests(@PDSStep(name = "service call", description = "cancels job if executed by this PDS and handles orphaned cancel requestes", number = 2))
    public void handleJobCancelRequests() {
        List<PDSJob> jobsWhereCancelIsRequested = repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED);

        int size = jobsWhereCancelIsRequested.size();
        if (size == 0) {
            LOG.trace("Did not find jobs where cancel is requested");
            return;
        }
        LOG.info("Found jobs where cancel is requested: {}", size);

        List<PDSJob> potentialOrphaned = new ArrayList<>();

        for (PDSJob jobToCancel : jobsWhereCancelIsRequested) {
            CancelResult result = executionService.cancel(jobToCancel.getUUID());

            switch (result) {
            case JOB_FOUND_JOB_ALREADY_DONE:
            case JOB_FOUND_CANCEL_WAS_DONE:
                break;
            /* when not handled here, it is a potential orphan */
            case JOB_FOUND_CANCEL_WAS_NOT_POSSIBLE:
            case JOB_NOT_FOUND:
                potentialOrphaned.add(jobToCancel);
                break;
            default:
                LOG.error("The handling for execution result:{} is not implemented!", result);

            }
        }

        handlePotentialOrphans(potentialOrphaned);

    }

    private void handlePotentialOrphans(List<PDSJob> potentialOrphaned) {
        if (potentialOrphaned.isEmpty()) {
            LOG.trace("Did not find jobs having potential orphaned cancel requests.");
            return;
        }
        Instant lastAccepted = LocalDateTime.now().minusMinutes(minutesToWaitBeforeTreatedAsOrphaned).toInstant(ZoneOffset.UTC);

        LOG.info("Inspect potential orphaned cancel requests: {}", potentialOrphaned.size());
        int amountOfOrphansfound = 0;
        int amountOfOrpahnsStillExisiting = 0;

        for (PDSJob potentialOrphan : potentialOrphaned) {

            if (isOrphaned(potentialOrphan, lastAccepted)) {

                amountOfOrphansfound++;

                if (!hardSetJobStateToCanceledIfpossible(potentialOrphan.getUUID())) {
                    amountOfOrpahnsStillExisiting++;
                }
            }
        }
        LOG.info("Found {} oprhans. After processing {} orphans still exists.", amountOfOrphansfound, amountOfOrpahnsStillExisiting);
    }

    private boolean hardSetJobStateToCanceledIfpossible(UUID pdsJobUUID) {
        try {
            LOG.info("Recognized orphaned cancel request for PDS job: {}. Will try to hard set job state to CANCELED", pdsJobUUID);

            jobTransactionService.markJobAsCanceledInOwnTransaction(pdsJobUUID);

            LOG.warn(
                    "PDS job:{} has been hard marked as canceled. This was only a cleanup in database! The origin PDS server launcher script process was not really stopped.");

            return true;

        } catch (ConcurrencyFailureException ce) {
            Optional<PDSJob> jobReadAgainOpt = repository.findById(pdsJobUUID);
            if (!jobReadAgainOpt.isPresent()) {
                LOG.info("The job {} is no longer found. So cancel is not needed.", pdsJobUUID);
                /* maybe auto cleanup... no longer existing - so just ignore */
                return true;
            }
            PDSJob jobReadAgain = jobReadAgainOpt.get();
            if (PDSJobStatusState.CANCELED.equals(jobReadAgain.getState())) {
                LOG.info("Cancel operation was already done by another PDS instance for PDS job: {}. New state found: {}", jobReadAgain.getUUID(),
                        jobReadAgain.getState());
                /* just already done by another PDS instance - so ignore gracefully */
                return true;
            }
            LOG.warn("Concurency problem - cannot reset state for job: {}. And was also not done by another PDS.", pdsJobUUID, ce);
            return false;
        }
    }

    /*
     * Returns true when the given PDS job is older than the accepted amount of
     * time. Means: no other cluster member will execute this orphaned job any
     * longer, false when not orphaned
     *
     */
    private boolean isOrphaned(PDSJob job, Instant lastAccepted) {
        LocalDateTime creationTime = job.getCreated();
        if (creationTime == null) {
            LOG.warn("PDS job: {} had no creation timestamp! Strange situation. Accepted as orphaned anyway.", job.getUUID());
            return true;
        }
        Instant creationTimeInstant = creationTime.toInstant(ZoneOffset.UTC);
        boolean orphaned = creationTimeInstant.isBefore(lastAccepted);

        LOG.debug("PDS job: {}, orphan detected: {}, instantAcceptedLast:{}, creationTimeInstant:{}", job.getUUID(), lastAccepted, creationTimeInstant);

        return orphaned;
    }

}
