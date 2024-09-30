// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

@Component
public class OnlyOneScanPerProjectAtSameTimeStrategy implements SchedulerStrategy {

    @Autowired
    SecHubJobRepository jobRepository;

    @Override
    public SchedulerStrategyId getSchedulerStrategyId() {
        return SchedulerStrategyId.ONE_SCAN_PER_PROJECT;
    }

    @Override
    public Optional<UUID> nextJobId(Set<Long> supportedEncryptionPoolIds) {
        return jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(supportedEncryptionPoolIds);
    }

}
