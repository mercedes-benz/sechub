// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

@Component
public class OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy implements SchedulerStrategy {

    @Autowired
    SecHubJobRepository jobRepository;

    @Override
    public SchedulerStrategyId getSchedulerId() {
        return SchedulerStrategyId.ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP;
    }

    @Override
    public UUID nextJobId() {
        Optional<UUID> nextJob = jobRepository.nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();
        if (!nextJob.isPresent()) {
            return null;
        }

        return nextJob.get();
    }

}
