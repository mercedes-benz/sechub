// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SchedulerStrategy {

    public SchedulerStrategyId getSchedulerStrategyId();

    public Optional<UUID> nextJobId(Set<Long> supportedEncryptionPoolIds);
}
