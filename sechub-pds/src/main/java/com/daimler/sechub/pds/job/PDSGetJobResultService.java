// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesJobResultOrFailureText;
import com.daimler.sechub.pds.usecase.UseCaseUserFetchesJobResult;

@Service
@RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
public class PDSGetJobResultService {

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserFetchesJobResult(@PDSStep(name = "service call", description = "job result fetch from database, when not done a failure will be shown", number = 2))
    @RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER })
    public String getJobResult(UUID jobUUID) {
        return getJobResult(jobUUID, true);
    }

    @RolesAllowed(PDSRoleConstants.ROLE_SUPERADMIN)
    @UseCaseAdminFetchesJobResultOrFailureText(@PDSStep(name = "service call", description = "result data will be returned - can be empty or even a failure text from job execution.", number = 2))
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
