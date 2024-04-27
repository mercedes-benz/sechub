// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminDisablesSchedulerJobProcessing;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminEnablesSchedulerJobProcessing;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdminTriggersRefreshOfSchedulerStatus;

import jakarta.annotation.security.RolesAllowed;

/**
 * The rest api for user administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class SchedulerAdministrationRestController {

    @Autowired
    SwitchSchedulerJobProcessingService switchJobProcessingService;

    @Autowired
    TriggerSchedulerStatusRefreshService triggerRefreshService;

    /* @formatter:off */
	@UseCaseAdminEnablesSchedulerJobProcessing(@Step(number=1,name="Rest call",description="Administrator wants to start (unpause) scheduler job processing",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_ENABLE_JOB_PROCESSING, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void enableJobProcessing() {
		/* @formatter:on */
        switchJobProcessingService.enableJobProcessing();
    }

    /* @formatter:off */
	@UseCaseAdminDisablesSchedulerJobProcessing(@Step(number=1,name="Rest call",description="Administrator wants to stop (pause) scheduler job processing",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_DISABLE_JOB_PROCESSING, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void disableJobProcessing() {
		/* @formatter:on */
        switchJobProcessingService.disableJobProcessing();
    }

    /* @formatter:off */
	@UseCaseAdminTriggersRefreshOfSchedulerStatus(@Step(number=1,name="Rest call",description="Administrator wants to trigger a refresh of scheduler status. Will update information about running, waiting and all jobs in scheduler etc. etc.",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_STATUS_REFRESH, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void triggerRefreshOfSchedulerStatus() {
		/* @formatter:on */
        triggerRefreshService.triggerSchedulerStatusRefresh();
    }

}