// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.UUID;

public interface SchedulerStrategy {

    public SchedulerStrategyId getSchedulerId();

    public UUID nextJobId();
}
