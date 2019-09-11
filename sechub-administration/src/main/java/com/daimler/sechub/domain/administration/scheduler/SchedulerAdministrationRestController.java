// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.scheduler;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.administration.AdministrationAPIConstants;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorDisablesSchedulerJobProcessing;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorEnablesSchedulerJobProcessing;

/**
 * The rest api for user administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class SchedulerAdministrationRestController {

	@Autowired
	SwitchSchedulerJobProcessingService switchJobProcessingService;

	@Autowired
	TriggerSchedulerStatusRefreshService triggerRefreshService;

	/* @formatter:off */
	@UseCaseAdministratorEnablesSchedulerJobProcessing(@Step(number=1,name="Rest call",description="Administrator wants to start (unpause) scheduler job processing",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_ENABLE_JOB_PROCESSING, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void enableJobProcessing() {
		/* @formatter:on */
		switchJobProcessingService.enableJobProcessing();
	}

	/* @formatter:off */
	@UseCaseAdministratorDisablesSchedulerJobProcessing(@Step(number=1,name="Rest call",description="Administrator wants to stop (pause) scheduler job processing",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_DISABLE_JOB_PROCESSING, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void disableJobProcessing() {
		/* @formatter:on */
		switchJobProcessingService.disableJobProcessing();
	}


}