// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminListsAllRunningJobs;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;

/**
 * The rest api for job administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile({ Profiles.TEST, Profiles.ADMIN_ACCESS })
public class JobAdministrationRestController {

    @Autowired
    JobInformationListService jobListService;

    @Autowired
    JobCancelService jobCancelService;

    @Autowired
    JobRestartRequestService jobRestartRequestService;

    /* @formatter:off */
	@UseCaseAdminListsAllRunningJobs(
			@Step(
				number=1,
				name="Rest call",
				needsRestDoc=true,
				description="Administrator lists all running jobs by calling rest api"))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_JOBS_RUNNING, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public List<JobInformationListEntry> fetchAllRunningJobs() {
		/* @formatter:on */
        return jobListService.fetchRunningJobs();
    }

    /* @formatter:off */
	@UseCaseAdminCancelsJob(@Step(number=1,name="Rest call",description="Triggers job cancellation request, owners of project will be informed",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_ADMIN_CANCELS_JOB, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void cancelJob(@PathVariable(name="jobUUID") UUID jobUUID) {
		/* @formatter:on */
        jobCancelService.cancelJob(jobUUID);
    }

    /* @formatter:off */
    @UseCaseAdminRestartsJob(@Step(number=1,name="Rest call",description="Triggeres job restart (soft) ",needsRestDoc=true))
    @RequestMapping(path = AdministrationAPIConstants.API_ADMIN_RESTARTS_JOB, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
    public void restartJob(@PathVariable(name="jobUUID") UUID jobUUID) {
        /* @formatter:on */
        jobRestartRequestService.restartJob(jobUUID);
    }

    /* @formatter:off */
    @UseCaseAdminRestartsJobHard(@Step(number=1,name="Rest call",description="Triggeres job restart (hard)",needsRestDoc=true))
    @RequestMapping(path = AdministrationAPIConstants.API_ADMIN_RESTARTS_JOB_HARD, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
    public void restartJobHard(@PathVariable(name="jobUUID") UUID jobUUID) {
        /* @formatter:on */
        jobRestartRequestService.restartJobHard(jobUUID);
    }

}
