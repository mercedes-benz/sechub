// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesJobResultOrFailureText;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobResult;

import jakarta.annotation.security.RolesAllowed;

@Service
public class PDSGetJobResultService {

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserFetchesJobResult(@PDSStep(name = "service call", description = "Fetches job result from database. When job is not already done a failure will be shown", number = 2))
    @RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER })
    public String getJobResult(UUID jobUUID) {
        return getJobResult(jobUUID, true);
    }

    @RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
    @UseCaseAdminFetchesJobResultOrFailureText(@PDSStep(name = "service call", description = "Result data will be returned - can be empty or even a failure text from job execution.", number = 2))
    public String getJobResultOrFailureText(UUID jobUUID) {
        return getJobResult(jobUUID, false);
    }

    private String getJobResult(UUID jobUUID, boolean onlyWhenDone) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);
        if (onlyWhenDone) {
            assertJobIsInState(job, PDSJobStatusState.DONE);
        }
        return job.getResult();
    }

}
