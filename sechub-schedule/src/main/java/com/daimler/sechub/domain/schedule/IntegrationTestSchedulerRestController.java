// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.schedule.access.ScheduleAccessCountService;
import com.daimler.sechub.domain.schedule.strategy.SchedulerStrategyFactory;
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
	
	@Autowired
    private IntegrationTestSchedulerService integrationTestSchedulerService;
	
	@Autowired
	private SchedulerStrategyFactory schedulerStrategyFactory;

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/schedule/access/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public long countProjectAccess(@PathVariable("projectId") String projectId) {
		return scheduleAccessCountService.countProjectAccess(projectId);
	}
	
	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/jobs/waiting", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void deleteWaitingJobs() {
	    integrationTestSchedulerService.deleteWaitingJobs();
	}

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/schedule/revert/job/{sechubJobUUID}/still-running", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void revertJobAsStillRunning(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {;
	    integrationTestSchedulerService.revertJobAsStillRunning(sechubJobUUID);
	}
	
	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/schedule/revert/job/{sechubJobUUID}/still-not-approved", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void revertJobAsStillNotApproved(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {;
        integrationTestSchedulerService.revertJobAsStillNotApproved(sechubJobUUID);
    }
	
	@RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/scheduler/strategy/{strategyId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void setSchedulerStrategy(@PathVariable("strategyId") String strategyId) {
	    schedulerStrategyFactory.setStrategyId(strategyId);
	}

}
