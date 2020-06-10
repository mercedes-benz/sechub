// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserMarksFalsePositivesForJob;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserUnmarksFalsePositives;

/**
 * The rest API for project false positive handling - API centric 
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_PROJECT+"{projectId}") 
@RolesAllowed({RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN})
public class FalsePositiveRestController {

	@Autowired
	private FalsePositiveJobDataService falsePositiveJobDataService;


	/* @formatter:off */
	@UseCaseUserMarksFalsePositivesForJob(@Step(number=1,name="REST API call to define false positives by JSON data containing identifiers for existing job",needsRestDoc=true))
	@RequestMapping(path = "/false-positives", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    public void addFalsePositivesByJobData(
            @PathVariable("projectId") String projectId,
            @RequestBody FalsePositiveJobDataList data
            ) {
        /* @formatter:on */
	    falsePositiveJobDataService.addFalsePositives(projectId, data);

    }
	

    /* @formatter:off */
    @UseCaseUserUnmarksFalsePositives(@Step(number=1,name="REST API call to remove false positives by JSON data containing identifiers and job UUID",needsRestDoc=true))
    @RequestMapping(path = "/false-positives", method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_VALUE})
    public void removeFalsePositivesByJobData(
            @PathVariable("projectId") String projectId,
            @RequestBody FalsePositiveJobDataList data
            ) {
        /* @formatter:on */
        falsePositiveJobDataService.removeFalsePositives(projectId, data);

    }

}
