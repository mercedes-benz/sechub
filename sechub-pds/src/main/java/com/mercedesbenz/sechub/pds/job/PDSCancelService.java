// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService.CancelResult;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemHandlesJobCancelRequests;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSCancelService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCancelService.class);
    private static final int DEFAULT_TIME_IN_MINUTES_BEFORE_TREATED_AS_ORPHANED_CANCEL = 60;

    @PDSMustBeDocumented("The time in minutes after which a cancel job request is treated as orphaned.")
    @Value("${sechub.pds.config.cancelrequest.minutes.before.treated.orphaned:" + DEFAULT_TIME_IN_MINUTES_BEFORE_TREATED_AS_ORPHANED_CANCEL + "}")
    int minutesToWaitBeforeTreatedAsOrphaned = DEFAULT_TIME_IN_MINUTES_BEFORE_TREATED_AS_ORPHANED_CANCEL;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSExecutionService executionService;

    @UseCaseSystemHandlesJobCancelRequests(@PDSStep(name = "service call", description = "cancels job if executed by this PDS and handles orphaned cancel requestes", number = 2))
    public void handleJobCancelRequests() {

        List<PDSJob> jobsWhereCancelIsRequested = repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED);

        List<PDSJob> potentialOrphaned = new ArrayList<>();

        for (PDSJob jobToCancel : jobsWhereCancelIsRequested) {

            CancelResult result = executionService.cancel(jobToCancel.getUUID());

            switch (result) {
            case JOB_FOUND_JOB_ALREADY_DONE:
            case JOB_FOUND_CANCEL_WAS_DONE:
                break;
            case JOB_FOUND_CANCEL_WAS_NOT_POSSIBLE:
            case JOB_NOT_FOUND:
                potentialOrphaned.add(jobToCancel);
                break;
            default:
                throw new IllegalStateException("The handling for result:" + result + " is not implemented");

            }
        }

        handlePotentialOrphans(potentialOrphaned);

    }

    private void handlePotentialOrphans(List<PDSJob> potentialOrphaned) {
        if (potentialOrphaned.isEmpty()) {
            return;
        }
        Instant nHoursbefore = LocalDateTime.now().minusMinutes(minutesToWaitBeforeTreatedAsOrphaned).toInstant(ZoneOffset.UTC);

        for (PDSJob potentialOrphan : potentialOrphaned) {
            if (isOrphaned(potentialOrphan, nHoursbefore)) {
                hardSetJobStateToCanceledIfpossible(potentialOrphan);
            }
        }
    }

    private void hardSetJobStateToCanceledIfpossible(PDSJob potentialOrphan) {
        PDSJob jobToHardReset = potentialOrphan;
        jobToHardReset.setState(PDSJobStatusState.CANCELED);
        try {
            LOG.info("Recognized orphaned cancel request for PDS job: {}. Will hard reset state to: {}", jobToHardReset.getUUID(), jobToHardReset.getState());
            repository.save(jobToHardReset);

        } catch (ConcurrencyFailureException ce) {
            Optional<PDSJob> jobReadAgainOpt = repository.findById(jobToHardReset.getUUID());
            if (!jobReadAgainOpt.isPresent()) {
                LOG.info("The job {} is no longer found. So cancel is not possible.", jobToHardReset.getUUID());
                /* maybe auto cleanup... no longer existing - so just ignore */
                return;
            }
            PDSJob jobReadAgain = jobReadAgainOpt.get();
            if (!PDSJobStatusState.CANCEL_REQUESTED.equals(jobReadAgain.getState())) {
                LOG.info("Cancel operation was already done by another PDS instance for PDS job: {}. New state found: {}", jobReadAgain.getUUID(),
                        jobReadAgain.getState());
                /* just already done by another PDS instance - so ignore gracefully */
                return;
            }
            LOG.warn("Concurency problem - cannot reset state for job: {}. And was also not done by another PDS.", jobToHardReset.getUUID(), ce);
        }
    }

    private boolean isOrphaned(PDSJob job, Instant instantAcceptedLast) {
        LocalDateTime creationTime = job.getCreated();
        if (creationTime == null) {
            LOG.warn("PDS job: {} had no creation timestamp! Strange situation. Accepted as orphaned anyway.", job.getUUID());
            return true;
        }
        Instant creationTimeInstant = creationTime.toInstant(ZoneOffset.UTC);
        boolean orphaned = creationTimeInstant.isBefore(instantAcceptedLast);

        LOG.debug("PDS job: {}, orphan detected: {}, instantAcceptedLast:{}, creationTimeInstant:{}", job.getUUID(), instantAcceptedLast, creationTimeInstant);

        return orphaned;
    }

}
