// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class SchedulerStrategyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerStrategyProvider.class);

    @Autowired
    FirstComeFirstServeSchedulerStrategy firstComeFirstServeStrategy;

    @Autowired
    OnlyOneScanPerProjectAtSameTimeStrategy onlyOneScanPerProjectStrategy;

    @Autowired
    OnlyOneScanPerProjectAndModuleGroupAtSameTimeStrategy onlyOneScanPerProjectAndModuleGroupStrategy;

    @MustBeDocumented("Define the scheduler strategy by given identifier. This strategy determines the next job which shall be executed by job scheduler. Possible values are:"
            + SchedulerStrategyConstants.FIRST_COME_FIRST_SERVE + "," + SchedulerStrategyConstants.ONLY_ONE_SCAN_PER_PROJECT_AT_A_TIME + " and "
            + SchedulerStrategyConstants.ONLY_ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP)
    @Value("${sechub.scheduler.strategy.id:}")
    private String strategyIdentifier;

    private SchedulerStrategyId currentStrategyId;

    /**
     * Provides the strategy for the scheduler. The strategy type is be defined by
     * {@link #currentStrategyId} on server start.
     *
     * @return strategy for scheduler
     */
    public SchedulerStrategy getStrategy() {

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
            return onlyOneScanPerProjectStrategy;

        case ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP:
            return onlyOneScanPerProjectAndModuleGroupStrategy;

        case FIRST_COME_FIRST_SERVE:
        default:
            return firstComeFirstServeStrategy;
        }
    }
}
