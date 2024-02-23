// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobStatus;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER })
public class PDSGetJobStatusService {

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserFetchesJobStatus(@PDSStep(name = "service call", description = "returns job status", number = 2))
    public PDSJobStatus getJobStatus(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);

        return new PDSJobStatus(job);
    }

}
