// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserRequestsJobCancellation;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSCancelJobService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCancelJobService.class);

    @Autowired
    PDSJobRepository repository;

    @Autowired
    PDSExecutionService executionService;

    @UseCaseUserRequestsJobCancellation(@PDSStep(name = "service call", description = "trigger change to execution service and marks job status as cancel requested", number = 2))
    public void cancelJob(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);
        if (PDSJobStatusState.CANCEL_REQUESTED.equals(job.getState()) || PDSJobStatusState.CANCELED.equals(job.getState())) {
            LOG.info("Cancel ignored because in state:{}", job.getState());
            return;
        }
        assertJobIsInState(job, PDSJobStatusState.RUNNING);

        LOG.info("Trigger cancel for job {} ", jobUUID);
        executionService.cancel(jobUUID);

        job.setState(PDSJobStatusState.CANCEL_REQUESTED);

        repository.save(job);

    }

}
