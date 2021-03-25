// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public interface SchedulerStrategy {
    
    public SchedulerStrategyId getSchedulerId();
    
    public UUID nextJobId();
}
