// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchedulerStrategyFactory {
    
    @Autowired
    FirstComeFirstServeSchedulerStrategy fifoStrategy;
    
    @Autowired
    OnlyOneScanPerProjectAtSameTimeStrategy oosppStrategy;

    @Value("sechub.scheduler.strategy.id")
    private String strategyId;

    public SchedulerStrategy build() {
        
        SchedulerStrategyId strategy = SchedulerStrategyId.getId(strategyId);
        if (strategy == null) {
            return fifoStrategy;
        }
        
        switch (strategy) {
        case FirstComeFirstServe:
            return fifoStrategy;
        case OnlyOneScanPerProjectAtATime:
            return oosppStrategy;
        default:
            return fifoStrategy;
        }
    }
}
