// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SchedulerStrategyIdTest {

    @Test
    void test_value_to_enum_mapping_succesful() {
        /* execute */
        SchedulerStrategyId fifoServe = SchedulerStrategyId.getId("first-come-first-serve");
        SchedulerStrategyId onlyOneScanAtATime = SchedulerStrategyId.getId("only-one-scan-per-project-at-a-time");

        /* test */
        assertEquals(SchedulerStrategyId.FirstComeFirstServe, fifoServe);
        assertEquals(SchedulerStrategyId.OnlyOneScanPerProjectAtATime, onlyOneScanAtATime);
    }

    @Test
    void test_unknown_value_to_enum_failure() {
        /* prepare */
        SchedulerStrategyId strategyId = SchedulerStrategyId.getId("gibberish");

        /* test */
        assertNull(strategyId);
    }

}
