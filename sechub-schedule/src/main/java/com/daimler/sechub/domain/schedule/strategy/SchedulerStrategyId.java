// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

public enum SchedulerStrategyId {
    FirstComeFirstServe("first-come-first-serve"),
    OnlyOneScanPerProjectAtATime("only-one-scan-per-project-at-a-time");

    private String strategy;

    SchedulerStrategyId(String strategyString) {

        if (strategyString == null ) {
            throw new IllegalArgumentException("strategyString may not be null!");
        }
        if (strategyString.isEmpty()) {
            throw new IllegalArgumentException("strategyString may not be empty!");
        }
        
        this.strategy = strategyString;
    }

    public static SchedulerStrategyId getId(String strategyString) {
        for (SchedulerStrategyId value : values()) {
            if (value.strategy.equals(strategyString)) {
                return value;
            }
        }
        return null;
    }
}
