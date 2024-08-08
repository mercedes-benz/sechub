// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

@Component
public class FirstComeFirstServeSchedulerStrategy implements SchedulerStrategy {

    @Autowired
    public SecHubJobRepository jobRepository;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Override
    public SchedulerStrategyId getSchedulerId() {
        return SchedulerStrategyId.FIRST_COME_FIRST_SERVE;
    }

    @Override
    public UUID nextJobId() {
        Set<Long> supportedPoolIds = encryptionService.getCurrentEncryptionPoolIds();

        Optional<UUID> nextJob = jobRepository.nextJobIdToExecuteFirstInFirstOut(supportedPoolIds);
        if (!nextJob.isPresent()) {
            return null;
        }

        return nextJob.get();
    }

}
