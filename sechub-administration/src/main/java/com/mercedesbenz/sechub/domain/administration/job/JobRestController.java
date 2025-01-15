// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserCancelsJob;

import jakarta.annotation.security.RolesAllowed;

@RestController
public class JobRestController {

    private final UserContextService userContextService;
    private final JobCancelService jobCancelService;

    public JobRestController(UserContextService userContextService, JobCancelService jobCancelService) {
        this.userContextService = userContextService;
        this.jobCancelService = jobCancelService;
    }

    @UseCaseUserCancelsJob(@Step(number = 1, name = "Rest call", description = "Triggers job cancellation request, owners of project will be informed", needsRestDoc = true))
    @PostMapping(path = AdministrationAPIConstants.API_USER_CANCEL_JOB, produces = { MediaType.APPLICATION_JSON_VALUE })
    @RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void userCancelJob(@PathVariable(name = "jobUUID") UUID jobUUID) {
        String userId = userContextService.getUserId();
        jobCancelService.userCancelJob(jobUUID, userId);
    }
}
