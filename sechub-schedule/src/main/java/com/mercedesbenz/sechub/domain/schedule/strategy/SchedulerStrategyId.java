// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

public enum SchedulerStrategyId {

    FIRST_COME_FIRST_SERVE(SchedulerStrategyConstants.FIRST_COME_FIRST_SERVE),

    ONE_SCAN_PER_PROJECT(SchedulerStrategyConstants.ONLY_ONE_SCAN_PER_PROJECT_AT_A_TIME),

    ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP(SchedulerStrategyConstants.ONLY_ONE_SCAN_PER_PROJECT_AND_MODULE_GROUP)

    ;

    private String identifier;

    SchedulerStrategyId(String identifier) {

        if (identifier == null) {
            throw new IllegalArgumentException("identifier may not be null!");
        }
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("identifier may not be empty!");
        }

        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static SchedulerStrategyId getByIdentifier(String strategyIdAsText) {
        for (SchedulerStrategyId value : values()) {
            if (value.identifier.equals(strategyIdAsText)) {
                return value;
            }
        }
        return null;
    }
}
