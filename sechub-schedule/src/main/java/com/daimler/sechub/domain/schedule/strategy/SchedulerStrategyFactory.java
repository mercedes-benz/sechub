package com.daimler.sechub.domain.schedule.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchedulerStrategyFactory {

    @Value("sechub.scheduler.strategy.id")
    private String strategyId;

    public SchedulerStrategy build() {
        
        SchedulerStrategyId strategy = SchedulerStrategyId.getId(strategyId);
        if (strategy == null) {
            return new FirstComeFirstServeSchedulerStrategy();
        }
        
        switch (strategy) {
        case FirstComeFirstServe:
            return new FirstComeFirstServeSchedulerStrategy();
        case OnlyOneScanPerProjectAtATime:
            return new OnlyOneScanPerProjectAtSameTimeStrategy();
        default:
            return new FirstComeFirstServeSchedulerStrategy();
        }
    }
}
