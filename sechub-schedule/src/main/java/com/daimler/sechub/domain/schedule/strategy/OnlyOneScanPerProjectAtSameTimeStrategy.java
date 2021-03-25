// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;

@Component
public class OnlyOneScanPerProjectAtSameTimeStrategy implements SchedulerStrategy {

    @Autowired
    SecHubJobRepository jobRepository;
    
    @Override
    public SchedulerStrategyId getSchedulerId() {
        return SchedulerStrategyId.OnlyOneScanPerProjectAtATime;
    }

    @Override
    public UUID nextJobId() {
        Optional<UUID> nextJob = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted();
        if (!nextJob.isPresent()) {
            return null;
        }
        
        return nextJob.get();
    }

}
