// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchedulerStrategyFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerStrategyFactory.class);
    
    @Autowired
    FirstComeFirstServeSchedulerStrategy fifoStrategy;
    
    @Autowired
    OnlyOneScanPerProjectAtSameTimeStrategy oosppStrategy;

    @Value("${sechub.scheduler.strategy.id:}")
    private String strategyId;
    
    private SchedulerStrategyId currentStrategyId;

    public SchedulerStrategy build() {
               
        SchedulerStrategyId strategy = SchedulerStrategyId.getId(strategyId);
        
        if (strategy == null) {
            strategy = SchedulerStrategyId.FirstComeFirstServe;
        }
        
        if (currentStrategyId != null && strategy != null && currentStrategyId == strategy) {
            return getStrategy(currentStrategyId);
        }
        
        if (currentStrategyId != strategy) {
            LOG.info("SCHEDULER STRATEGY : " + strategy.toString());
        }
        
        currentStrategyId = strategy;
        
        return getStrategy(strategy);
    }
    
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }
    
    private SchedulerStrategy getStrategy(SchedulerStrategyId id) {
        switch (id) {
        case FirstComeFirstServe:
            return fifoStrategy;
        case OnlyOneScanPerProjectAtATime:
            return oosppStrategy;
        default:
            return fifoStrategy;
        }
    }
}
