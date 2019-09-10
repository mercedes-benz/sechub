// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.schedule;

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
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorStartScheduler;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorStopScheduler;
import com.daimler.sechub.sharedkernel.usecases.admin.schedule.UseCaseAdministratorTriggersRefreshOfSchedulerStatus;

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
	StartSchedulerService startSchedulerService;

	@Autowired
	StopSchedulerService stopSchedulerService;

	@Autowired
	TriggerSchedulerStatusRefreshService triggerRefreshService;

	/* @formatter:off */
	@UseCaseAdministratorStartScheduler(@Step(number=1,name="Rest call",description="Administrator wants to start (unpause) scheduler",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_START, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void startScheduler() {
		/* @formatter:on */
		startSchedulerService.startScheduler();
	}

	/* @formatter:off */
	@UseCaseAdministratorStopScheduler(@Step(number=1,name="Rest call",description="Administrator wants to stop (pause) scheduler",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_STOP, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void stopScheduler() {
		/* @formatter:on */
		stopSchedulerService.stopScheduler();
	}

	/* @formatter:off */
	@UseCaseAdministratorTriggersRefreshOfSchedulerStatus(@Step(number=1,name="Rest call",description="Administrator wants to refresh information about scheduler status",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_STATUS_REFRESH, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void triggerSchedulerStatusRefresh() {
		/* @formatter:on */
		triggerRefreshService.triggerSchedulerStatusRefresh();
	}

}