// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserRequestsJobCancelation;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_USER, PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSRequestJobCancelationService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSRequestJobCancelationService.class);

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserRequestsJobCancelation(@PDSStep(name = "service call", description = "marks job status as cancel requested", number = 2))
    public void requectJobCancelation(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);
        if (PDSJobStatusState.CANCEL_REQUESTED.equals(job.getState()) || PDSJobStatusState.CANCELED.equals(job.getState())) {
            LOG.info("Cancel request ignored because in state:{}", job.getState());
            return;
        }
        assertJobIsInState(job, PDSJobStatusState.RUNNING);

        LOG.info("Request cancelation of PDS job: {} ", jobUUID);

        job.setState(PDSJobStatusState.CANCEL_REQUESTED);

        repository.save(job);

    }

}
