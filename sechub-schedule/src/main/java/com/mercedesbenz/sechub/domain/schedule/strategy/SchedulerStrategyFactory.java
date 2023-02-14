// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class SchedulerStrategyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerStrategyFactory.class);

    @Autowired
    FirstComeFirstServeSchedulerStrategy fifoStrategy;

    @Autowired
    OnlyOneScanPerProjectAtSameTimeStrategy oosppStrategy;

    @Autowired
    OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy oosppagStrategy;

    @MustBeDocumented("Define the scheduler strategy by given identifier. This strategy determines the next job which shall be executed by job scheduler. Possible values are:"
            + SchedulerStrategyConstants.FIRST_COME_FIRST_SERVE + "," + SchedulerStrategyConstants.ONLY_ONE_SCAN_PER_PROJECT_AT_A_TIME + " and "
            + SchedulerStrategyConstants.ONLY_ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP)
    @Value("${sechub.scheduler.strategy.id:}")
    private String strategyIdentifier;

    private SchedulerStrategyId currentStrategyId;

    public SchedulerStrategy build() {

        SchedulerStrategyId strategy = SchedulerStrategyId.getByIdentifier(strategyIdentifier);

        if (strategy == null) {
            strategy = SchedulerStrategyId.FIRST_COME_FIRST_SERVE;
        }

        if (currentStrategyId != null && strategy != null && currentStrategyId == strategy) {
            return getStrategy(currentStrategyId);
        }

        if (currentStrategyId != strategy) {
            LOG.info("Building scheduler strategy: {}", strategy);
        }

        currentStrategyId = strategy;

        return getStrategy(strategy);
    }

    public void setStrategyIdentifier(String strategyId) {
        this.strategyIdentifier = strategyId;
    }

    private SchedulerStrategy getStrategy(SchedulerStrategyId id) {
        switch (id) {

        case ONE_SCAN_PER_PROJECT:
            return oosppStrategy;

        case ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP:
            return oosppagStrategy;

        case FIRST_COME_FIRST_SERVE:
        default:
            return fifoStrategy;
        }
    }
}
