// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.job;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.administration.AdministrationAPIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorListsAllRunningJobs;

/**
 * The rest api for job administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class JobAdministrationRestController {

	@Autowired
	JobInformationListService jobListService;


	/* @formatter:off */
	@UseCaseAdministratorListsAllRunningJobs(
			@Step(
				number=1,
				name="Rest call",
				needsRestDoc=true,
				description="Administrator lists all running jobs by calling rest api"))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_JOBS_RUNNING, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public List<JobInformation> fetchAllRunningJobs() {
		/* @formatter:on */
		return jobListService.fetchRunningJobs();
	}

}
