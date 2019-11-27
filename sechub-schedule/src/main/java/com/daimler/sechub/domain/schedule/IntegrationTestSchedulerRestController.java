// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.schedule.access.ScheduleAccessCountService;
import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;

/**
 * Contains additional rest call functionality for integration tests on scan domain
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestSchedulerRestController {

	@Autowired
	private ScheduleAccessCountService scheduleAccessCountService;

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/schedule/access/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public long countProjectAccess(@PathVariable("projectId") String projectId) {
		return scheduleAccessCountService.countProjectAccess(projectId);
	}


}
