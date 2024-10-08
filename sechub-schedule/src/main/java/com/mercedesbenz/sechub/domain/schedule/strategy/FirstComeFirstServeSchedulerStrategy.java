// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

@Component
public class FirstComeFirstServeSchedulerStrategy implements SchedulerStrategy {

    @Autowired
    public SecHubJobRepository jobRepository;

    @Override
    public SchedulerStrategyId getSchedulerStrategyId() {
        return SchedulerStrategyId.FIRST_COME_FIRST_SERVE;
    }

    @Override
    public Optional<UUID> nextJobId(Set<Long> supportedEncryptionPoolIds) {
        return jobRepository.nextJobIdToExecuteFirstInFirstOut(supportedEncryptionPoolIds);
    }

}
