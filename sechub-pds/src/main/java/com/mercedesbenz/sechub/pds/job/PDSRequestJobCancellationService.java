// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserRequestsJobCancellation;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor.ExceptionThrower;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSRequestJobCancellationService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSRequestJobCancellationService.class);

    private ExceptionThrower<IllegalStateException> pdsJobUpdateExceptionThrower;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSJobTransactionService transactionService;

    public PDSRequestJobCancellationService() {

        pdsJobUpdateExceptionThrower = new ExceptionThrower<IllegalStateException>() {

            @Override
            public void throwException(String message, Exception cause) throws IllegalStateException {
                throw new IllegalStateException("PDS Job cancellation state update failed. " + message, cause);
            }
        };
    }

    @UseCaseUserRequestsJobCancellation(@PDSStep(name = "service call", description = "marks job status as cancel requested", number = 2))
    public void requestJobCancellation(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSResilientRetryExecutor<IllegalStateException> executor = new PDSResilientRetryExecutor<>(3, pdsJobUpdateExceptionThrower,
                OptimisticLockingFailureException.class);
        executor.execute(() -> {
            PDSJob job = assertJobFound(jobUUID, repository);
            if (PDSJobStatusState.CANCEL_REQUESTED.equals(job.getState()) || PDSJobStatusState.CANCELED.equals(job.getState())) {
                LOG.debug("Cancel request ignored because already in state:{}", job.getState());
                return;
            }
            PDSJobStatusState state = job.getState();
            switch (state) {
            case CANCELED:
            case CANCEL_REQUESTED:
            case DONE:
            case FAILED:
                LOG.info("PDS job is already in state: {} - so skip cancellation");
                return;
            case QUEUED:
            case READY_TO_START:
            case CREATED:
            case RUNNING:
            default:
                break;

            }
            LOG.info("PDS job is currently in state: {} - will now mark as cancel requested");

            LOG.info("Request cancellation of PDS job: {} ", jobUUID);

            transactionService.markJobAsCancelRequestedInOwnTransaction(jobUUID);
        }, jobUUID.toString());

    }

}
