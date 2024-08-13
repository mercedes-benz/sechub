// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

/**
 * With this Scheduler strategy, the logic for fetching the next job to execute
 * does only allow one job for one project and one module group at same
 * time.<br>
 * <br>
 * An example:<br>
 * Users have created jobs in following order (all jobs are marked as ready to
 * start).
 *
 * <ul>
 * <li>Job A</li>code scan for project1
 * <li>Job B</li>code scan for project1
 * <li>Job C</li>web scan for project1
 * <li>Job D</li>code scan and license scan for project2
 * <li>Job E</li>web scan for project2
 * <li>Job F</li>web scan for project2
 * <li>Job G</li>code scan for project2
 * </ul>
 *
 * The scheduler will start jobs in following order:
 * <ul>
 * <li>Job A</li>
 * <li>Job C</li>
 * <li>Job D</li>
 * <li>Job E</li>
 * </ul>
 *
 * Following jobs will wait for the others to be done:
 * <ul>
 * <li>Job B</li> will not be started until Job A has ended
 * <li>Job F</li> will not be started until Job E has ended
 * <li>Job G</li> will not be started until Job D has ended
 * </ul>
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy implements SchedulerStrategy {

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Override
    public SchedulerStrategyId getSchedulerId() {
        return SchedulerStrategyId.ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP;
    }

    @Override
    public UUID nextJobId() {
        Set<Long> supportedPoolIds = encryptionService.getCurrentEncryptionPoolIds();

        Optional<UUID> nextJob = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted(supportedPoolIds);
        if (!nextJob.isPresent()) {
            return null;
        }

        return nextJob.get();
    }

}
