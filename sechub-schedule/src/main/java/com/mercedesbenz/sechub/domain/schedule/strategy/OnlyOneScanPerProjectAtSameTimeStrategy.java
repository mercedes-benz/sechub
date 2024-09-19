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
public class OnlyOneScanPerProjectAtSameTimeStrategy implements SchedulerStrategy {

    @Autowired
    SecHubJobRepository jobRepository;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Override
    public SchedulerStrategyId getSchedulerId() {
        return SchedulerStrategyId.ONE_SCAN_PER_PROJECT;
    }

    @Override
    public UUID nextJobId() {
        Set<Long> supportedPoolIds = encryptionService.getCurrentEncryptionPoolIds();

        Optional<UUID> nextJob = jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(supportedPoolIds);
        if (!nextJob.isPresent()) {
            return null;
        }

        return nextJob.get();
    }

}
