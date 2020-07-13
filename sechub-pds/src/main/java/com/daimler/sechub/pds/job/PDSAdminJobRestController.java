// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.pds.PDSAPIConstants;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesJobResultOrFailureText;

/**
 * The REST API for PDS jobs
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(PDSAPIConstants.API_ADMIN)
@RolesAllowed({PDSRoleConstants.ROLE_SUPERADMIN})
public class PDSAdminJobRestController {

	@Autowired
    private PDSGetJobResultService jobResultService;
	
	/* @formatter:off */
    @Validated
    @RequestMapping(path = "job/{jobUUID}/result", method = RequestMethod.GET)
    @UseCaseAdminFetchesJobResultOrFailureText(@PDSStep(name="rest call",description = "an admin fetches result or failure text for job from db.",number=1))
    public String getJobResultOrFailureText(
            @PathVariable("jobUUID") UUID jobUUID
            ) {
        /* @formatter:on */
        return jobResultService.getJobResultOrFailureText(jobUUID);
    }
	

}
