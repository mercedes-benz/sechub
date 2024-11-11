// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccessCountService;
import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleCipherPoolCleanupService;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.domain.schedule.strategy.SchedulerStrategyProvider;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;

/**
 * Contains additional rest call functionality for integration tests on scan
 * domain
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
    private SchedulerStrategyProvider schedulerStrategyProvider;

    @Autowired
    private SchedulerConfigService scheduleConfigService;

    @Autowired
    private SecHubJobRepository jobRepository;

    @Autowired
    private ScheduleCipherPoolCleanupService scheduleCipherPoolCleanupService;

    @Autowired
    private SchedulerTerminationService schedulerTerminationService;

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/autocleanup/inspection/schedule/days", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long fetchScheduleAutoCleanupConfiguredDays() {
        return scheduleConfigService.getAutoCleanupInDays();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/schedule/cipher-pool-data/cleanup", method = RequestMethod.PUT, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void startScheduleAutoCleanupDirectlyForTesting() {
        scheduleCipherPoolCleanupService.cleanupCipherPoolDataIfNecessaryAndPossible();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/schedule/access/count", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long countProjectAccess(@PathVariable("projectId") String projectId) {
        return scheduleAccessCountService.countProjectAccess(projectId);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/jobs/waiting", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void deleteWaitingJobs() {
        integrationTestSchedulerService.deleteWaitingJobs();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS
            + "integrationtest/schedule/revert/job/{sechubJobUUID}/still-running", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void revertJobAsStillRunning(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        integrationTestSchedulerService.revertJobAsStillRunning(sechubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS
            + "integrationtest/schedule/revert/job/{sechubJobUUID}/still-not-approved", method = RequestMethod.PUT, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public void revertJobAsStillNotApproved(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        integrationTestSchedulerService.revertJobAsStillNotApproved(sechubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/scheduler/strategy/{strategyId}", method = RequestMethod.PUT, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void setSchedulerStrategy(@PathVariable("strategyId") String strategyId) {
        schedulerStrategyProvider.setStrategyIdentifier(strategyId);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS
            + "integrationtest/schedule/encryption-pool-id/job/{sechubJobUUID}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @Transactional
    public Long fetchEncryptionPoolIdForSecHubJob(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        Optional<ScheduleSecHubJob> job = jobRepository.findById(sechubJobUUID);
        if (job.isEmpty()) {
            throw new IllegalArgumentException("SecHub job: " + sechubJobUUID + " not found!");
        }
        return job.get().getEncryptionCipherPoolId();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/termination-state/{terminate}", method = RequestMethod.PUT)
    public void changeTerinationServiceState(@PathVariable("terminate") boolean terminate) {
        if (terminate) {
            schedulerTerminationService.terminate();
        } else {
            schedulerTerminationService.internalResetTermination();
        }
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/termination-state", method = RequestMethod.GET)
    public boolean fetchTerminationState() {
        return schedulerTerminationService.isTerminating();
    }

}
